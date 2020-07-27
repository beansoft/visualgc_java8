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

class VisualHeap extends JFrame
  implements ActionListener, ComponentListener
{
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

  public VisualHeap(GraphGC paramGraphGC, VisualAgeHistogram paramVisualAgeHistogram, GCSample paramGCSample)
  {
    this.previousSample = paramGCSample;
    this.g = paramGraphGC;
    this.a = paramVisualAgeHistogram;

    setTitle("VisualGC 3.0");

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent paramWindowEvent) {
        Window localWindow = paramWindowEvent.getWindow();
        localWindow.dispose();
        System.exit(0);
      }
    });
    this.infoPanel = new JPanel();
    this.infoPanel.setBackground(Color.BLACK);
    this.infoPanel.setLayout(new BorderLayout());
    Border localBorder1 = BorderFactory.createEtchedBorder(Color.WHITE, Color.GRAY);
    Font localFont = new Font("Dialog", 1, 12);
    String str = "Application Information";
    TitledBorder localTitledBorder = BorderFactory.createTitledBorder(localBorder1, str, 0, 0, localFont, Color.WHITE);

    this.infoPanel.setBorder(localTitledBorder);
    this.infoPanel.addComponentListener(this);

    this.livenessPanel = new JPanel();
    this.livenessPanel.setBackground(Color.BLACK);
    this.livenessPanel.setLayout(new FlowLayout());
    this.livenessPanel.addComponentListener(this);

    this.oldPanel = new JPanel();
    this.oldPanel.setBackground(Color.BLACK);
    this.oldPanel.setLayout(new GridLayout(1, 1));
    Color localColor1 = Color.getColor("old.color", new Color(204, 102, 0));

    this.oldLevel = new Level(localColor1);
    localBorder1 = BorderFactory.createEtchedBorder(localColor1, Color.GRAY);
    str = "Old";
    localTitledBorder = BorderFactory.createTitledBorder(localBorder1, str, 0, 0, localFont, localColor1);

    this.oldPanel.setBorder(localTitledBorder);
    this.oldPanel.addComponentListener(this);
    this.oldPanel.add(this.oldLevel);

    this.permPanel = new JPanel();
    this.permPanel.setBackground(Color.BLACK);
    this.permPanel.setLayout(new GridLayout(1, 1));
    Color localColor2 = Color.getColor("perm.color", new Color(240, 200, 150));
    this.permLevel = new Level(localColor2);
    localBorder1 = BorderFactory.createEtchedBorder(localColor2, Color.GRAY);
    str = "Perm";
    localTitledBorder = BorderFactory.createTitledBorder(localBorder1, str, 0, 0, localFont, localColor2);

    this.permPanel.setBorder(localTitledBorder);
    this.permPanel.addComponentListener(this);
    this.permPanel.add(this.permLevel);

    this.edenPanel = new JPanel();
    this.edenPanel.setBackground(Color.BLACK);
    this.edenPanel.setLayout(new GridLayout(1, 1));
    Color localColor3 = Color.getColor("eden.color", new Color(255, 150, 0));
    this.edenLevel = new Level(localColor3);
    localBorder1 = BorderFactory.createEtchedBorder(localColor3, Color.GRAY);
    str = "Eden";
    localTitledBorder = BorderFactory.createTitledBorder(localBorder1, str, 0, 0, localFont, localColor3);

    this.edenPanel.setBorder(localTitledBorder);
    this.edenPanel.addComponentListener(this);
    this.edenPanel.add(this.edenLevel);

    Color localColor4 = Color.getColor("survivor.color", new Color(255, 204, 102));

    this.s0Panel = new JPanel();
    this.s0Panel.setBackground(Color.BLACK);
    this.s0Panel.setLayout(new GridLayout(1, 1));
    this.s0Level = new Level(localColor4);
    localBorder1 = BorderFactory.createEtchedBorder(localColor4, Color.GRAY);
    str = "S0";
    localTitledBorder = BorderFactory.createTitledBorder(localBorder1, str, 0, 0, localFont, localColor4);

    this.s0Panel.setBorder(localTitledBorder);
    this.s0Panel.addComponentListener(this);
    this.s0Panel.add(this.s0Level);

    this.s1Panel = new JPanel();
    this.s1Panel.setBackground(Color.BLACK);
    this.s1Panel.setLayout(new GridLayout(1, 1));
    this.s1Level = new Level(localColor4);
    localBorder1 = BorderFactory.createEtchedBorder(localColor4, Color.GRAY);
    str = "S1";
    localTitledBorder = BorderFactory.createTitledBorder(localBorder1, str, 0, 0, localFont, localColor4);

    this.s1Panel.setBorder(localTitledBorder);
    this.s1Panel.addComponentListener(this);
    this.s1Panel.add(this.s1Level);

    Border localBorder2 = BorderFactory.createEmptyBorder();
    this.newPanel = new JPanel();
    this.newPanel.setBackground(Color.BLACK);
    this.newPanel.setBorder(localBorder2);

    this.spacesPanel = new JPanel();
    this.spacesPanel.setBackground(Color.BLACK);
    this.spacesPanel.setBorder(localBorder2);

    initializeInfoPanel(paramGCSample);
    resetPanel(paramGCSample);
  }

  private void initializeInfoPanel(GCSample paramGCSample)
  {
    Font localFont = new Font("Dialog", 1, 12);

    this.livenessIndicator = new JLabel("Alive    ");
    this.livenessIndicator.setFont(localFont);
    this.livenessIndicator.setForeground(Color.green);

    JLabel localJLabel = new JLabel("Elapsed Time: ");
    localJLabel.setFont(localFont);
    localJLabel.setForeground(Color.WHITE);
    this.etField = new JLabel(Converter.longToTimeString(paramGCSample.osElapsedTime, GCSample.osFrequency));

    this.etField.setFont(localFont);
    this.etField.setForeground(Color.WHITE);
    localJLabel.setLabelFor(this.etField);

    JTextArea localJTextArea = new JTextArea();
    localJTextArea.setFont(localFont);
    localJTextArea.setForeground(Color.WHITE);
    localJTextArea.setBackground(Color.BLACK);
    localJTextArea.append("Java Command Line: " + GCSample.javaCommand + "\n\n");
    localJTextArea.append("Java VM Arguments: " + GCSample.vmArgs + "\n\n");
    localJTextArea.append("Java VM Flags: " + GCSample.vmFlags + "\n\n");
    localJTextArea.append("java.home=" + GCSample.javaHome + "\n\n");
    localJTextArea.append("java.class.path=" + GCSample.classPath + "\n\n");
    localJTextArea.append("java.library.path=" + GCSample.libraryPath + "\n\n");
    localJTextArea.append("java.endorsed.dirs=" + GCSample.endorsedDirs + "\n\n");
    localJTextArea.append("java.ext.dirs=" + GCSample.extDirs + "\n\n");
    localJTextArea.append("sun.boot.class.path=" + GCSample.bootClassPath + "\n\n");
    localJTextArea.append("sun.boot.library.path=" + GCSample.bootLibraryPath + "\n\n");
    localJTextArea.append("java.vm.name=" + GCSample.vmName + "\n\n");
    localJTextArea.append("java.vm.info=" + GCSample.vmInfo + "\n\n");
    localJTextArea.append("java.vm.vendor=" + GCSample.vmVendor + "\n\n");
    localJTextArea.append("java.vm.version=" + GCSample.vmVersion + "\n\n");
    localJTextArea.append("java.vm.specification.name=" + GCSample.vmSpecName + "\n\n");
    localJTextArea.append("java.vm.specification.vendor=" + GCSample.vmSpecVendor + "\n\n");
    localJTextArea.append("java.vm.specification.version=" + GCSample.vmSpecVersion + "\n\n");
    localJTextArea.setRows(4);
    localJTextArea.setLineWrap(true);
    JScrollPane localJScrollPane = new JScrollPane(localJTextArea);

    updateTextFields(paramGCSample);

    this.livenessPanel.add(this.livenessIndicator);
    this.livenessPanel.add(localJLabel);
    this.livenessPanel.add(this.etField);

    this.infoPanel.add(this.livenessPanel, "North");
    this.infoPanel.add(localJScrollPane, "Center");
  }

  private void buildStackedSurvivors(GCSample paramGCSample)
  {
    GridBagLayout localGridBagLayout = new GridBagLayout();
    this.newPanel.setLayout(localGridBagLayout);

    GridBagConstraints localGridBagConstraints = new GridBagConstraints();
    localGridBagConstraints.fill = 1;
    localGridBagConstraints.gridwidth = 0;

    double d1 = paramGCSample.survivor0Capacity / paramGCSample.newGenMaxSize;
    double d2 = paramGCSample.survivor1Capacity / paramGCSample.newGenMaxSize;
    double d3 = 1.0D - (d1 + d2);

    localGridBagConstraints.weightx = 1.0D;
    localGridBagConstraints.weighty = d3;
    localGridBagLayout.setConstraints(this.edenPanel, localGridBagConstraints);

    localGridBagConstraints.weighty = d1;
    localGridBagLayout.setConstraints(this.s0Panel, localGridBagConstraints);

    localGridBagConstraints.weighty = d2;
    localGridBagConstraints.gridheight = 0;
    localGridBagLayout.setConstraints(this.s1Panel, localGridBagConstraints);

    this.newPanel.add(this.edenPanel);
    this.newPanel.add(this.s0Panel);
    this.newPanel.add(this.s1Panel);
  }

  private void resetPanel(GCSample paramGCSample)
  {
    Container localContainer = getContentPane();
    localContainer.removeAll();
    this.newPanel.removeAll();
    this.spacesPanel.removeAll();

    buildStackedSurvivors(paramGCSample);

    GridBagLayout localGridBagLayout = new GridBagLayout();
    this.spacesPanel.setLayout(localGridBagLayout);

    GridBagConstraints localGridBagConstraints = new GridBagConstraints();
    localGridBagConstraints.fill = 1;
    localGridBagConstraints.gridheight = 0;
    localGridBagConstraints.weighty = 1.0D;

    double d1 = paramGCSample.permSize + paramGCSample.tenuredSize + paramGCSample.newGenMaxSize;
    double d2 = paramGCSample.permSize / d1;
    double d3 = paramGCSample.tenuredSize / d1;
    double d4 = 1.0D - (d2 + d3);

    localGridBagConstraints.weightx = d2;
    localGridBagLayout.setConstraints(this.permPanel, localGridBagConstraints);

    localGridBagConstraints.weightx = d3;
    localGridBagLayout.setConstraints(this.oldPanel, localGridBagConstraints);

    localGridBagConstraints.weightx = d4;
    localGridBagConstraints.gridwidth = 0;
    localGridBagLayout.setConstraints(this.newPanel, localGridBagConstraints);

    this.spacesPanel.add(this.permPanel);
    this.spacesPanel.add(this.oldPanel);
    this.spacesPanel.add(this.newPanel);

    localContainer.add(this.infoPanel, "North");
    localContainer.add(this.spacesPanel, "Center");
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
    this.g.validate();
    if (this.a != null) this.a.validate();
  }

  private void resetSpace(GCSample paramGCSample)
  {
    resetPanel(paramGCSample);
    this.g.resetPanel(paramGCSample);
    if (this.a != null) this.a.resetPanel(paramGCSample); 
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
  }

  public void update(GCSample paramGCSample)
  {
    if (paramGCSample.heapSizeChanged(this.previousSample))
    {
      resetSpace(paramGCSample);
    }

    updateLevel(paramGCSample);
    updateTextFields(paramGCSample);
    refreshPanels();
    this.previousSample = paramGCSample;
  }

  public void refreshPanels() {
    repaint();
  }

  public void updateLevel(GCSample paramGCSample)
  {
    this.permLevel.updateLevel(paramGCSample.getPermLiveRatio());
    this.permLevel.updateGrayLevel(1.0D - paramGCSample.getPermCommittedRatio());

    this.oldLevel.updateLevel(paramGCSample.getTenuredLiveRatio());
    this.oldLevel.updateGrayLevel(1.0D - paramGCSample.getTenuredCommittedRatio());

    double d1 = paramGCSample.getAdjustedEdenSize();
    double d2 = paramGCSample.edenUsed;
    double d3 = paramGCSample.edenCapacity;
    this.edenLevel.updateGrayLevel(1.0D - d3 / d1);
    this.edenLevel.updateLevel(d2 / d1);

    double d4 = paramGCSample.survivor0Used;
    double d5 = paramGCSample.survivor0Capacity;
    this.s0Level.updateLevel(d4 / d5);

    double d6 = paramGCSample.survivor1Used;
    double d7 = paramGCSample.survivor1Capacity;
    this.s1Level.updateLevel(d6 / d7);
  }

  public void updateTextFields(GCSample paramGCSample)
  {
    Color localColor = Color.WHITE;
    String str = "Alive     ";

    if (paramGCSample != this.previousSample) {
      if (paramGCSample.osElapsedTime == this.previousSample.osElapsedTime)
      {
        if (this.livenessIndicator.getForeground() != Color.red) {
          localColor = Color.red;
        }

        str = "Dead     ";
      }
      else if (this.livenessIndicator.getForeground() != Color.green) {
        localColor = Color.green;
      }
    }

    this.livenessIndicator.setForeground(localColor);
    this.livenessIndicator.setText(str);

    this.etField.setText(Converter.longToTimeString(paramGCSample.osElapsedTime, GCSample.osFrequency));
  }

  public void update(Graphics paramGraphics)
  {
    paint(paramGraphics);
  }

  public void draw() {
    repaint();
  }

  public void paint(Graphics paramGraphics) {
    super.paint(paramGraphics);
  }
}