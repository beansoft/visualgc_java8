package com.sun.jvmstat.tools.visualgc;

import com.sun.jvmstat.graph.Level;
import com.sun.jvmstat.tools.visualgc.resource.Res;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

class VisualAgeHistogram extends JFrame implements ActionListener, ComponentListener {
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


   public VisualAgeHistogram(GCSample gcSample) {
      this.previousSample = gcSample;
      this.setTitle(Res.getString("survivor.age.histogram"));
      int length = gcSample.ageTableSizes.length;
      this.bucketLevel = new Level[length];
      Font font = new Font("Dialog", 1, 12);
      Color color = Color.getColor("survivor.color", new Color(255, 204, 102));
      JLabel jLabel = new JLabel(Res.getString("tenuring.threshold"));
      jLabel.setFont(font);
      jLabel.setForeground(color);
      this.ttField = new JLabel(String.valueOf(gcSample.tenuringThreshold));
      this.ttField.setFont(font);
      this.ttField.setForeground(color);
      jLabel.setLabelFor(this.ttField);
      JLabel maxTTLabel = new JLabel(Res.getString("max.tenuring.threshold"));
      maxTTLabel.setFont(font);
      maxTTLabel.setForeground(color);
      this.mttField = new JLabel(String.valueOf(GCSample.maxTenuringThreshold));
      this.mttField.setFont(font);
      this.mttField.setForeground(color);
      maxTTLabel.setLabelFor(this.mttField);
      JLabel dssLabel = new JLabel(Res.getString("desired.survivor.size"));
      dssLabel.setFont(font);
      dssLabel.setForeground(color);
      this.dssField = new JLabel(String.valueOf(gcSample.desiredSurvivorSize));
      this.dssField.setFont(font);
      this.dssField.setForeground(color);
      dssLabel.setLabelFor(this.dssField);
      JLabel cssLabel = new JLabel(Res.getString("current.survivor.size"));
      cssLabel.setFont(font);
      cssLabel.setForeground(color);
      this.cssField = new JLabel(String.valueOf(gcSample.desiredSurvivorSize));
      this.cssField.setFont(font);
      this.cssField.setForeground(color);
      cssLabel.setLabelFor(this.cssField);
      this.textPanel = new JPanel();
      this.textPanel.setBackground(Color.BLACK);
      this.textPanel.setLayout(new GridLayout(1, 6));
      Border etchedBorder = BorderFactory.createEtchedBorder(color, Color.GRAY);
      String parameters = Res.getString("parameters");
      TitledBorder titledBorder = BorderFactory.createTitledBorder(etchedBorder, parameters, 0, 0, font, color);
      this.textPanel.setBorder(titledBorder);
      this.textPanel.add(jLabel);
      this.textPanel.add(this.ttField);
      this.textPanel.add(maxTTLabel);
      this.textPanel.add(this.mttField);
      this.textPanel.add(dssLabel);
      this.textPanel.add(this.dssField);
      this.textPanel.add(cssLabel);
      this.textPanel.add(this.cssField);
      this.histogramPanel = new JPanel();
      this.histogramPanel.setBackground(Color.BLACK);
      GridBagLayout gridBagLayout = new GridBagLayout();
      this.histogramPanel.setLayout(gridBagLayout);
      parameters = Res.getString("histogram");
      titledBorder = BorderFactory.createTitledBorder(etchedBorder, parameters, 0, 0, font, color);
      this.histogramPanel.setBorder(titledBorder);
      etchedBorder = BorderFactory.createEtchedBorder(color, Color.GRAY);
      Dimension preferredSize = new Dimension(20, 50);
      GridBagConstraints gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.fill = 1;
      gridBagConstraints.gridheight = 0;
      gridBagConstraints.weighty = 1.0D;
      gridBagConstraints.weightx = 1.0D / (double)length;

      for(int i = 0; i < length; ++i) {
         JPanel chart = new JPanel();
         chart.setBackground(Color.BLACK);
         chart.setLayout(new GridLayout(1, 1));
         parameters = "" + i;
         titledBorder = BorderFactory.createTitledBorder(etchedBorder, parameters, 0, 0, font, color);
         chart.setBorder(titledBorder);
         this.bucketLevel[i] = new Level(color);
         this.bucketLevel[i].setMaximumSize(preferredSize);
         this.bucketLevel[i].setMaximumSize(preferredSize);
         this.bucketLevel[i].setPreferredSize(preferredSize);
         chart.add(this.bucketLevel[i]);
         if (i == length - 1) {
            gridBagConstraints.gridwidth = 0;
         }

         gridBagLayout.setConstraints(chart, gridBagConstraints);
         this.histogramPanel.add(chart);
      }

      this.resetPanel(gcSample);
   }

   public void resetPanel(GCSample sample) {
      Container container = getContentPane();
      container.removeAll();
      GridBagLayout gridBagLayout = new GridBagLayout();
      container.setLayout(gridBagLayout);
      GridBagConstraints gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.fill = 1;
      gridBagConstraints.gridwidth = 0;
      gridBagConstraints.gridheight = -1;
      gridBagConstraints.weighty = 0.1D;
      gridBagConstraints.weightx = 1.0D;
      gridBagLayout.setConstraints(this.textPanel, gridBagConstraints);
      gridBagConstraints.weighty = 0.9D;
      gridBagLayout.setConstraints(this.histogramPanel, gridBagConstraints);
      container.add(this.textPanel);
      container.add(this.histogramPanel);
   }

   private void resetSpace(GCSample sample) {
      this.resetPanel(sample);
   }

   public void refreshPanels() {
      this.repaint();
   }

   public void updateLevel(GCSample sample) {
      int length = sample.ageTableSizes.length;

      for(int i = 0; i < length; ++i) {
         double var4 = (double)sample.ageTableSizes[i] / (double)sample.survivor0Size;
         this.bucketLevel[i].updateLevel(var4);
      }

      this.ttField.setText(String.valueOf(sample.tenuringThreshold));
      this.mttField.setText(String.valueOf(GCSample.maxTenuringThreshold));
      this.dssField.setText(String.valueOf(sample.desiredSurvivorSize));
      this.cssField.setText(String.valueOf(sample.survivor0Capacity));
   }

   public void update(GCSample sample) {
      if (sample.heapSizeChanged(this.previousSample)) {
         this.resetSpace(sample);
      }

      this.updateLevel(sample);
      this.refreshPanels();
      this.previousSample = sample;
   }

   public void componentHidden(ComponentEvent e) { }

   public void componentMoved(ComponentEvent e) { }

   public void componentShown(ComponentEvent e) {
   }

   public void componentResized(ComponentEvent e) {
      this.resetSpace(this.previousSample);
      this.validate(); }

   public void actionPerformed(ActionEvent e) {}
}