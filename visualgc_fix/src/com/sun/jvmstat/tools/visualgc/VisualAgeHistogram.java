package com.sun.jvmstat.tools.visualgc;

import com.sun.jvmstat.graph.Level;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

class VisualAgeHistogram extends JFrame
  implements ActionListener, ComponentListener
{
  static final int BUCKET_WIDTH = 20;
  static final int BUCKET_HEIGHT = 50;
  GCSample previousSample;
  Level[] bucketLevel;
  JPanel textPanel;
  JPanel histogramPanel;
  JLabel ttField;
  JLabel mttField;
  JLabel dssField;
  JLabel cssField;

  public VisualAgeHistogram(GCSample paramGCSample)
  {
    this.previousSample = paramGCSample;

    setTitle("Survivor Age Histogram");

    int i = paramGCSample.ageTableSizes.length;

    this.bucketLevel = new Level[i];

    Font localFont = new Font("Dialog", 1, 12);
    Color localColor = Color.getColor("survivor.color", new Color(255, 204, 102));

    JLabel localJLabel1 = new JLabel("Tenuring Threshold: ");
    localJLabel1.setFont(localFont);
    localJLabel1.setForeground(localColor);
    this.ttField = new JLabel(String.valueOf(paramGCSample.tenuringThreshold));
    this.ttField.setFont(localFont);
    this.ttField.setForeground(localColor);
    localJLabel1.setLabelFor(this.ttField);

    JLabel localJLabel2 = new JLabel("Max Tenuring Threshold: ");
    localJLabel2.setFont(localFont);
    localJLabel2.setForeground(localColor);
    this.mttField = new JLabel(String.valueOf(GCSample.maxTenuringThreshold));
    this.mttField.setFont(localFont);
    this.mttField.setForeground(localColor);
    localJLabel2.setLabelFor(this.mttField);

    JLabel localJLabel3 = new JLabel("Desired Survivor Size: ");
    localJLabel3.setFont(localFont);
    localJLabel3.setForeground(localColor);
    this.dssField = new JLabel(String.valueOf(paramGCSample.desiredSurvivorSize));
    this.dssField.setFont(localFont);
    this.dssField.setForeground(localColor);
    localJLabel3.setLabelFor(this.dssField);

    JLabel localJLabel4 = new JLabel("Current Survivor Size: ");
    localJLabel4.setFont(localFont);
    localJLabel4.setForeground(localColor);
    this.cssField = new JLabel(String.valueOf(paramGCSample.desiredSurvivorSize));
    this.cssField.setFont(localFont);
    this.cssField.setForeground(localColor);
    localJLabel4.setLabelFor(this.cssField);

    this.textPanel = new JPanel();
    this.textPanel.setBackground(Color.BLACK);
    this.textPanel.setLayout(new GridLayout(1, 6));
    Border localBorder = BorderFactory.createEtchedBorder(localColor, Color.GRAY);
    String str = "Parameters";
    TitledBorder localTitledBorder = BorderFactory.createTitledBorder(localBorder, str, 0, 0, localFont, localColor);

    this.textPanel.setBorder(localTitledBorder);
    this.textPanel.add(localJLabel1);
    this.textPanel.add(this.ttField);
    this.textPanel.add(localJLabel2);
    this.textPanel.add(this.mttField);
    this.textPanel.add(localJLabel3);
    this.textPanel.add(this.dssField);
    this.textPanel.add(localJLabel4);
    this.textPanel.add(this.cssField);

    this.histogramPanel = new JPanel();
    this.histogramPanel.setBackground(Color.BLACK);
    GridBagLayout localGridBagLayout = new GridBagLayout();
    this.histogramPanel.setLayout(localGridBagLayout);
    str = "Histogram";
    localTitledBorder = BorderFactory.createTitledBorder(localBorder, str, 0, 0, localFont, localColor);

    this.histogramPanel.setBorder(localTitledBorder);

    localBorder = BorderFactory.createEtchedBorder(localColor, Color.GRAY);

    Dimension localDimension = new Dimension(20, 50);

    GridBagConstraints localGridBagConstraints = new GridBagConstraints();
    localGridBagConstraints.fill = 1;
    localGridBagConstraints.gridheight = 0;
    localGridBagConstraints.weighty = 1.0D;
    localGridBagConstraints.weightx = (1.0D / i);

    for (int j = 0; j < i; j++) {
      JPanel localJPanel = new JPanel();
      localJPanel = new JPanel();
      localJPanel.setBackground(Color.BLACK);
      localJPanel.setLayout(new GridLayout(1, 1));
      str = "" + j;
      localTitledBorder = BorderFactory.createTitledBorder(localBorder, str, 0, 0, localFont, localColor);

      localJPanel.setBorder(localTitledBorder);
      this.bucketLevel[j] = new Level(localColor);
      this.bucketLevel[j].setMaximumSize(localDimension);
      this.bucketLevel[j].setMaximumSize(localDimension);
      this.bucketLevel[j].setPreferredSize(localDimension);

      localJPanel.add(this.bucketLevel[j]);

      if (j == i - 1) {
        localGridBagConstraints.gridwidth = 0;
      }

      localGridBagLayout.setConstraints(localJPanel, localGridBagConstraints);
      this.histogramPanel.add(localJPanel);
    }

    resetPanel(paramGCSample);
  }

  public void resetPanel(GCSample paramGCSample) {
    Container localContainer = getContentPane();
    localContainer.removeAll();

    GridBagLayout localGridBagLayout = new GridBagLayout();
    localContainer.setLayout(localGridBagLayout);

    GridBagConstraints localGridBagConstraints = new GridBagConstraints();
    localGridBagConstraints.fill = 1;
    localGridBagConstraints.gridwidth = 0;
    localGridBagConstraints.gridheight = -1;

    localGridBagConstraints.weighty = 0.1D;
    localGridBagConstraints.weightx = 1.0D;
    localGridBagLayout.setConstraints(this.textPanel, localGridBagConstraints);

    localGridBagConstraints.weighty = 0.9D;
    localGridBagLayout.setConstraints(this.histogramPanel, localGridBagConstraints);

    localContainer.add(this.textPanel);
    localContainer.add(this.histogramPanel);
  }

  private void resetSpace(GCSample paramGCSample) {
    resetPanel(paramGCSample);
  }

  public void refreshPanels() {
    repaint();
  }

  public void updateLevel(GCSample paramGCSample)
  {
    int i = paramGCSample.ageTableSizes.length;
    for (int j = 0; j < i; j++) {
      double d = paramGCSample.ageTableSizes[j] / paramGCSample.survivor0Size;
      this.bucketLevel[j].updateLevel(d);
    }

    this.ttField.setText(String.valueOf(paramGCSample.tenuringThreshold));
    this.mttField.setText(String.valueOf(GCSample.maxTenuringThreshold));
    this.dssField.setText(String.valueOf(paramGCSample.desiredSurvivorSize));
    this.cssField.setText(String.valueOf(paramGCSample.survivor0Capacity));
  }

  public void update(GCSample paramGCSample)
  {
    if (paramGCSample.heapSizeChanged(this.previousSample))
    {
      resetSpace(paramGCSample);
    }

    updateLevel(paramGCSample);

    refreshPanels();

    this.previousSample = paramGCSample;
  }

  public void componentHidden(ComponentEvent paramComponentEvent)
  {
  }

  public void componentMoved(ComponentEvent paramComponentEvent) {
  }

  public void componentShown(ComponentEvent paramComponentEvent) {
  }

  public void componentResized(ComponentEvent paramComponentEvent) {
    resetSpace(this.previousSample);
    validate();
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
  }
}