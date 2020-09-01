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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PreferencesPanel extends JDialog
{
    private int maxStackTraceSize;
    private JTextField stackTraceSizeField;
    private Frame parent;
    private JCheckBox showCpuUsageHistory;
    private JCheckBox storePrefs;
    private JTextField maxNrThreadsDisplayedField;
    private JTextField stackTraceTabsField;
    private int maxNrThreads;

    public PreferencesPanel(Frame frame) {
        super(frame, "TopThreads - Preferences", true);
        this.parent = frame;

        final JPanel settingsPanel = new JPanel();
        settingsPanel.setBorder(new LineBorder(Color.BLACK));
        Border raisedbevel = BorderFactory.createRaisedBevelBorder();
        Border loweredbevel = BorderFactory.createLoweredBevelBorder();
        settingsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        BorderFactory.createCompoundBorder(raisedbevel,
                                BorderFactory.createCompoundBorder(loweredbevel,
                                        BorderFactory.createEmptyBorder(5, 5, 5, 5)))));

        settingsPanel.setLayout(new GridLayout(0, 2, 0, 0));

        // Stacktrace size
        JLabel label = new JLabel(" Max stack trace size:   ");
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        settingsPanel.add(label);
        stackTraceSizeField = new JTextField();
        int height = stackTraceSizeField.getPreferredSize().height;
        stackTraceSizeField.setPreferredSize(new Dimension(100, height));
        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        //panel1.setBackground(Color.yellow);
        panel1.add(stackTraceSizeField);
        settingsPanel.add(panel1);

        maxNrThreadsDisplayedField = new JTextField();
        maxNrThreadsDisplayedField.setPreferredSize(new Dimension(100, height));
        settingsPanel.add(new JLabel("  Max number of threads displayed:"));
        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        panel2.add(maxNrThreadsDisplayedField);
        settingsPanel.add(panel2);

        // Max number of stacktrace tabs
        JLabel label3 = new JLabel("Max number of stacktrace tabs:");
        label3.setHorizontalAlignment(SwingConstants.RIGHT);
        settingsPanel.add(label3);
        stackTraceTabsField = new JTextField();
        stackTraceTabsField.setPreferredSize(new Dimension(100, height));
        JPanel panel5 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        panel5.add(stackTraceTabsField);
        settingsPanel.add(panel5);

        // Show cpu (process) usage too
        showCpuUsageHistory = new JCheckBox();
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        checkboxPanel.add(showCpuUsageHistory);
        settingsPanel.add(checkboxPanel);
        settingsPanel.add(new JLabel("Show process cpu usage too (top row)"));

        Container contentPane = getContentPane();
        contentPane.add(settingsPanel, BorderLayout.CENTER);

        // Control panel: buttons, store-as-default checkbox
        JPanel controlPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(2, 1);
        controlPanel.setLayout(gridLayout);
        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        storePrefs = new JCheckBox("store these settings as default");
        storePrefs.setSelected(true);
        flowPanel.add(storePrefs);
        controlPanel.add(flowPanel);

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);

        controlPanel.add(buttonPanel);
        contentPane.add(controlPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);

        pack();
        this.setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void close() {
        setVisible(false);
    }

    public int getMaxStackTraceSize() {
        try {
            return Integer.parseInt(stackTraceSizeField.getText());
        } catch (NumberFormatException nfe) {
            // What the hack, return previous value
            return maxStackTraceSize;
        }
    }

    public void setMaxStackTraceSize(int maxStrackTraceSize) {
        this.maxStackTraceSize = maxStrackTraceSize;
        stackTraceSizeField.setText("" + maxStrackTraceSize);
    }

    public int getMaxNrThreadsDisplayed() {
        try {
            return Integer.parseInt(maxNrThreadsDisplayedField.getText());
        } catch (NumberFormatException nfe) {
            // What the hack, return previous value
            return maxNrThreads;
        }
    }

    public void setMaxNrThreadsDisplayed(int value) {
        maxNrThreadsDisplayedField.setText("" + value);
        maxNrThreads = value;
    }

    public boolean getShowCpuUsageHistory() {
        return showCpuUsageHistory.isSelected();
    }

    public void setShowCpuUsageHistory(boolean showCpuUsageHistory) {
        this.showCpuUsageHistory.setSelected(showCpuUsageHistory);
    }

    public boolean getStoreAsPrefs() {
        return storePrefs.isSelected();
    }

    public Integer getMaxStacktraceTabs() {
        try {
            return Integer.parseInt(stackTraceTabsField.getText());
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    public void setMaxStacktraceTabs(Integer value) {
        if (value != null)
            stackTraceTabsField.setText(value.toString());
    }

    public static void main(String[] args) {
        PreferencesPanel preferencesPanel = new PreferencesPanel(null);
        preferencesPanel.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        preferencesPanel.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        preferencesPanel.setVisible(true);
    }
}
