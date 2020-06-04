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


   public VisualAgeHistogram(GCSample var1) {
      this.previousSample = var1;
      this.setTitle(Res.getString("survivor.age.histogram"));
      int var2 = var1.ageTableSizes.length;
      this.bucketLevel = new Level[var2];
      Font var6 = new Font("Dialog", 1, 12);
      Color var7 = Color.getColor("survivor.color", new Color(255, 204, 102));
      JLabel var8 = new JLabel("Tenuring Threshold: ");
      var8.setFont(var6);
      var8.setForeground(var7);
      this.ttField = new JLabel(String.valueOf(var1.tenuringThreshold));
      this.ttField.setFont(var6);
      this.ttField.setForeground(var7);
      var8.setLabelFor(this.ttField);
      JLabel var9 = new JLabel("Max Tenuring Threshold: ");
      var9.setFont(var6);
      var9.setForeground(var7);
      this.mttField = new JLabel(String.valueOf(GCSample.maxTenuringThreshold));
      this.mttField.setFont(var6);
      this.mttField.setForeground(var7);
      var9.setLabelFor(this.mttField);
      JLabel var10 = new JLabel("Desired Survivor Size: ");
      var10.setFont(var6);
      var10.setForeground(var7);
      this.dssField = new JLabel(String.valueOf(var1.desiredSurvivorSize));
      this.dssField.setFont(var6);
      this.dssField.setForeground(var7);
      var10.setLabelFor(this.dssField);
      JLabel var11 = new JLabel("Current Survivor Size: ");
      var11.setFont(var6);
      var11.setForeground(var7);
      this.cssField = new JLabel(String.valueOf(var1.desiredSurvivorSize));
      this.cssField.setFont(var6);
      this.cssField.setForeground(var7);
      var11.setLabelFor(this.cssField);
      this.textPanel = new JPanel();
      this.textPanel.setBackground(Color.BLACK);
      this.textPanel.setLayout(new GridLayout(1, 6));
      Border var4 = BorderFactory.createEtchedBorder(var7, Color.GRAY);
      String var3 = "Parameters";
      TitledBorder var5 = BorderFactory.createTitledBorder(var4, var3, 0, 0, var6, var7);
      this.textPanel.setBorder(var5);
      this.textPanel.add(var8);
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
      var5 = BorderFactory.createTitledBorder(var4, var3, 0, 0, var6, var7);
      this.histogramPanel.setBorder(var5);
      var4 = BorderFactory.createEtchedBorder(var7, Color.GRAY);
      Dimension var13 = new Dimension(20, 50);
      GridBagConstraints var14 = new GridBagConstraints();
      var14.fill = 1;
      var14.gridheight = 0;
      var14.weighty = 1.0D;
      var14.weightx = 1.0D / (double)var2;

      for(int var15 = 0; var15 < var2; ++var15) {
         new JPanel();
         JPanel var16 = new JPanel();
         var16.setBackground(Color.BLACK);
         var16.setLayout(new GridLayout(1, 1));
         var3 = "" + var15;
         var5 = BorderFactory.createTitledBorder(var4, var3, 0, 0, var6, var7);
         var16.setBorder(var5);
         this.bucketLevel[var15] = new Level(var7);
         this.bucketLevel[var15].setMaximumSize(var13);
         this.bucketLevel[var15].setMaximumSize(var13);
         this.bucketLevel[var15].setPreferredSize(var13);
         var16.add(this.bucketLevel[var15]);
         if (var15 == var2 - 1) {
            var14.gridwidth = 0;
         }

         var12.setConstraints(var16, var14);
         this.histogramPanel.add(var16);
      }

      this.resetPanel(var1);
   }

   public void resetPanel(GCSample var1) {
      Container var2 = this.getContentPane();
      var2.removeAll();
      GridBagLayout var3 = new GridBagLayout();
      var2.setLayout(var3);
      GridBagConstraints var4 = new GridBagConstraints();
      var4.fill = 1;
      var4.gridwidth = 0;
      var4.gridheight = -1;
      var4.weighty = 0.1D;
      var4.weightx = 1.0D;
      var3.setConstraints(this.textPanel, var4);
      var4.weighty = 0.9D;
      var3.setConstraints(this.histogramPanel, var4);
      var2.add(this.textPanel);
      var2.add(this.histogramPanel);
   }

   private void resetSpace(GCSample var1) {
      this.resetPanel(var1);
   }

   public void refreshPanels() {
      this.repaint();
   }

   public void updateLevel(GCSample var1) {
      int var2 = var1.ageTableSizes.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         double var4 = (double)var1.ageTableSizes[var3] / (double)var1.survivor0Size;
         this.bucketLevel[var3].updateLevel(var4);
      }

      this.ttField.setText(String.valueOf(var1.tenuringThreshold));
      this.mttField.setText(String.valueOf(GCSample.maxTenuringThreshold));
      this.dssField.setText(String.valueOf(var1.desiredSurvivorSize));
      this.cssField.setText(String.valueOf(var1.survivor0Capacity));
   }

   public void update(GCSample var1) {
      if (var1.heapSizeChanged(this.previousSample)) {
         this.resetSpace(var1);
      }

      this.updateLevel(var1);
      this.refreshPanels();
      this.previousSample = var1;
   }

   public void componentHidden(ComponentEvent var1) {
   }

   public void componentMoved(ComponentEvent var1) {
   }

   public void componentShown(ComponentEvent var1) {
   }

   public void componentResized(ComponentEvent var1) {
      this.resetSpace(this.previousSample);
      this.validate();
   }

   public void actionPerformed(ActionEvent var1) {
   }
}
