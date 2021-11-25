package com.sun.jvmstat.tools.visualgc;

import com.intellij.ide.ui.laf.darcula.ui.DarculaTextAreaUI;
import com.intellij.openapi.ui.ex.MultiLineLabel;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.util.ui.JBUI;
import com.sun.jvmstat.graph.Level;
import com.sun.jvmstat.tools.visualgc.resource.Res;
import com.sun.jvmstat.util.Converter;
import com.yworks.util.annotation.Obfuscation;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.List;

/**
 * 可视化堆空间(柱图区).
 */
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
   @Obfuscation
   private JPanel permPanel;
   @Obfuscation
   private JLabel livenessIndicator;// process liveness indicator
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
      Font font = new Font("Dialog", Font.BOLD, 12);
      String title = Res.getString("application.information");
      TitledBorder titledBorder = BorderFactory.createTitledBorder(etchedBorder, title, 0, 0,
              font, JBColor.DARK_GRAY);
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

   //  Init the process infomation pane
   private void initializeInfoPanel(GCSample sample) {
      Font font = new Font("Dialog", Font.BOLD, 12);
      this.livenessIndicator = new JLabel(Res.getString("alive"));
      this.livenessIndicator.setFont(font);
      this.livenessIndicator.setForeground(Color.green);
      JLabel elapsedLabel = new JLabel(Res.getString("elapsed.time"));
      elapsedLabel.setFont(font);
//      elapsedLabel.setForeground(Color.WHITE);
      this.etField = new JLabel(Converter.longToTimeString(sample.osElapsedTime, GCSample.osFrequency));
      this.etField.setFont(font);
//      this.etField.setForeground(Color.WHITE);
      elapsedLabel.setLabelFor(this.etField);

      StringBuilder vmInfoString = new StringBuilder();

      vmInfoString.append(MessageFormat.format(Res.getString("java.command.line.0"), GCSample.javaCommand));
      vmInfoString.append(MessageFormat.format(Res.getString("java.vm.arguments.0"), GCSample.vmArgs));
      vmInfoString.append(MessageFormat.format(Res.getString("java.vm.flags.0"), GCSample.vmFlags));
      vmInfoString.append("java.home=" + GCSample.javaHome + "\n");
      vmInfoString.append("java.class.path=" + GCSample.classPath + "\n");
      vmInfoString.append("java.library.path=" + GCSample.libraryPath + "\n");
      vmInfoString.append("java.endorsed.dirs=" + GCSample.endorsedDirs + "\n");
      vmInfoString.append("java.ext.dirs=" + GCSample.extDirs + "\n");
      vmInfoString.append("sun.boot.class.path=" + GCSample.bootClassPath + "\n");
      vmInfoString.append("sun.boot.library.path=" + GCSample.bootLibraryPath + "\n");
      vmInfoString.append("java.vm.name=" + GCSample.vmName + "\n");
      vmInfoString.append("java.vm.info=" + GCSample.vmInfo + "\n");
      vmInfoString.append("java.vm.vendor=" + GCSample.vmVendor + "\n");
      vmInfoString.append("java.vm.version=" + GCSample.vmVersion + "\n");
      vmInfoString.append("java.vm.specification.name=" + GCSample.vmSpecName + "\n");
      vmInfoString.append("java.vm.specification.vendor=" + GCSample.vmSpecVendor + "\n");
      vmInfoString.append("java.vm.specification.version=" + GCSample.vmSpecVersion + "\n");

      JBTextArea textArea = new JBTextArea() {
         /** 此处的设置颜色外部调用时总会变成 black, 强制覆盖一下. 最终发现是被项目中的方法强制设置成了黑色.
          * @date 2021-11-25
          * @see VisualGCPane#customizeComponents(Component, List)
          * <code>jComponent instanceof JTextArea</code>
          * @param fg
          */
         public void setForeground(Color fg) {
//            Color oldFg = JBColor.gray;
            super.setForeground(fg);
         }
      };

      textArea.setCaretColor(JBColor.red);
      textArea.setFont(font);
//      textArea.setForeground(JBColor.GRAY);
      textArea.setRows(8);
      textArea.setLineWrap(true);

      textArea.setText( vmInfoString.toString());
//      appendToPane(textArea, vmInfoString.toString(), Color.RED);
//      textArea.setMinimumSize(JBUI.size(400, 200));
//      textArea.setBackground(Color.BLACK);

//      JTextPane textPane = new JTextPane();
//      appendToPane(textPane, vmInfoString.toString(), Color.RED);
//      textPane.setMaximumSize(JBUI.size(0, 200));
//      MultiLineLabel multiLineLabel = new MultiLineLabel();
//      multiLineLabel.setText(vmInfoString.toString());
//      multiLineLabel.setMaximumSize(JBUI.size(0, 200));

      JScrollPane appInfoScrollPane = new JBScrollPane(textArea);

      this.updateTextFields(sample);
      this.livenessPanel.add(this.livenessIndicator);
      this.livenessPanel.add(elapsedLabel);
      this.livenessPanel.add(this.etField);

      this.infoPanel.add(this.livenessPanel, "North");
      this.infoPanel.add(appInfoScrollPane, BorderLayout.SOUTH);
//      infoPanel.setMinimumSize(new Dimension(0, 400));
//      this.infoPanel.add(), BorderLayout.SOUTH);
   }

   private void appendToPane(JTextArea tp, String msg, Color c)
   {
      StyleContext sc = StyleContext.getDefaultStyleContext();
      AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

      aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
      aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

      int len = tp.getDocument().getLength();
      tp.setCaretPosition(len);
//      tp.setCharacterAttributes(aset, false);
      tp.replaceSelection(msg);
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

   private void resetPanel(GCSample gcSample) {
      Container var2 = this.getContentPane();
      var2.removeAll();
      this.newPanel.removeAll();
      this.spacesPanel.removeAll();
      this.buildStackedSurvivors(gcSample);
      GridBagLayout gridBagLayout = new GridBagLayout();
      this.spacesPanel.setLayout(gridBagLayout);
      GridBagConstraints var4 = new GridBagConstraints();
      var4.fill = 1;
      var4.gridheight = 0;
      var4.weighty = 1.0D;
      double var5 = (double)(gcSample.permSize + gcSample.tenuredSize + gcSample.newGenMaxSize);
      double var7 = (double)gcSample.permSize / var5;
      double var9 = (double)gcSample.tenuredSize / var5;
      double var11 = 1.0D - (var7 + var9);
      var4.weightx = var7;
      gridBagLayout.setConstraints(this.permPanel, var4);
      var4.weightx = var9;
      gridBagLayout.setConstraints(this.oldPanel, var4);
      var4.weightx = var11;
      var4.gridwidth = 0;
      gridBagLayout.setConstraints(this.newPanel, var4);
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
      Color color = Color.GRAY;
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

   public static void main(String[] args) {
      JFrame frame = new JFrame();
      JBTextArea textArea = new JBTextArea() {
         public void setForeground(Color fg) {
            Color oldFg = Color.red;
            super.setForeground(oldFg);
         }
      };
      textArea.setCaretColor(JBColor.red);
      textArea.setForeground(JBColor.GRAY);
      textArea.setRows(8);
      textArea.setLineWrap(true);
      textArea.setText("Hello");
      frame.getContentPane().add(textArea);
      frame.pack();
      frame.setVisible(true);
   }

}
