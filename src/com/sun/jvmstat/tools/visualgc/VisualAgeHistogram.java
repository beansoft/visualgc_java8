package com.sun.jvmstat.tools.visualgc;

import com.sun.jvmstat.graph.Level;
import com.sun.jvmstat.tools.visualgc.resource.Res;

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
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

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
      JLabel jLabel = new JLabel("Tenuring Threshold: ");
      jLabel.setFont(font);
      jLabel.setForeground(color);
      this.ttField = new JLabel(String.valueOf(gcSample.tenuringThreshold));
      this.ttField.setFont(font);
      this.ttField.setForeground(color);
      jLabel.setLabelFor(this.ttField);
      JLabel var9 = new JLabel("Max Tenuring Threshold: ");
      var9.setFont(font);
      var9.setForeground(color);
      this.mttField = new JLabel(String.valueOf(GCSample.maxTenuringThreshold));
      this.mttField.setFont(font);
      this.mttField.setForeground(color);
      var9.setLabelFor(this.mttField);
      JLabel var10 = new JLabel("Desired Survivor Size: ");
      var10.setFont(font);
      var10.setForeground(color);
      this.dssField = new JLabel(String.valueOf(gcSample.desiredSurvivorSize));
      this.dssField.setFont(font);
      this.dssField.setForeground(color);
      var10.setLabelFor(this.dssField);
      JLabel var11 = new JLabel("Current Survivor Size: ");
      var11.setFont(font);
      var11.setForeground(color);
      this.cssField = new JLabel(String.valueOf(gcSample.desiredSurvivorSize));
      this.cssField.setFont(font);
      this.cssField.setForeground(color);
      var11.setLabelFor(this.cssField);
      this.textPanel = new JPanel();
      this.textPanel.setBackground(Color.BLACK);
      this.textPanel.setLayout(new GridLayout(1, 6));
      Border var4 = BorderFactory.createEtchedBorder(color, Color.GRAY);
      String var3 = "Parameters";
      TitledBorder var5 = BorderFactory.createTitledBorder(var4, var3, 0, 0, font, color);
      this.textPanel.setBorder(var5);
      this.textPanel.add(jLabel);
      this.textPanel.add(this.ttField);
      this.textPanel.add(var9);
      this.textPanel.add(this.mttField);
      this.textPanel.add(var10);
      this.textPanel.add(this.dssField);
      this.textPanel.add(var11);
      this.textPanel.add(this.cssField);
      this.histogramPanel = new JPanel();
      this.histogramPanel.setBackground(Color.BLACK);
      GridBagLayout var12 = new GridBagLayout();
      this.histogramPanel.setLayout(var12);
      var3 = "Histogram";
      var5 = BorderFactory.createTitledBorder(var4, var3, 0, 0, font, color);
      this.histogramPanel.setBorder(var5);
      var4 = BorderFactory.createEtchedBorder(color, Color.GRAY);
      Dimension var13 = new Dimension(20, 50);
      GridBagConstraints var14 = new GridBagConstraints();
      var14.fill = 1;
      var14.gridheight = 0;
      var14.weighty = 1.0D;
      var14.weightx = 1.0D / (double)length;

      for(int var15 = 0; var15 < length; ++var15) {
         new JPanel();
         JPanel var16 = new JPanel();
         var16.setBackground(Color.BLACK);
         var16.setLayout(new GridLayout(1, 1));
         var3 = "" + var15;
         var5 = BorderFactory.createTitledBorder(var4, var3, 0, 0, font, color);
         var16.setBorder(var5);
         this.bucketLevel[var15] = new Level(color);
         this.bucketLevel[var15].setMaximumSize(var13);
         this.bucketLevel[var15].setMaximumSize(var13);
         this.bucketLevel[var15].setPreferredSize(var13);
         var16.add(this.bucketLevel[var15]);
         if (var15 == length - 1) {
            var14.gridwidth = 0;
         }

         var12.setConstraints(var16, var14);
         this.histogramPanel.add(var16);
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

   public void componentHidden(ComponentEvent e) {
   }

   public void componentMoved(ComponentEvent e) {
   }

   public void componentShown(ComponentEvent e) {
   }

   public void componentResized(ComponentEvent e) {
      this.resetSpace(this.previousSample);
      this.validate();
   }

   public void actionPerformed(ActionEvent e) {
   }
}
