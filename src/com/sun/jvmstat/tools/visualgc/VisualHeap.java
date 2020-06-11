package com.sun.jvmstat.tools.visualgc;

import com.sun.jvmstat.graph.Level;
import com.sun.jvmstat.tools.visualgc.resource.Res;
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
import java.awt.event.WindowEvent;import java.text.MessageFormat;
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

   public VisualHeap(GraphGC graphGC, VisualAgeHistogram ageHistogram, GCSample gcSample) {
      this.previousSample = gcSample;
      this.g = graphGC;
      this.a = ageHistogram;
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
      Border etchedBorder = BorderFactory.createEtchedBorder(Color.WHITE, Color.GRAY);
      Font font = new Font("Dialog", 1, 12);
      String title = Res.getString("application.information");
      TitledBorder titledBorder = BorderFactory.createTitledBorder(etchedBorder, title, 0, 0, font, Color.WHITE);
      this.infoPanel.setBorder(titledBorder);
      this.infoPanel.addComponentListener(this);
      this.livenessPanel = new JPanel();
      this.livenessPanel.setBackground(Color.BLACK);
      this.livenessPanel.setLayout(new FlowLayout());
      this.livenessPanel.addComponentListener(this);
      this.oldPanel = new JPanel();
      this.oldPanel.setBackground(Color.BLACK);
      this.oldPanel.setLayout(new GridLayout(1, 1));
      Color color = Color.getColor("old.color", new Color(204, 102, 0));
      this.oldLevel = new Level(color);
      etchedBorder = BorderFactory.createEtchedBorder(color, Color.GRAY);
      title = Res.getString("old");
      titledBorder = BorderFactory.createTitledBorder(etchedBorder, title, 0, 0, font, color);
      this.oldPanel.setBorder(titledBorder);
      this.oldPanel.addComponentListener(this);
      this.oldPanel.add(this.oldLevel);
      this.permPanel = new JPanel();
      this.permPanel.setBackground(Color.BLACK);
      this.permPanel.setLayout(new GridLayout(1, 1));
      Color permColor = Color.getColor("perm.color", new Color(240, 200, 150));
      this.permLevel = new Level(permColor);
      etchedBorder = BorderFactory.createEtchedBorder(permColor, Color.GRAY);
      title = Res.getString("perm");
      titledBorder = BorderFactory.createTitledBorder(etchedBorder, title, 0, 0, font, permColor);
      this.permPanel.setBorder(titledBorder);
      this.permPanel.addComponentListener(this);
      this.permPanel.add(this.permLevel);
      this.edenPanel = new JPanel();
      this.edenPanel.setBackground(Color.BLACK);
      this.edenPanel.setLayout(new GridLayout(1, 1));
      Color var10 = Color.getColor("eden.color", new Color(255, 150, 0));
      this.edenLevel = new Level(var10);
      etchedBorder = BorderFactory.createEtchedBorder(var10, Color.GRAY);
      title = Res.getString("eden");
      titledBorder = BorderFactory.createTitledBorder(etchedBorder, title, 0, 0, font, var10);
      this.edenPanel.setBorder(titledBorder);
      this.edenPanel.addComponentListener(this);
      this.edenPanel.add(this.edenLevel);
      Color var11 = Color.getColor("survivor.color", new Color(255, 204, 102));
      this.s0Panel = new JPanel();
      this.s0Panel.setBackground(Color.BLACK);
      this.s0Panel.setLayout(new GridLayout(1, 1));
      this.s0Level = new Level(var11);
      etchedBorder = BorderFactory.createEtchedBorder(var11, Color.GRAY);
      title = Res.getString("s0");
      titledBorder = BorderFactory.createTitledBorder(etchedBorder, title, 0, 0, font, var11);
      this.s0Panel.setBorder(titledBorder);
      this.s0Panel.addComponentListener(this);
      this.s0Panel.add(this.s0Level);
      this.s1Panel = new JPanel();
      this.s1Panel.setBackground(Color.BLACK);
      this.s1Panel.setLayout(new GridLayout(1, 1));
      this.s1Level = new Level(var11);
      etchedBorder = BorderFactory.createEtchedBorder(var11, Color.GRAY);
      title = Res.getString("s1");
      titledBorder = BorderFactory.createTitledBorder(etchedBorder, title, 0, 0, font, var11);
      this.s1Panel.setBorder(titledBorder);
      this.s1Panel.addComponentListener(this);
      this.s1Panel.add(this.s1Level);
      Border var12 = BorderFactory.createEmptyBorder();
      this.newPanel = new JPanel();
      this.newPanel.setBackground(Color.BLACK);
      this.newPanel.setBorder(var12);
      this.spacesPanel = new JPanel();
      this.spacesPanel.setBackground(Color.BLACK);
      this.spacesPanel.setBorder(var12);
      this.initializeInfoPanel(gcSample);
      this.resetPanel(gcSample);
   }

   private void initializeInfoPanel(GCSample var1) {
      Font font = new Font("Dialog", 1, 12);
      this.livenessIndicator = new JLabel(Res.getString("alive"));
      this.livenessIndicator.setFont(font);
      this.livenessIndicator.setForeground(Color.green);
      JLabel elapsedLabel = new JLabel(Res.getString("elapsed.time"));
      elapsedLabel.setFont(font);
      elapsedLabel.setForeground(Color.WHITE);
      this.etField = new JLabel(Converter.longToTimeString(var1.osElapsedTime, GCSample.osFrequency));
      this.etField.setFont(font);
      this.etField.setForeground(Color.WHITE);
      elapsedLabel.setLabelFor(this.etField);
      JTextArea textArea = new JTextArea();
      textArea.setFont(font);
      textArea.setForeground(Color.WHITE);
      textArea.setBackground(Color.BLACK);
      textArea.append(MessageFormat.format(Res.getString("java.command.line.0"), GCSample.javaCommand));
      textArea.append(MessageFormat.format(Res.getString("java.vm.arguments.0"), GCSample.vmArgs));
      textArea.append(MessageFormat.format(Res.getString("java.vm.flags.0"), GCSample.vmFlags));
      textArea.append("java.home=" + GCSample.javaHome + "\n\n");
      textArea.append("java.class.path=" + GCSample.classPath + "\n\n");
      textArea.append("java.library.path=" + GCSample.libraryPath + "\n\n");
      textArea.append("java.endorsed.dirs=" + GCSample.endorsedDirs + "\n\n");
      textArea.append("java.ext.dirs=" + GCSample.extDirs + "\n\n");
      textArea.append("sun.boot.class.path=" + GCSample.bootClassPath + "\n\n");
      textArea.append("sun.boot.library.path=" + GCSample.bootLibraryPath + "\n\n");
      textArea.append("java.vm.name=" + GCSample.vmName + "\n\n");
      textArea.append("java.vm.info=" + GCSample.vmInfo + "\n\n");
      textArea.append("java.vm.vendor=" + GCSample.vmVendor + "\n\n");
      textArea.append("java.vm.version=" + GCSample.vmVersion + "\n\n");
      textArea.append("java.vm.specification.name=" + GCSample.vmSpecName + "\n\n");
      textArea.append("java.vm.specification.vendor=" + GCSample.vmSpecVendor + "\n\n");
      textArea.append("java.vm.specification.version=" + GCSample.vmSpecVersion + "\n\n");
      textArea.setRows(4);
      textArea.setLineWrap(true);
      JScrollPane var5 = new JScrollPane(textArea);
      this.updateTextFields(var1);
      this.livenessPanel.add(this.livenessIndicator);
      this.livenessPanel.add(elapsedLabel);
      this.livenessPanel.add(this.etField);

      this.infoPanel.add(this.livenessPanel, "North");
      this.infoPanel.add(var5, "Center");

//      this.infoPanel.add(), BorderLayout.SOUTH);
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
      var2.add(this.infoPanel, BorderLayout.NORTH);
      var2.add(this.spacesPanel, BorderLayout.CENTER);
   }

   public void componentHidden(ComponentEvent e) {
   }

   public void componentMoved(ComponentEvent e) {
   }

   public void componentShown(ComponentEvent e) {
   }

   public void componentResized(ComponentEvent e) {
      this.resetSpace(this.previousSample);
      this.g.validate();
      if (this.a != null) {
         this.a.validate();
      }
   }

   private void resetSpace(GCSample sample) {
      this.resetPanel(sample);
      this.g.resetPanel(sample);
      if (this.a != null) {
         this.a.resetPanel(sample);
      }

   }

   public void actionPerformed(ActionEvent e) {
   }

   public void update(GCSample sample) {
      if (sample.heapSizeChanged(this.previousSample)) {
         this.resetSpace(sample);
      }

      this.updateLevel(sample);
      this.updateTextFields(sample);
      this.refreshPanels();
      this.previousSample = sample;
   }

   public void refreshPanels() {
      this.repaint();
   }

   public void updateLevel(GCSample sample) {
      this.permLevel.updateLevel(sample.getPermLiveRatio());
      this.permLevel.updateGrayLevel(1.0D - sample.getPermCommittedRatio());
      this.oldLevel.updateLevel(sample.getTenuredLiveRatio());
      this.oldLevel.updateGrayLevel(1.0D - sample.getTenuredCommittedRatio());
      double adjustedEdenSize = sample.getAdjustedEdenSize();
      double edenUsed = (double)sample.edenUsed;
      double edenCapacity = (double)sample.edenCapacity;
      this.edenLevel.updateGrayLevel(1.0D - edenCapacity / adjustedEdenSize);
      this.edenLevel.updateLevel(edenUsed / adjustedEdenSize);
      double survivor0Used = (double)sample.survivor0Used;
      double survivor0Capacity = (double)sample.survivor0Capacity;
      this.s0Level.updateLevel(survivor0Used / survivor0Capacity);
      double survivor1Used = (double)sample.survivor1Used;
      double survivor1Capacity = (double)sample.survivor1Capacity;
      this.s1Level.updateLevel(survivor1Used / survivor1Capacity);
   }

   public void updateTextFields(GCSample sample) {
      Color color = Color.WHITE;
      String str = Res.getString("alive");
      if (sample != this.previousSample) {
         if (sample.osElapsedTime == this.previousSample.osElapsedTime) {
            if (this.livenessIndicator.getForeground() != Color.red) {
               color = Color.red;
            }

            str = Res.getString("dead");
         } else if (this.livenessIndicator.getForeground() != Color.green) {
            color = Color.green;
         }
      }

      this.livenessIndicator.setForeground(color);
      this.livenessIndicator.setText(str);
      this.etField.setText(Converter.longToTimeString(sample.osElapsedTime, GCSample.osFrequency));
   }

   public void update(Graphics g) {
      this.paint(g);
   }

   public void draw() {
      this.repaint();
   }

}
