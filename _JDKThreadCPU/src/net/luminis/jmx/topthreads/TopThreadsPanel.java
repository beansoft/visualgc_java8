/*
 * Copyright 2007-2016 Peter Doornbosch
 *
 * This file is part of TopThreads, a JConsole plugin to analyse CPU-usage per thread.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * TopThreads is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package net.luminis.jmx.topthreads;

import javax.management.MBeanServerConnection;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.Thread.State;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import static java.lang.management.ManagementFactory.THREAD_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;


public class TopThreadsPanel extends JPanel
{
    public static final String INITIAL_INTERVAL_FLAG = "net.luminis.jmx.topthreads.interval";

    public static final int INITIAL_MAX_THREADS = 100;
    public static final int INITIAL_TAB_COUNT = 5;

    public static final Color TABLE_BACKGROUND_COLOR = Color.white;
    public static final Color TABLE_BACKGROUND_COLOR_PROCESS_ROW = new Color(235, 255, 255); // or 255, 239, 188: looks fine too.

    private static final int COLUMN_SETTRACE = 0;
    private static final int COLUMN_THREADNAME = 1;
    private static final int COLUMN_HISTORY = 2;
    private static final int COLUMN_USAGE = 3;
    private static final int COLUMN_PERCENTAGE = 4;
    private static final int COLUMN_AVG = 5;
    private static final int COLUMN_THREADSTATE = 6;
    private int columnCount = 1 + COLUMN_THREADSTATE;

    // Swing components and related values
    private JLabel threadCountLabel;
    private JLabel threadDisplayedLabel;
    private JLabel threadsRunningLabel;
    private JLabel threadsBlockedLabel;
    private JLabel threadsWaitingLabel;
    private JLabel threadsTimedWaitingLabel;
    private JTable table;
    private ThreadInfoTableModel tableModel;
    private JTextField intervalField;
    private int timerInterval = 15; // Any value will do, just not 0; updated when connected.
    private int maxThreadsDisplayed = INITIAL_MAX_THREADS;
    private JLabel cpuUsage;
    private JToggleButton lockTabButton;
    private boolean debug;

    private ThreadMXBean threadMXBean;

    private Comparator<InfoStats> m_comparator = ThreadInfoStats.lastUsageComparator();
    private boolean fixOrder;

    /** The thread for which a stacktrace is shown (if any). */
    private InfoStats tracedThread;
    /** The poll-for-thread-info timer */
    private Timer timer;
    /** The poll-for-thread-info timer task. */
    private TimerTask timerTask;

    private int maxStackTraceDepth = Integer.MAX_VALUE;
    private int m_maxTabCount = 5;
    private JSplitPane m_splitter;
    private ThreadDataCollector threadDataCollector;
    private volatile boolean showCpuUsageHistory;
    private volatile boolean isShownCpuUsageHistory;
    private volatile boolean isStackTraceShown;
    private Integer lastDividerLocation;
    private JTabbedPane m_tabs;
    private volatile boolean lockTab;
    private boolean programmaticTabSelectionChange;

    private StackTraceElement[] previousStackTrace = new StackTraceElement[0];
    private boolean showStackTraceMatch = true;

    public TopThreadsPanel() {
        super(new BorderLayout());

        debug = TopThreads.debug;

        readPrefs();

        // Upper info panel
        add(createTopPanel(), BorderLayout.NORTH);

        // Thread table
        createThreadTable();
        JScrollPane tableScrollPane = new JScrollPane(table);

        // Create the show-stacktrace area
        m_tabs = createStacktracePanel();

        // And put both on a split-panel
        m_splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, m_tabs);
        m_splitter.setOneTouchExpandable(true);
        m_splitter.setContinuousLayout(false);
        m_splitter.setResizeWeight(1);
        add(m_splitter, BorderLayout.CENTER);

        // Bottom button panel
        add(createButtonBar(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        int strutLarge = 13;
        int strutSmall = 3;
        threadCountLabel = new JLabel(" ");
        threadDisplayedLabel = new JLabel();
        threadsRunningLabel = new JLabel();
        threadsBlockedLabel = new JLabel();
        threadsWaitingLabel = new JLabel();
        threadsTimedWaitingLabel = new JLabel();
        cpuUsage = new JLabel();
        threadCountLabel.setToolTipText("current thread count");
        JPanel countPanel = new JPanel();
        countPanel.setLayout(new BoxLayout(countPanel, BoxLayout.X_AXIS));
        countPanel.add(new JLabel("Total:"));
        countPanel.add(Box.createHorizontalStrut(strutSmall));
        countPanel.add(threadCountLabel);
        countPanel.add(Box.createHorizontalStrut(strutLarge));
        countPanel.add(new JLabel("Shown:"));
        countPanel.add(Box.createHorizontalStrut(strutSmall));
        countPanel.add(threadDisplayedLabel);
        countPanel.add(Box.createHorizontalStrut(strutLarge));
        countPanel.add(new JLabel("Running:"));
        countPanel.add(Box.createHorizontalStrut(strutSmall));
        countPanel.add(threadsRunningLabel);
        countPanel.add(Box.createHorizontalStrut(strutLarge));
        countPanel.add(new JLabel("Blocked:"));
        countPanel.add(Box.createHorizontalStrut(strutSmall));
        countPanel.add(threadsBlockedLabel);
        countPanel.add(Box.createHorizontalStrut(strutLarge));
        countPanel.add(new JLabel("Waiting:"));
        countPanel.add(Box.createHorizontalStrut(strutSmall));
        countPanel.add(threadsWaitingLabel);
        countPanel.add(Box.createHorizontalStrut(strutLarge));
        countPanel.add(new JLabel("Timed waiting:"));
        countPanel.add(Box.createHorizontalStrut(strutSmall));
        countPanel.add(threadsTimedWaitingLabel);
        countPanel.add(Box.createRigidArea(new Dimension(13, 25)));
        countPanel.add(Box.createHorizontalGlue());
        countPanel.add(cpuUsage);
        cpuUsage.setText("Cpu usage:  0 %");
        cpuUsage.setToolTipText("cpu usage of the monitored process");
        return countPanel;
    }

    private void createThreadTable() {
        final int horizontalCellSpacing = 3;
        tableModel = new ThreadInfoTableModel();
        table = new JTable(tableModel)
        {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                // Set different background for "process-row"
                JComponent c = (JComponent) super.prepareRenderer(renderer, row, column);
                c.setBorder(new EmptyBorder(0, horizontalCellSpacing, 0, horizontalCellSpacing));
                if (isShownCpuUsageHistory && row == 0) {
                    c.setBackground(TABLE_BACKGROUND_COLOR_PROCESS_ROW);
                }
                else {
                    c.setBackground(TABLE_BACKGROUND_COLOR);
                    c.setToolTipText("click row to see stacktrace");
                }

                return c;
            }
        };
        table.getColumnModel().getColumn(COLUMN_SETTRACE).setCellEditor(new DefaultCellEditor(new JCheckBox()));
        table.getColumnModel().getColumn(COLUMN_SETTRACE).setPreferredWidth(50);
        table.getColumnModel().getColumn(COLUMN_THREADNAME).setPreferredWidth(300);
        table.getColumnModel().getColumn(COLUMN_PERCENTAGE).setPreferredWidth(30);
        table.getColumnModel().getColumn(COLUMN_HISTORY).setPreferredWidth(120);
        table.getColumnModel().getColumn(COLUMN_HISTORY).setCellRenderer(new ColoredHistoryRenderer());
        table.getColumnModel().getColumn(COLUMN_PERCENTAGE).setCellRenderer(new ColoredIntegerCellRenderer());

        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(false);

        table.setGridColor(Color.LIGHT_GRAY);
        table.setRowHeight(table.getRowHeight() + 5);
        table.setPreferredScrollableViewportSize(new Dimension(850, 510));

        // Click on table row is identical to selecting the trace checkbox
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                if (column != COLUMN_SETTRACE) {
                    int row = table.rowAtPoint(e.getPoint());
                    // Simulate trace checkbox set to true
                    tableModel.setValueAt(Boolean.TRUE, row, COLUMN_SETTRACE);
                }
            }
        });
    }

    private JTabbedPane createStacktracePanel() {
        final JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);
        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                if (!programmaticTabSelectionChange && isStackTraceShown && !lockTab && tabs.getSelectedIndex() < tabs.getTabCount()) {
                    lockTabButton.setSelected(true);
                    lockTab = true;
                }
            }
        });
        return tabs;
    }

    private Component createButtonBar()
    {
    	JPanel buttonPanel = new JPanel(new FlowLayout());
        int strutSmall = 3;
        int strutLarge = 6;

        intervalField = new JTextField(2);
        intervalField.setToolTipText("Refresh interval: each " + timerInterval + " seconds, new thread usage (and stracktrace, if enabled) is retrieved from the application.");
        intervalField.setHorizontalAlignment(JTextField.RIGHT);
        intervalField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {}
			public void focusLost(FocusEvent e) {
				updateTimerInterval();
			}
        });
        buttonPanel.add(new JLabel("refresh:"));
        buttonPanel.add(Box.createHorizontalStrut(strutSmall));
        buttonPanel.add(intervalField);
        buttonPanel.add(Box.createHorizontalStrut(strutLarge));
        buttonPanel.add(Box.createGlue());

        final JToggleButton fixOrderButton = new JToggleButton("fix order");
        fixOrderButton.setToolTipText("Fixes the order of the threads in the table, only 'new' busy threads will pop up.");
        fixOrderButton.setActionCommand("fix");
        fixOrderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("fix")) {
                    fixOrderButton.setActionCommand("unfix");
                    fixOrder(true);
                }
                else {
                    fixOrderButton.setActionCommand("fix");
                    fixOrder(false);
                }
            }
        });
        buttonPanel.add(fixOrderButton);

        buttonPanel.add(Box.createHorizontalStrut(strutLarge));
        buttonPanel.add(Box.createGlue());

        final JButton prefsButton = new JButton("settings...");
        prefsButton.setToolTipText("Displays settings dialog.");
        prefsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPreferencesDialog();
            }
        });
        buttonPanel.add(prefsButton);

        buttonPanel.add(Box.createHorizontalStrut(strutLarge));
        buttonPanel.add(Box.createGlue());

        lockTabButton = new JToggleButton("lock tab");
        lockTabButton.setToolTipText("Keep currently selected stractrace tab visible.");
        lockTabButton.setEnabled(false);
        lockTabButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                lockTab = ! lockTab;
            }
        });
        buttonPanel.add(lockTabButton);

        if (debug) {
            JButton debug = new JButton("debug");
            debug.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    dumpLayoutStatus();
                }
            });
            buttonPanel.add(debug);
        }

        return buttonPanel;
    }

    private void dumpLayoutStatus() {
        Dimension minimumSize = m_tabs.getMinimumSize();
        System.out.println("Tab min: " + minimumSize);
        Dimension currentSize = m_tabs.getSize();
        System.out.println("Tab cur: " + currentSize);
        Dimension maximumSize = m_tabs.getMaximumSize();
        System.out.println("Tab max: " + maximumSize);
    }

    protected void fixOrder(boolean on) {
    	fixOrder = on;
    	if (on)
    	    m_comparator = ThreadInfoStats.fixOrderComparator();
    	else
    		m_comparator = ThreadInfoStats.lastUsageComparator();
	}

    // synchronized in order to synchronize changes of threadMXBean member variable
    public synchronized void connect(MBeanServerConnection serverConnection) {
        try {
            this.threadMXBean = newPlatformMXBeanProxy(serverConnection, THREAD_MXBEAN_NAME, ThreadMXBean.class);
        } catch (Exception e) {
            if (debug)
                System.err.println("Cannot access the Thread MXBean in the target VM: " + e);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(TopThreadsPanel.this, "Cannot access the Thread MXBean in the target VM.",
                            "TopThreadsPanel", JOptionPane.ERROR_MESSAGE);
                }
            });
            return;
        }
        if (!threadMXBean.isThreadCpuTimeSupported()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(TopThreadsPanel.this, "Target VM does not support thread CPU time monitoring",
                            "TopThreadsPanel", JOptionPane.ERROR_MESSAGE);
                }
            });
        } else {
            boolean enabled = threadMXBean.isThreadCpuTimeEnabled();
            try {
                if (!enabled) {
                    threadMXBean.setThreadCpuTimeEnabled(true);
                }
                initPollTime(threadMXBean.getThreadCount());
                initTimer(false);
                threadDataCollector = new ThreadDataCollector(threadMXBean, debug);
            }
            catch (SecurityException se) {
                if (debug)
                    System.err.println("Thread CPU time monitoring cannot be enabled in target VM: " + se);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JOptionPane.showMessageDialog(TopThreadsPanel.this, "Thread CPU time monitoring cannot be enabled in target VM.",
                                "TopThreadsPanel", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        }
    }

    // synchronized in order to synchronize changes of threadMXBean member variable
    public synchronized void disconnect() {
        if (debug)
            System.err.println("JMX connection is reset.");
        if (timer != null)
            timer.cancel();
        threadMXBean = null;
    }

    private void retrieveAndShowStackTrace(final Long threadId)
    {
        if (threadId != null) {
            // Retrieve stacktrace asynchronuously, can take a while...
            new SwingWorker<StackTraceElement[], Object>() {
                @Override
                protected StackTraceElement[] doInBackground() throws Exception {
                    return threadDataCollector.retrieveStackTrace(threadId, maxStackTraceDepth);
                }

                @Override
                protected void done() {
                    try {
                        showStackTrace(get());
                    } catch (InterruptedException e) {
                        // Fairly impossible
                    } catch (ExecutionException e) {
                        // Fairly impossible
                    }
                }
            }.execute();
        }
    }

    class ThreadInfoTableModel extends AbstractTableModel {
        private String[] columnNames = new String[columnCount];

        private List<? extends InfoStats> threadInfo = Collections.emptyList();

        public ThreadInfoTableModel() {
            columnNames[COLUMN_THREADNAME] = "Thread";
            columnNames[COLUMN_HISTORY] = "Usage history";
            columnNames[COLUMN_USAGE] = "Cpu";
            columnNames[COLUMN_PERCENTAGE] = "%";
            columnNames[COLUMN_AVG] = "Average";
            columnNames[COLUMN_THREADSTATE] = "State";
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return threadInfo.size();
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            InfoStats stats = threadInfo.get(row);
            switch (col) {
                case COLUMN_SETTRACE:
                	return stats.getSelect();
                case COLUMN_THREADNAME:
                    return stats.getName();
                case COLUMN_HISTORY:
                	return stats.getHistory();
                case COLUMN_USAGE:
                	long usage = stats.getCpuUsage() / 1000;
                    return new Long(usage);
                case COLUMN_PERCENTAGE:
                	return stats.getPercentage();
                case COLUMN_AVG:
                    return stats.getAverageUsage();
                case COLUMN_THREADSTATE:
                    return stats.getState();
                default:
                    return null;
            }
        }

        @Override
        public Class<? extends Object> getColumnClass(int c) {
            Object value = getValueAt(0, c);
            return value != null? value.getClass(): String.class;
        }

        @Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
        	return columnIndex == COLUMN_SETTRACE;
		}

        @Override
        public void setValueAt(Object newValue, int rowIndex, int columnIndex) {
            if (showCpuUsageHistory && rowIndex == 0) {
                // First row is process cpu usage, nothing to do here.
                return;
            }

        	if (columnIndex == COLUMN_SETTRACE) {
        		if (newValue.equals(Boolean.TRUE)) {
                    // reset previous
        			if (tracedThread != null)
        				tracedThread.setSelect(false);
        			// Notify table that previous selected is now reset (so it can repaint).
                    // Calling fireTableCellUpdated(row, column) would be better than what
                    // do now, but this seems to be fast enough, so why bother...
                    tableModel.fireTableDataChanged();
                    // Show stacktrace immediately
                    retrieveAndShowStackTrace(threadInfo.get(rowIndex).getId());
                    tracedThread = threadInfo.get(rowIndex);
                    tracedThread.setSelect(true);
                    lockTabButton.setEnabled(true);
        		}
        		else {
        		    tracedThread.setSelect(false);
        		    tracedThread = null;
                    lockTabButton.setEnabled(false);
                    showStackTrace(new StackTraceElement[0]);
        		}
        	}
        }

		void setThreadInfo(List<? extends InfoStats> list) {
            threadInfo = list;
        }
    }

    private void showStackTrace(StackTraceElement[] stackTrace) {

        if (tracedThread != null) {
            Date now = new Date();
            String time = "" + (now.getHours() % 12) + ":" + (now.getMinutes() < 10? "0":"") + now.getMinutes() + ":" + (now.getSeconds() < 10? "0":"") + now.getSeconds();
            JTextPane stackTraceArea = new JTextPane();
            stackTraceArea.setEditable(false);

            if (stackTrace.length > 0) {
                String content = "Thread \"" + tracedThread.getName() + "\"";
                content += " at " + time + "\n\n";

                if (showStackTraceMatch && previousStackTrace.length > 0) {
                    StyledDocument document = new DefaultStyledDocument();
                    AttributeSet defaultAttrs = new SimpleAttributeSet();
                    SimpleAttributeSet sameAsBeforeAttrs = new SimpleAttributeSet();
                    sameAsBeforeAttrs.addAttribute(StyleConstants.Foreground, Color.lightGray);

                    Object[] match = new DiffStackTraces(previousStackTrace, stackTrace).diff().largestMatch();
                    int[] range = match != null? (int[]) match[1]: new int[] { -1, -1 };
                    int lineNr = 0;
                    try {
                        document.insertString(0, content, defaultAttrs);
                        for (StackTraceElement el: stackTrace) {
                            if (lineNr >= range[0] && lineNr <= range[1])
                                document.insertString(document.getLength(), el.toString() + "\n", sameAsBeforeAttrs);
                            else {
                                document.insertString(document.getLength(), el.toString() + "\n", defaultAttrs);
                            }
                            lineNr++;
                        }
                        stackTraceArea.setDocument(document);
                    } catch (BadLocationException e) {
                        // Impossible
                    }
                }
                else {
                    for (StackTraceElement el: stackTrace) {
                        content += el.toString() + "\n";
                    }
                    stackTraceArea.setText(content);
                }
            }
            else {
                stackTraceArea.setText("     <no stacktrace available>");
            }

            previousStackTrace = stackTrace;

            final JScrollPane textScrollPane = new JScrollPane(stackTraceArea);
            m_tabs.add(time, textScrollPane);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    textScrollPane.getViewport().setViewPosition(new Point(0,0));
                }
            });

            programmaticTabSelectionChange = true;     // Set flag so we can distinguish user action from program action
            // Remove oldest tabs if limit is exceeded, but never remove the current tab is lock-tab is set.
            // (Number of tabs to remove can be larger than 1 if max-tabs settings is just changed)
            int selected = m_tabs.getSelectedIndex();
            int nextTabToRemove = 0;
            for (int index = 0; m_tabs.getTabCount() > m_maxTabCount; index++) {
                if (lockTab) {
                    if (index != selected) {
                        m_tabs.removeTabAt(nextTabToRemove);
                    }
                    else {
                        nextTabToRemove++;
                    }
                }
                else {
                    m_tabs.removeTabAt(nextTabToRemove);
                }

            }
            if (! lockTab)
                m_tabs.setSelectedIndex(m_tabs.getTabCount() - 1);
            programmaticTabSelectionChange = false;
        }

        if (tracedThread == null) {
            // If switching from filled to empty, remember divider location
            if (isStackTraceShown) {
                isStackTraceShown = false;
                lastDividerLocation = m_splitter.getDividerLocation();
            }
        }
        else {
            // If switching from empty to filled, reset divider location
            // If location greater than max, that means bottom component is invisible.
            if (! isStackTraceShown || m_splitter.getDividerLocation() > m_splitter.getMaximumDividerLocation()) {
                isStackTraceShown = true;
                if (lastDividerLocation != null)
                    m_splitter.setDividerLocation(lastDividerLocation);
                else
                    m_splitter.setDividerLocation(0.5f);
            }
        }
    }

    class RefreshUiWorker extends SwingWorker<Data, Object> {
        @Override
        public Data doInBackground() {
            try {
                Long tracedThreadId = tracedThread != null ? tracedThread.getId() : null;
                Data data = threadDataCollector.getThreadData(maxThreadsDisplayed, showCpuUsageHistory, fixOrder, m_comparator, tracedThreadId, maxStackTraceDepth);

                if (data.error != null && timer != null)
                    timer.cancel();

                return data;
            } catch (RuntimeException e) {
                System.err.println(e.toString());
                throw e;
            }
        }

        @Override
        protected void done() {
            try {
                if (showCpuUsageHistory != isShownCpuUsageHistory) {
                    isShownCpuUsageHistory = showCpuUsageHistory;
                }

                Data data = get();
                if (data.error == null) {
                    tableModel.setThreadInfo(data.threadList);
                    tableModel.fireTableDataChanged();
                    threadCountLabel.setText(String.valueOf(data.threadCount));
                    if (data.threadCount <= maxThreadsDisplayed)
                        threadDisplayedLabel.setText("all");
                    else
                        threadDisplayedLabel.setText(String.valueOf(Math.min(data.threadCount, maxThreadsDisplayed)));
                    showStackTrace(data.stackTrace);

                    // update state stats
                    threadsBlockedLabel.setText("" + data.threadStats.get(State.BLOCKED));
                    threadsRunningLabel.setText("" + data.threadStats.get(State.RUNNABLE));
                    threadsWaitingLabel.setText("" + data.threadStats.get(State.WAITING));
                    threadsTimedWaitingLabel.setText("" + data.threadStats.get(State.TIMED_WAITING));
                    cpuUsage.setText(String.format("Cpu usage: %2d %%", data.cpuUsagePercentage));
                } else {
                    if (data.error instanceof SecurityException) {
                        if (debug)
                            System.err.println("Target VM does not allow retrieving thread CPU time: " + data.error);
                        JOptionPane.showMessageDialog(TopThreadsPanel.this, "Target VM does not allow retrieving thread CPU time.",
                                "TopThreadsPanel", JOptionPane.ERROR_MESSAGE);
                    } else {
                        if (debug)
                            System.err.println("An error occured while retrieving thread CPU time: " + data.error);
                        JOptionPane.showMessageDialog(TopThreadsPanel.this, "An error occured while retrieving thread CPU time.",
                                "TopThreadsPanel", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (ExecutionException e) {
                // Fairly impossible
            } catch (InterruptedException e) {
                // Fairly impossible
            }
        }
    }

    void initTimer(boolean reset)
    {
    	timerTask = new TimerTask() {
            SwingWorker<?, ?> swingWorker;

    		@Override
            public void run() {
                if (swingWorker != null && ! swingWorker.isDone()) {
                    enlargeTimerInterval();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            JOptionPane.showMessageDialog(TopThreadsPanel.this, "Refresh interval is modified, because it was too small.",
                                    "TopThreadsPanel", JOptionPane.WARNING_MESSAGE);
                        }
                    });
                }
                else {
                    swingWorker = new RefreshUiWorker();
                    swingWorker.execute();
                }
    		}
    	};
    	timer = new Timer("Topthreads timer thread");
        int delay = reset? timerInterval: 0;
    	timer.schedule(timerTask, delay * 1000, timerInterval * 1000);
    }

    private void initPollTime(int threadCount) {
        Integer initialSetting = null;
        if (System.getProperty(INITIAL_INTERVAL_FLAG) != null) {
            initialSetting = Integer.getInteger(INITIAL_INTERVAL_FLAG);
        }
        if (initialSetting == null) {
            if (threadCount != 0) {
                timerInterval = threadCount / 10;
                if (timerInterval < 1)
                    timerInterval = 1;
            }
            else {
                // Fairly impossible, but still....
                timerInterval = 1;
            }
        }
        else
            timerInterval = initialSetting;

        intervalField.setText(String.valueOf(timerInterval));
        intervalField.setToolTipText("Refresh interval: each " + timerInterval + " seconds, new thread usage (and stracktrace, if enabled) is retrieved from the application.");
    }

    protected void updateTimerInterval() {
    	try {
    		int newInterval = Integer.parseInt(intervalField.getText());
    		if (newInterval > 0 && newInterval < 999) {
    			timer.cancel();
                timerInterval = newInterval;
                initTimer(true);
                intervalField.setToolTipText("Refresh interval: each " + timerInterval + " seconds, new thread usage (and stracktrace, if enabled) is retrieved from the application.");
    		}
    	}
    	catch (NumberFormatException nfe) {
    		intervalField.setText(String.valueOf(timerInterval));
    	}

	}

    protected void enlargeTimerInterval() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int factor = Math.max(2, 5 - timerInterval);  // 1 -> 4, 2 -> 6, 3 -> 6
                timerInterval *= factor;
                intervalField.setText(String.valueOf(timerInterval));
                updateTimerInterval();
            }
        });
    }

    public void dispose()
    {
        ThreadDataCollector dataCollector = threadDataCollector;
        if (dataCollector != null)
            dataCollector.dispose();
    }

    private void showPreferencesDialog() {
        // Find owner of this panel
        Container parent = TopThreadsPanel.this.getParent();
        while (parent != null && !(parent instanceof JFrame)) {
            parent = parent.getParent();
        }
        // Init panel and show it.
        PreferencesPanel prefsPane = new PreferencesPanel((JFrame) parent);
        prefsPane.setMaxStackTraceSize(maxStackTraceDepth);
        prefsPane.setShowCpuUsageHistory(showCpuUsageHistory);
        prefsPane.setMaxNrThreadsDisplayed(maxThreadsDisplayed);
        prefsPane.setMaxStacktraceTabs(m_maxTabCount);
        prefsPane.setVisible(true);
        // If it gets here, the dialog is closed.
        maxStackTraceDepth = prefsPane.getMaxStackTraceSize();
        maxThreadsDisplayed = prefsPane.getMaxNrThreadsDisplayed();
        showCpuUsageHistory = prefsPane.getShowCpuUsageHistory();
        if (prefsPane.getMaxStacktraceTabs() != null)
            m_maxTabCount = prefsPane.getMaxStacktraceTabs();
        else
            m_maxTabCount = INITIAL_TAB_COUNT;
        boolean store = prefsPane.getStoreAsPrefs();
        prefsPane.dispose();
        if (store)
            storePrefs();
    }

    private void storePrefs()
    {
        try {
            Preferences userNode = Preferences.userNodeForPackage(this.getClass());
            userNode.putInt("maxStackTraceDepth", maxStackTraceDepth);
            userNode.putBoolean("showCpuUsageHistory", showCpuUsageHistory);
            userNode.putInt("maxThreadsDisplayed", maxThreadsDisplayed);
            userNode.putInt("maxStacktraceTabCount", m_maxTabCount);
        } catch (Exception error) {
            if (debug)
                System.err.println("Storing preferences failed: " + error);
        }
    }

    private void readPrefs()
    {
        try {
            Preferences userNode = Preferences.userNodeForPackage(this.getClass());
            maxStackTraceDepth = userNode.getInt("maxStackTraceDepth", 100);
            showCpuUsageHistory = userNode.getBoolean("showCpuUsageHistory", false);
            isShownCpuUsageHistory = showCpuUsageHistory;
            maxThreadsDisplayed = userNode.getInt("maxThreadsDisplayed", INITIAL_MAX_THREADS);
            m_maxTabCount = userNode.getInt("maxStacktraceTabCount", INITIAL_TAB_COUNT);
        } catch (Exception error) {
            if (debug)
                System.err.println("Reading preferences failed: " + error);
        }
    }

    public static void removePrefs()
    {
        try {
            Preferences userNode = Preferences.userNodeForPackage(TopThreadsPanel.class);
            userNode.removeNode();
        } catch (Exception error) {
            System.err.println("Reading preferences failed: " + error);
        }
    }
}

