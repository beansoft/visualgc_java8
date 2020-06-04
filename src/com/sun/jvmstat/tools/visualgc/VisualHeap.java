package com.sun.jvmstat.tools.visualgc;

import com.sun.jvmstat.graph.Level;
import com.sun.jvmstat.util.Converter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

class VisualHeap extends JFrame implements ActionListener, ComponentListener {
   private static final boolean debug = false;
   public Level permLevel = new Level(new Color(240, 200, 150));
   public Level oldLevel = new Level(new Color(204, 102, 0));
   public Level edenLevel = new Level(new Color(255, 150, 0));
   public Level s0Level = new Level(new Color(255, 204, 102));
   public Level s1Level = new Level(new Color(255, 204, 102));
   private JPanel infoPanel;
   private JPanel livenessPanel;
   private JPanel spacesPanel;
   private JPanel oldPanel;
   private JPanel newPanel;
   private JPanel edenPanel;
   private JPanel s0Panel;
   private JPanel s1Panel;
   private JPanel permPanel;
   private JLabel livenessIndicator;
   private JLabel etField;
   private double infoAreaPercent = 0.4D;
   private double textAreaPercent = 0.5D;
   public GraphGC g;
   public VisualAgeHistogram a;
   GCSample previousSample;

   public VisualHeap(GraphGC var1, VisualAgeHistogram var2, GCSample var3) {
      this.previousSample = var3;
      this.g = var1;
      this.a = var2;
      this.setTitle("VisualGC 3.0");
      this.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent var1) {
            Window var2 = var1.getWindow();
            var2.dispose();
            System.exit(0);
         }
      });
      this.infoPanel = new JPanel();
      this.infoPanel.setBackground(Color.BLACK);
      this.infoPanel.setLayout(new BorderLayout());
      Border var5 = BorderFactory.createEtchedBorder(Color.WHITE, Color.GRAY);
      Font var7 = new Font("Dialog", 1, 12);
      String var4 = "Application Information";
      TitledBorder var6 = BorderFactory.createTitledBorder(var5, var4, 0, 0, var7, Color.WHITE);
      this.infoPanel.setBorder(var6);
      this.infoPanel.addComponentListener(this);
      this.livenessPanel = new JPanel();
      this.livenessPanel.setBackground(Color.BLACK);
      this.livenessPanel.setLayout(new FlowLayout());
      this.livenessPanel.addComponentListener(this);
      this.oldPanel = new JPanel();
      this.oldPanel.setBackground(Color.BLACK);
      this.oldPanel.setLayout(new GridLayout(1, 1));
      Color var8 = Color.getColor("old.color", new Color(204, 102, 0));
      this.oldLevel = new Level(var8);
      var5 = BorderFactory.createEtchedBorder(var8, Color.GRAY);
      var4 = "Old";
      var6 = BorderFactory.createTitledBorder(var5, var4, 0, 0, var7, var8);
      this.oldPanel.setBorder(var6);
      this.oldPanel.addComponentListener(this);
      this.oldPanel.add(this.oldLevel);
      this.permPanel = new JPanel();
      this.permPanel.setBackground(Color.BLACK);
      this.permPanel.setLayout(new GridLayout(1, 1));
      Color var9 = Color.getColor("perm.color", new Color(240, 200, 150));
      this.permLevel = new Level(var9);
      var5 = BorderFactory.createEtchedBorder(var9, Color.GRAY);
      var4 = "Perm";
      var6 = BorderFactory.createTitledBorder(var5, var4, 0, 0, var7, var9);
      this.permPanel.setBorder(var6);
      this.permPanel.addComponentListener(this);
      this.permPanel.add(this.permLevel);
      this.edenPanel = new JPanel();
      this.edenPanel.setBackground(Color.BLACK);
      this.edenPanel.setLayout(new GridLayout(1, 1));
      Color var10 = Color.getColor("eden.color", new Color(255, 150, 0));
      this.edenLevel = new Level(var10);
      var5 = BorderFactory.createEtchedBorder(var10, Color.GRAY);
      var4 = "Eden";
      var6 = BorderFactory.createTitledBorder(var5, var4, 0, 0, var7, var10);
      this.edenPanel.setBorder(var6);
      this.edenPanel.addComponentListener(this);
      this.edenPanel.add(this.edenLevel);
      Color var11 = Color.getColor("survivor.color", new Color(255, 204, 102));
      this.s0Panel = new JPanel();
      this.s0Panel.setBackground(Color.BLACK);
      this.s0Panel.setLayout(new GridLayout(1, 1));
      this.s0Level = new Level(var11);
      var5 = BorderFactory.createEtchedBorder(var11, Color.GRAY);
      var4 = "S0";
      var6 = BorderFactory.createTitledBorder(var5, var4, 0, 0, var7, var11);
      this.s0Panel.setBorder(var6);
      this.s0Panel.addComponentListener(this);
      this.s0Panel.add(this.s0Level);
      this.s1Panel = new JPanel();
      this.s1Panel.setBackground(Color.BLACK);
      this.s1Panel.setLayout(new GridLayout(1, 1));
      this.s1Level = new Level(var11);
      var5 = BorderFactory.createEtchedBorder(var11, Color.GRAY);
      var4 = "S1";
      var6 = BorderFactory.createTitledBorder(var5, var4, 0, 0, var7, var11);
      this.s1Panel.setBorder(var6);
      this.s1Panel.addComponentListener(this);
      this.s1Panel.add(this.s1Level);
      Border var12 = BorderFactory.createEmptyBorder();
      this.newPanel = new JPanel();
      this.newPanel.setBackground(Color.BLACK);
      this.newPanel.setBorder(var12);
      this.spacesPanel = new JPanel();
      this.spacesPanel.setBackground(Color.BLACK);
      this.spacesPanel.setBorder(var12);
      this.initializeInfoPanel(var3);
      this.resetPanel(var3);
   }

   private void initializeInfoPanel(GCSample var1) {
      Font var2 = new Font("Dialog", 1, 12);
      this.livenessIndicator = new JLabel("Alive    ");
      this.livenessIndicator.setFont(var2);
      this.livenessIndicator.setForeground(Color.green);
      JLabel var3 = new JLabel("Elapsed Time: ");
      var3.setFont(var2);
      var3.setForeground(Color.WHITE);
      this.etField = new JLabel(Converter.longToTimeString(var1.osElapsedTime, GCSample.osFrequency));
      this.etField.setFont(var2);
      this.etField.setForeground(Color.WHITE);
      var3.setLabelFor(this.etField);
      JTextArea var4 = new JTextArea();
      var4.setFont(var2);
      var4.setForeground(Color.WHITE);
      var4.setBackground(Color.BLACK);
      var4.append("Java Command Line: " + GCSample.javaCommand + "\n\n");
      var4.append("Java VM Arguments: " + GCSample.vmArgs + "\n\n");
      var4.append("Java VM Flags: " + GCSample.vmFlags + "\n\n");
      var4.append("java.home=" + GCSample.javaHome + "\n\n");
      var4.append("java.class.path=" + GCSample.classPath + "\n\n");
      var4.append("java.library.path=" + GCSample.libraryPath + "\n\n");
      var4.append("java.endorsed.dirs=" + GCSample.endorsedDirs + "\n\n");
      var4.append("java.ext.dirs=" + GCSample.extDirs + "\n\n");
      var4.append("sun.boot.class.path=" + GCSample.bootClassPath + "\n\n");
      var4.append("sun.boot.library.path=" + GCSample.bootLibraryPath + "\n\n");
      var4.append("java.vm.name=" + GCSample.vmName + "\n\n");
      var4.append("java.vm.info=" + GCSample.vmInfo + "\n\n");
      var4.append("java.vm.vendor=" + GCSample.vmVendor + "\n\n");
      var4.append("java.vm.version=" + GCSample.vmVersion + "\n\n");
      var4.append("java.vm.specification.name=" + GCSample.vmSpecName + "\n\n");
      var4.append("java.vm.specification.vendor=" + GCSample.vmSpecVendor + "\n\n");
      var4.append("java.vm.specification.version=" + GCSample.vmSpecVersion + "\n\n");
      var4.setRows(4);
      var4.setLineWrap(true);
      JScrollPane var5 = new JScrollPane(var4);
      this.updateTextFields(var1);
      this.livenessPanel.add(this.livenessIndicator);
      this.livenessPanel.add(var3);
      this.livenessPanel.add(this.etField);
      this.infoPanel.add(this.livenessPanel, "North");
      this.infoPanel.add(var5, "Center");
   }

   private void buildStackedSurvivors(GCSample var1) {
      GridBagLayout var2 = new GridBagLayout();
      this.newPanel.setLayout(var2);
      GridBagConstraints var3 = new GridBagConstraints();
      var3.fill = 1;
      var3.gridwidth = 0;
      double var4 = (double)var1.survivor0Capacity / (double)var1.newGenMaxSize;
      double var6 = (double)var1.survivor1Capacity / (double)var1.newGenMaxSize;
      double var8 = 1.0D - (var4 + var6);
      var3.weightx = 1.0D;
      var3.weighty = var8;
      var2.setConstraints(this.edenPanel, var3);
      var3.weighty = var4;
      var2.setConstraints(this.s0Panel, var3);
      var3.weighty = var6;
      var3.gridheight = 0;
      var2.setConstraints(this.s1Panel, var3);
      this.newPanel.add(this.edenPanel);
      this.newPanel.add(this.s0Panel);
      this.newPanel.add(this.s1Panel);
   }

   private void resetPanel(GCSample var1) {
      Container var2 = this.getContentPane();
      var2.removeAll();
      this.newPanel.removeAll();
      this.spacesPanel.removeAll();
      this.buildStackedSurvivors(var1);
      GridBagLayout var3 = new GridBagLayout();
      this.spacesPanel.setLayout(var3);
      GridBagConstraints var4 = new GridBagConstraints();
      var4.fill = 1;
      var4.gridheight = 0;
      var4.weighty = 1.0D;
      double var5 = (double)(var1.permSize + var1.tenuredSize + var1.newGenMaxSize);
      double var7 = (double)var1.permSize / var5;
      double var9 = (double)var1.tenuredSize / var5;
      double var11 = 1.0D - (var7 + var9);
      var4.weightx = var7;
      var3.setConstraints(this.permPanel, var4);
      var4.weightx = var9;
      var3.setConstraints(this.oldPanel, var4);
      var4.weightx = var11;
      var4.gridwidth = 0;
      var3.setConstraints(this.newPanel, var4);
      this.spacesPanel.add(this.permPanel);
      this.spacesPanel.add(this.oldPanel);
      this.spacesPanel.add(this.newPanel);
      var2.add(this.infoPanel, "North");
      var2.add(this.spacesPanel, "Center");
   }

   public void componentHidden(ComponentEvent var1) {
   }

   public void componentMoved(ComponentEvent var1) {
   }

   public void componentShown(ComponentEvent var1) {
   }

   public void componentResized(ComponentEvent var1) {
      this.resetSpace(this.previousSample);
      this.g.validate();
      if (this.a != null) {
         this.a.validate();
      }

   }

   private void resetSpace(GCSample var1) {
      this.resetPanel(var1);
      this.g.resetPanel(var1);
      if (this.a != null) {
         this.a.resetPanel(var1);
      }

   }

   public void actionPerformed(ActionEvent var1) {
   }

   public void update(GCSample var1) {
      if (var1.heapSizeChanged(this.previousSample)) {
         this.resetSpace(var1);
      }

      this.updateLevel(var1);
      this.updateTextFields(var1);
      this.refreshPanels();
      this.previousSample = var1;
   }

   public void refreshPanels() {
      this.repaint();
   }

   public void updateLevel(GCSample var1) {
      this.permLevel.updateLevel(var1.getPermLiveRatio());
      this.permLevel.updateGrayLevel(1.0D - var1.getPermCommittedRatio());
      this.oldLevel.updateLevel(var1.getTenuredLiveRatio());
      this.oldLevel.updateGrayLevel(1.0D - var1.getTenuredCommittedRatio());
      double var2 = var1.getAdjustedEdenSize();
      double var4 = (double)var1.edenUsed;
      double var6 = (double)var1.edenCapacity;
      this.edenLevel.updateGrayLevel(1.0D - var6 / var2);
      this.edenLevel.updateLevel(var4 / var2);
      double var8 = (double)var1.survivor0Used;
      double var10 = (double)var1.survivor0Capacity;
      this.s0Level.updateLevel(var8 / var10);
      double var12 = (double)var1.survivor1Used;
      double var14 = (double)var1.survivor1Capacity;
      this.s1Level.updateLevel(var12 / var14);
   }

   public void updateTextFields(GCSample var1) {
      Color var2 = Color.WHITE;
      String var3 = "Alive     ";
      if (var1 != this.previousSample) {
         if (var1.osElapsedTime == this.previousSample.osElapsedTime) {
            if (this.livenessIndicator.getForeground() != Color.red) {
               var2 = Color.red;
            }

            var3 = "Dead     ";
         } else if (this.livenessIndicator.getForeground() != Color.green) {
            var2 = Color.green;
         }
      }

      this.livenessIndicator.setForeground(var2);
      this.livenessIndicator.setText(var3);
      this.etField.setText(Converter.longToTimeString(var1.osElapsedTime, GCSample.osFrequency));
   }

   public void update(Graphics var1) {
      this.paint(var1);
   }

   public void draw() {
      this.repaint();
   }

   public void paint(Graphics var1) {
      super.paint(var1);
   }
}
