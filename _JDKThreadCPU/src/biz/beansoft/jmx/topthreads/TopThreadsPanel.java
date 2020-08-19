package biz.beansoft.jmx.topthreads;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import javax.management.MBeanServerConnection;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.jdesktop.swingworker.SwingWorker;

public class TopThreadsPanel extends JPanel
{
  public static final String DEBUG_FLAG = "biz.beansoft.jmx.topthreads.debug";
  public static final String PRIORITY_FLAG = "biz.beansoft.jmx.topthreads.priorities";
  public static final int INITIAL_POLL_INTERVAL = 10;
  public static final int INITIAL_MAX_THREADS = 100;
  private static final int COLUMN_SETTRACE = 0;
  private static final int COLUMN_THREADNAME = 1;
  private static final int COLUMN_HISTORY = 2;
  private static final int COLUMN_USAGE = 3;
  private static final int COLUMN_PERCENTAGE = 4;
  private static final int COLUMN_AVG = 5;
  private static final int COLUMN_THREADSTATE = 6;
  private static final int COLUMN_PRIORITY = 7;
  private int columnCount = 7;
  private JLabel threadCountLabel;
  private int threadCount;
  private JLabel threadsRunningLabel;
  private int threadsRunning;
  private JLabel threadsBlockedLabel;
  private int threadsBlocked;
  private JLabel threadsWaitingLabel;
  private int threadsWaiting;
  private JLabel threadsTimedWaitingLabel;
  private int threadsTimedWaiting;
  private JTable table;
  private ThreadInfoTableModel tableModel;
  private JTextArea stackTraceArea;
  private JTextField intervalField;
  private int timerInterval = 10;
  private JTextField maxThreadsField;
  private int maxThreadsDisplayed = 100;
  private boolean debug;
  private boolean includeThreadPriorities;
  private MBeanServerConnection server;
  private ThreadMXBean threadMXBean;
  private Comparator<ThreadInfoStats> m_comparator = ThreadInfoStats.lastUsageComparator();
  private boolean fixOrder;
  private Map<Long, ThreadInfoStats> threadData = new HashMap();
  private int updateCount;
  private ThreadInfoStats tracedThread;
  private String latestStackTrace = "";
  private Timer timer;
  private TimerTask timerTask;
  private int maxStackTraceDepth = 100;
  private JSplitPane m_splitter;

  public TopThreadsPanel()
  {
    super(new BorderLayout());

	 // System.out.println("欢迎使用 www.beansoft.biz 出品的JVM线程监控小工具\r\n更多中间件监控及自动运维软件请关注WebLogic中文博客官方网站");

    if ((System.getProperty("biz.beansoft.jmx.topthreads.debug") != null) && (System.getProperty("biz.beansoft.jmx.topthreads.debug").equals("true")))
      this.debug = true;
    add(createTopPanel(), "North");
    createTable();
    JScrollPane localJScrollPane1 = new JScrollPane(this.table);
    this.stackTraceArea = new JTextArea();
    this.stackTraceArea.setEditable(false);
    this.stackTraceArea.setMinimumSize(new Dimension(0, 30));
    JScrollPane localJScrollPane2 = new JScrollPane(this.stackTraceArea);
    this.m_splitter = new JSplitPane(0, localJScrollPane1, localJScrollPane2);
    this.m_splitter.setOneTouchExpandable(true);
    this.m_splitter.setContinuousLayout(false);
    this.m_splitter.setDividerLocation(0.300000011920929D);
    this.m_splitter.setResizeWeight(1.0D);
    add(this.m_splitter, "Center");
    add(createBottomPanel(), "South");
  }

  private JPanel createTopPanel()
  {
    int i = 13;
    int j = 3;
    this.threadCountLabel = new JLabel(" ");
    this.threadsRunningLabel = new JLabel();
    this.threadsBlockedLabel = new JLabel();
    this.threadsWaitingLabel = new JLabel();
    this.threadsTimedWaitingLabel = new JLabel();
    this.threadCountLabel.setToolTipText("当前线程数");
    JPanel localJPanel = new JPanel(new FlowLayout(0, 0, 5));
    localJPanel.add(new JLabel("线程数:"));
    localJPanel.add(Box.createHorizontalStrut(j));
    localJPanel.add(this.threadCountLabel);
    localJPanel.add(Box.createHorizontalStrut(i));
    localJPanel.add(new JLabel("运行:"));
    localJPanel.add(Box.createHorizontalStrut(j));
    localJPanel.add(this.threadsRunningLabel);
    localJPanel.add(Box.createHorizontalStrut(i));
    localJPanel.add(new JLabel("阻塞:"));
    localJPanel.add(Box.createHorizontalStrut(j));
    localJPanel.add(this.threadsBlockedLabel);
    localJPanel.add(Box.createHorizontalStrut(i));
    localJPanel.add(new JLabel("等待:"));
    localJPanel.add(Box.createHorizontalStrut(j));
    localJPanel.add(this.threadsWaitingLabel);
    localJPanel.add(Box.createHorizontalStrut(i));
    localJPanel.add(new JLabel("定时等待:"));
    localJPanel.add(Box.createHorizontalStrut(j));
    localJPanel.add(this.threadsTimedWaitingLabel);

    JPanel topPane = new JPanel();
    topPane.setLayout(new BorderLayout());
    topPane.add(new JLabel("线程诊断工具 by github.com/beansoft"), BorderLayout.NORTH);
    topPane.add(localJPanel, BorderLayout.CENTER);
    return topPane;
  }

  private Component createTopPanel2()
  {
    this.threadCountLabel = new JLabel(" ");
    this.threadCountLabel.setToolTipText("current thread count");
    this.threadsRunningLabel = new JLabel();
    this.threadsBlockedLabel = new JLabel();
    this.threadsWaitingLabel = new JLabel();
    this.threadsTimedWaitingLabel = new JLabel();
    Box localBox = new Box(0);
    int i = 3;
    int j = 6;
    localBox.add(new JLabel("Count:"));
    localBox.add(Box.createHorizontalStrut(i));
    localBox.add(this.threadCountLabel);
    localBox.add(Box.createHorizontalStrut(j));
    localBox.add(Box.createGlue());
    localBox.add(new JLabel("Running:"));
    localBox.add(Box.createHorizontalStrut(i));
    localBox.add(this.threadsRunningLabel);
    localBox.add(Box.createHorizontalStrut(j));
    localBox.add(Box.createGlue());
    localBox.add(new JLabel("Blocked:"));
    localBox.add(Box.createHorizontalStrut(i));
    localBox.add(this.threadsBlockedLabel);
    localBox.add(Box.createHorizontalStrut(j));
    localBox.add(Box.createGlue());
    localBox.add(new JLabel("Waiting:"));
    localBox.add(Box.createHorizontalStrut(i));
    localBox.add(this.threadsWaitingLabel);
    localBox.add(Box.createHorizontalStrut(j));
    localBox.add(Box.createGlue());
    localBox.add(new JLabel("TimedWaiting:"));
    localBox.add(Box.createHorizontalStrut(i));
    localBox.add(this.threadsTimedWaitingLabel);
    return localBox;
  }

  private void createTable()
  {
    this.tableModel = new ThreadInfoTableModel();
    this.table = new JTable(this.tableModel);
    this.table.setPreferredScrollableViewportSize(new Dimension(500, 300));
    this.table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JCheckBox()));
    this.table.getColumnModel().getColumn(0).setPreferredWidth(50);
    this.table.getColumnModel().getColumn(1).setPreferredWidth(300);
    this.table.getColumnModel().getColumn(4).setPreferredWidth(30);
    this.table.getColumnModel().getColumn(2).setPreferredWidth(120);
    this.table.setIntercellSpacing(new Dimension(6, 3));
    this.table.setRowHeight(this.table.getRowHeight() + 4);
    this.table.setRowSelectionAllowed(false);
    this.table.setCellSelectionEnabled(false);
    this.table.setGridColor(Color.LIGHT_GRAY);
    this.table.getColumnModel().getColumn(2).setCellRenderer(new UsageHistoryRenderer());
    this.table.getColumnModel().getColumn(4).setCellRenderer(new CurrentValueRenderer());
    this.table.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent paramAnonymousMouseEvent)
      {
        int i = TopThreadsPanel.this.table.columnAtPoint(paramAnonymousMouseEvent.getPoint());
        if (i != 0)
        {
          int j = TopThreadsPanel.this.table.rowAtPoint(paramAnonymousMouseEvent.getPoint());
          TopThreadsPanel.this.tableModel.setValueAt(Boolean.TRUE, j, 0);
        }
      }
    });
  }

  private Component createBottomPanel()
  {
    JPanel localJPanel = new JPanel(new FlowLayout());
    int i = 3;
    int j = 6;
    this.intervalField = new JTextField(2);
    this.intervalField.setHorizontalAlignment(4);
    this.intervalField.setText(String.valueOf(10));
    this.intervalField.addFocusListener(new FocusListener()
    {
      public void focusGained(FocusEvent paramAnonymousFocusEvent)
      {
      }

      public void focusLost(FocusEvent paramAnonymousFocusEvent)
      {
        TopThreadsPanel.this.updateTimerInterval();
      }
    });
    localJPanel.add(new JLabel("刷新频率:"));
    localJPanel.add(Box.createHorizontalStrut(i));
    localJPanel.add(this.intervalField);
    localJPanel.add(Box.createHorizontalStrut(j));
    localJPanel.add(Box.createGlue());
    this.maxThreadsField = new JTextField(3);
    this.maxThreadsField.setHorizontalAlignment(4);
    this.maxThreadsField.setText(String.valueOf(100));
    this.maxThreadsField.addFocusListener(new FocusListener()
    {
      public void focusGained(FocusEvent paramAnonymousFocusEvent)
      {
      }

      public void focusLost(FocusEvent paramAnonymousFocusEvent)
      {
        TopThreadsPanel.this.updateMaxDisplayed();
      }
    });
    localJPanel.add(new JLabel("最大线程显示列表数:"));
    localJPanel.add(Box.createHorizontalStrut(i));
    localJPanel.add(this.maxThreadsField);
    localJPanel.add(Box.createHorizontalStrut(j));
    localJPanel.add(Box.createGlue());
    final JToggleButton localJToggleButton = new JToggleButton("固定顺序");
    localJToggleButton.setActionCommand("fix");
    localJToggleButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramAnonymousActionEvent)
      {
        if (paramAnonymousActionEvent.getActionCommand().equals("fix"))
        {
          localJToggleButton.setActionCommand("unfix");
          TopThreadsPanel.this.fixOrder(true);
        }
        else
        {
          localJToggleButton.setActionCommand("fix");
          TopThreadsPanel.this.fixOrder(false);
        }
      }
    });
    localJPanel.add(localJToggleButton);
    return localJPanel;
  }

  protected void fixOrder(boolean paramBoolean)
  {
    this.fixOrder = paramBoolean;
    if (paramBoolean)
      this.m_comparator = ThreadInfoStats.fixOrderComparator();
    else
      this.m_comparator = ThreadInfoStats.lastUsageComparator();
  }

  public synchronized void setMBeanServerConnection(MBeanServerConnection paramMBeanServerConnection)
  {
    this.server = paramMBeanServerConnection;
    if (paramMBeanServerConnection != null)
    {
      try
      {
        this.threadMXBean = ((ThreadMXBean)ManagementFactory.newPlatformMXBeanProxy(this.server, "java.lang:type=Threading", ThreadMXBean.class));
      }
      catch (IOException localIOException)
      {
        if (this.debug)
          System.err.println("Retrieving Thread MXBean failed: " + localIOException);
      }
      if (!this.threadMXBean.isThreadCpuTimeSupported())
        System.err.println("This VM does not support thread CPU time monitoring");
      else
        this.threadMXBean.setThreadCpuTimeEnabled(true);
      initTimer(false);
    }
    else
    {
      if (this.debug)
        System.err.println("JMX connection is reset.");
      this.timer.cancel();
      this.threadMXBean = null;
    }
  }

  private void showStackTrace(final ThreadInfoStats paramThreadInfoStats)
  {
    if (paramThreadInfoStats != null)
      new SwingWorker()
      {
        protected Object doInBackground()
          throws Exception
        {
          return TopThreadsPanel.this.retrieveStackTrace(paramThreadInfoStats);
        }

        protected void done()
        {
          try
          {
            TopThreadsPanel.this.stackTraceArea.setText((String)get());
            TopThreadsPanel.this.stackTraceArea.setCaretPosition(0);
          }
          catch (InterruptedException localInterruptedException)
          {
          }
          catch (ExecutionException localExecutionException)
          {
          }
        }
      }
      .execute();
  }

  private synchronized List<ThreadInfoStats> getThreadList()
  {
    if (this.threadMXBean == null)
      return Collections.emptyList();
    this.updateCount += 1;
    long[] allThreadIds = null;
    ThreadInfo[] allThreadInfos = null;
    try
    {
      allThreadIds = this.threadMXBean.getAllThreadIds();
      allThreadInfos = this.threadMXBean.getThreadInfo(allThreadIds);
    }
    catch (UndeclaredThrowableException localUndeclaredThrowableException)
    {
      if (this.debug)
        System.err.println("error while getting thread info: " + localUndeclaredThrowableException + " caused by " + localUndeclaredThrowableException.getCause());
      return Collections.emptyList();
    }
    catch (Exception localException)
    {
      if (this.debug)
        System.err.println("error while getting thread info: " + localException);
      return Collections.emptyList();
    }
    Map threadPrioritiesMap = null;
    if (this.includeThreadPriorities)
      threadPrioritiesMap = determineThreadPriorities();
    long totalProcessCpuUsage = 0L;
    for (int i = 0; i < allThreadIds.length; i++)
    {
      assert (allThreadInfos[i].getThreadId() == allThreadIds[i]);
      long threadID = allThreadIds[i];
      if (allThreadInfos[i] != null)
      {
        long threadCpuTime = this.threadMXBean.getThreadCpuTime(threadID);
        if (threadCpuTime != -1L)
        {
          ThreadInfoStats _threadInfoStats = (ThreadInfoStats)this.threadData.get(Long.valueOf(threadID));
          if (_threadInfoStats == null)
          {
            _threadInfoStats = new ThreadInfoStats(threadID, allThreadInfos[i], threadCpuTime);
            this.threadData.put(Long.valueOf(threadID), _threadInfoStats);
          }
          else
          {
            totalProcessCpuUsage += _threadInfoStats.update(allThreadInfos[i], threadCpuTime);
          }
          if (this.includeThreadPriorities)
          {
            Integer threadPriorit = (Integer)threadPrioritiesMap.get(Long.valueOf(threadID));
            if (threadPriorit != null)
              _threadInfoStats.setThreadPriority(threadPriorit);
            else
              System.err.println("no prio for thread " + threadID);
          }
        }
      }
    }
    resetStateCounts();
    this.latestStackTrace = "";
    ArrayList localArrayList = new ArrayList(this.threadData.values());
    Iterator localIterator = localArrayList.iterator();
    Object threadInfoStats;
    while (localIterator.hasNext())
    {
      threadInfoStats = (ThreadInfoStats)localIterator.next();
      if ((!((ThreadInfoStats)threadInfoStats).checkUpdate(this.updateCount)) && (this.threadMXBean.getThreadInfo(((ThreadInfoStats)threadInfoStats).getThreadId()) == null))
        this.threadData.remove(Long.valueOf(((ThreadInfoStats)threadInfoStats).getThreadId()));
      ((ThreadInfoStats)threadInfoStats).computePercentage(totalProcessCpuUsage);
      updateStateCounts(((ThreadInfoStats)threadInfoStats).getThreadState());
      if (((ThreadInfoStats)threadInfoStats).mustShowTrace())
        this.latestStackTrace = retrieveStackTrace((ThreadInfoStats)threadInfoStats);
    }
    Collections.sort(localArrayList, this.m_comparator);
    if (!this.fixOrder)
    {
      int j = 1;
      threadInfoStats = localArrayList.iterator();
      while (((Iterator)threadInfoStats).hasNext())
      {
        ThreadInfoStats localThreadInfoStats1 = (ThreadInfoStats)((Iterator)threadInfoStats).next();
        localThreadInfoStats1.setIndex(j++);
      }
    }
    this.threadCount = localArrayList.size();
    return localArrayList.subList(0, Math.min(this.threadCount, this.maxThreadsDisplayed));
  }

  private Map<Long, Integer> determineThreadPriorities()
  {
    return Collections.emptyMap();
  }

  private String retrieveStackTrace(ThreadInfoStats paramThreadInfoStats)
  {
    ThreadInfo localThreadInfo = this.threadMXBean.getThreadInfo(paramThreadInfoStats.getThreadId(), this.maxStackTraceDepth);
    if (localThreadInfo != null)
    {
      StackTraceElement[] stackTraces = localThreadInfo.getStackTrace();
      StringBuffer localStringBuffer = new StringBuffer();
      for (StackTraceElement stackTraceElement : stackTraces)
      {
        localStringBuffer.append(stackTraceElement.toString());
        localStringBuffer.append("\n");
      }
      return localStringBuffer.toString();
    }
    return "";
  }

  /**
   * 重置状态计数器.
   */
  private void resetStateCounts()
  {
    this.threadsBlocked = 0;
    this.threadsRunning = 0;
    this.threadsTimedWaiting = 0;
    this.threadsWaiting = 0;
  }

  private void updateStateCounts(Thread.State threadState)
  {
	  switch (threadState) {
      case BLOCKED:
    	  this.threadsBlocked += 1;
    	  break;
      case RUNNABLE:
    	  this.threadsRunning += 1;
    	  break;
      case TIMED_WAITING:
    	  this.threadsTimedWaiting += 1;
    	  break;
      case WAITING:
    	  this.threadsWaiting += 1;
    	  break;
  }

	  // 反编译后代码, 导致running统计为0
//    switch (threadState.ordinal())
//    {
//    case 1:
//      this.threadsBlocked += 1;
//      break;
//    case 2:
//      this.threadsRunning += 1;
//      break;
//    case 3:
//      this.threadsTimedWaiting += 1;
//      break;
//    case 4:
//      this.threadsWaiting += 1;
//    }
	  // BeanSoft 添加的
//	  if(threadState.equals(Thread.State.RUNNABLE)) {
//		  this.threadsRunning += 1;
//	  } else  if(threadState.equals(Thread.State.BLOCKED)) {
//		  this.threadsBlocked += 1;
//	  } else  if(threadState.equals(Thread.State.TIMED_WAITING)) {
//		  this.threadsTimedWaiting += 1;
//	  } else  if(threadState.equals(Thread.State.WAITING)) {
//		  this.threadsWaiting += 1;
//	  }
  }

  void initTimer(boolean paramBoolean)
  {
    this.timerTask = new TimerTask()
    {
      SwingWorker<?, ?> swingWorker;

      public void run()
      {
        if ((this.swingWorker != null) && (!this.swingWorker.isDone()))
        {
          TopThreadsPanel.this.enlargeTimerInterval();
          SwingUtilities.invokeLater(new Runnable()
          {
            public void run()
            {
              JOptionPane.showMessageDialog(TopThreadsPanel.this, "Refresh interval is modified, because it was too small.", "TopThreadsPanel", 2);
            }
          });
        }
        else
        {
          this.swingWorker = new RefreshUiWorker();
          this.swingWorker.execute();
        }
      }
    };
    this.timer = new Timer("Topthreads timer thread");
    int i = paramBoolean ? this.timerInterval : 0;
    this.timer.schedule(this.timerTask, i * 1000, this.timerInterval * 1000);
  }

  protected void updateTimerInterval()
  {
    try
    {
      int i = Integer.parseInt(this.intervalField.getText());
      if ((i > 0) && (i < 999))
      {
        this.timer.cancel();
        this.timerInterval = i;
        initTimer(true);
      }
    }
    catch (NumberFormatException localNumberFormatException)
    {
      this.intervalField.setText(String.valueOf(this.timerInterval));
    }
  }

  protected void enlargeTimerInterval()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        int i = Math.max(2, 5 - TopThreadsPanel.this.timerInterval);
//        TopThreadsPanel.access$1928(TopThreadsPanel.this, i);
        TopThreadsPanel.this.timerInterval = i;
        TopThreadsPanel.this.intervalField.setText(String.valueOf(TopThreadsPanel.this.timerInterval));
        TopThreadsPanel.this.updateTimerInterval();
      }
    });
  }

  protected void updateMaxDisplayed()
  {
    try
    {
      int i = Integer.parseInt(this.maxThreadsField.getText());
      if (i >= 1)
        this.maxThreadsDisplayed = i;
      else
        this.maxThreadsField.setText(String.valueOf(this.maxThreadsDisplayed));
    }
    catch (NumberFormatException localNumberFormatException)
    {
      this.maxThreadsField.setText(String.valueOf(this.maxThreadsDisplayed));
    }
  }

  class RefreshUiWorker extends SwingWorker<List<ThreadInfoStats>, Object>
  {
    RefreshUiWorker()
    {
    }

    public List<ThreadInfoStats> doInBackground()
    {
      return TopThreadsPanel.this.getThreadList();
    }

    protected void done()
    {
      try
      {
        List localList = (List)get();
        TopThreadsPanel.this.tableModel.setThreadList(localList);
        TopThreadsPanel.this.tableModel.fireTableDataChanged();
        TopThreadsPanel.this.threadCountLabel.setText(String.valueOf(TopThreadsPanel.this.threadCount));
        TopThreadsPanel.this.stackTraceArea.setText(TopThreadsPanel.this.latestStackTrace);
        TopThreadsPanel.this.threadsBlockedLabel.setText("" + TopThreadsPanel.this.threadsBlocked);
        TopThreadsPanel.this.threadsRunningLabel.setText("" + TopThreadsPanel.this.threadsRunning);
        TopThreadsPanel.this.threadsWaitingLabel.setText("" + TopThreadsPanel.this.threadsWaiting);
        TopThreadsPanel.this.threadsTimedWaitingLabel.setText("" + TopThreadsPanel.this.threadsTimedWaiting);
      }
      catch (InterruptedException localInterruptedException)
      {
      }
      catch (ExecutionException localExecutionException)
      {
      }
    }
  }

  public class UsageHistoryRenderer extends TopThreadsPanel.CustomTableCellRenderer
    implements TableCellRenderer
  {
    public UsageHistoryRenderer()
    {
      super();
    }

    protected void paintComponent(Graphics g)
	{
		int ai[] = (int[])(int[])table.getValueAt(row, column);
		int i = 0;
		int ai1[] = ai;
		int k = ai1.length;
		for (int l = 0; l < k; l++)
		{
			int i1 = ai1[l];
			i += i1;
		}

		int j = getHeight();
		k = getWidth();
		byte byte0 = 25;
		int j1 = ai.length - 1;
		do
		{
			if (j1 < 0)
				break;
			Color color = determineColor(ai[j1]);
			int k1 = k - byte0;
			byte byte1 = byte0;
			k -= byte0;
			float f = (float)ai[j1] / 100F;
			int l1 = Math.round(f * (float)j);
			int i2 = j - l1;
			int j2 = j;
			g.setColor(color);
			g.fillRect(k1, i2, byte1, j2);
			if (i > 0)
			{
				g.setColor(Color.LIGHT_GRAY);
				g.drawString((new StringBuilder()).append("").append(ai[j1]).toString(), k1 + 3, j2 - 3);
			}
			if (k1 <= 0)
				break;
			j1--;
		} while (true);
	}

  }

  public class CurrentValueRenderer extends TopThreadsPanel.CustomTableCellRenderer
    implements TableCellRenderer
  {
    public CurrentValueRenderer()
    {
      super();
    }

    protected void paintComponent(Graphics paramGraphics)
    {
      int i = ((Integer)this.table.getValueAt(this.row, this.column)).intValue();
      Color localColor = determineColor(i);
      paramGraphics.setColor(localColor);
      int j = getWidth();
      int k = getHeight();
      int m = Math.round(i * k / 100);
      paramGraphics.fillRect(0, k - m, j, k);
      paramGraphics.setColor(Color.BLACK);
      paramGraphics.drawString("" + i, 3, k - 3);
    }
  }

  public abstract class CustomTableCellRenderer extends JComponent
  {
    protected int row;
    protected int column;
    protected JTable table;

    public CustomTableCellRenderer()
    {
    }

    public Component getTableCellRendererComponent(JTable paramJTable, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, int paramInt1, int paramInt2)
    {
      this.table = paramJTable;
      this.row = paramInt1;
      this.column = paramInt2;
      return this;
    }

    protected Color determineColor(int paramInt)
    {
      if (paramInt < 0)
        paramInt = 0;
      if (paramInt > 100)
        paramInt = 100;
      int i;
      int j;
      if (paramInt < 50)
      {
        i = (int)(5.1D * paramInt);
        j = 255;
      }
      else
      {
        i = 255;
        j = 255 - (int)((paramInt - 50) * 5.1D);
      }
      int k = 0;
      return new Color(i, j, k);
    }
  }

  class ThreadInfoTableModel extends AbstractTableModel
  {
    private String[] columnNames = new String[TopThreadsPanel.this.columnCount];
    private List<ThreadInfoStats> threadList = Collections.EMPTY_LIST;

    public ThreadInfoTableModel()
    {
      this.columnNames[1] = "线程名称";
      this.columnNames[2] = "历史趋势";
      this.columnNames[3] = "CPU执行时间";
      this.columnNames[4] = "%";
      this.columnNames[5] = "平均值";
      this.columnNames[6] = "状态";
      if (TopThreadsPanel.this.columnCount > 7)
        this.columnNames[7] = "优先级";
    }

    public int getColumnCount()
    {
      return this.columnNames.length;
    }

    public int getRowCount()
    {
      return this.threadList.size();
    }

    public String getColumnName(int paramInt)
    {
      return this.columnNames[paramInt];
    }

    public Object getValueAt(int paramInt1, int paramInt2)
    {
      ThreadInfoStats localThreadInfoStats = (ThreadInfoStats)this.threadList.get(paramInt1);
      switch (paramInt2)
      {
      case 0:
        return Boolean.valueOf(localThreadInfoStats.getShowTrace());
      case 1:
        return localThreadInfoStats.getThreadName();
      case 2:
        return localThreadInfoStats.getHistory();
      case 3:
        long l = localThreadInfoStats.getCpuUsage() / 1000L;
        return new Long(l);
      case 4:
        return Integer.valueOf(localThreadInfoStats.getPercentage());
      case 5:
        return Integer.valueOf(localThreadInfoStats.getAverageUsage());
      case 6:
        return localThreadInfoStats.getThreadState();
      case 7:
        Integer localInteger = localThreadInfoStats.getThreadPriority();
        if (localInteger != null)
          return localInteger.toString();
        return "";
      }
      return null;
    }

    public Class getColumnClass(int paramInt)
    {
      return getValueAt(0, paramInt).getClass();
    }

    public boolean isCellEditable(int paramInt1, int paramInt2)
    {
      return paramInt2 == 0;
    }

    public void setValueAt(Object paramObject, int paramInt1, int paramInt2)
    {
      if (paramInt2 == 0)
      {
        if (paramObject.equals(Boolean.TRUE))
        {
          if (TopThreadsPanel.this.tracedThread != null)
            TopThreadsPanel.this.tracedThread.showTrace(false);
          TopThreadsPanel.this.tableModel.fireTableDataChanged();
          TopThreadsPanel.this.showStackTrace((ThreadInfoStats)this.threadList.get(paramInt1));
        }
        TopThreadsPanel.this.tracedThread = ((ThreadInfoStats)this.threadList.get(paramInt1));
        TopThreadsPanel.this.tracedThread.showTrace(((Boolean)paramObject).booleanValue());
      }
    }

    void setThreadList(List<ThreadInfoStats> paramList)
    {
      this.threadList = paramList;
    }
  }
}

/* Location:           C:\Users\BeanSoft\Downloads\topthreads.jar
 * Qualified Name:     biz.beansoft.jmx.topthreads.TopThreadsPanel
 * JD-Core Version:    0.6.2
 */
