package com.sun.jvmstat.tools.visualgc;

import com.sun.jvmstat.graph.FIFOList;
import com.sun.jvmstat.graph.Line;
import com.sun.jvmstat.util.Converter;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
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
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

class GraphGC extends JFrame
  implements ActionListener, ComponentListener
{
  public JPanel heapPanel;
  public JPanel timePanel;
  public JPanel gcPanel;
  public JPanel classPanel;
  public JPanel compilePanel;
  public JPanel finalizerPanel;
  public JPanel finalizerQPanel;
  private GCSpacePanel permPanel;
  private GCSpacePanel oldPanel;
  private GCSpacePanel edenPanel;
  private GCSpacePanel s0Panel;
  private GCSpacePanel s1Panel;
  public FIFOList gcActiveDataSet;
  public FIFOList finalizerActiveDataSet;
  public FIFOList finalizerQLengthDataSet;
  public FIFOList compilerActiveDataSet;
  public FIFOList classLoaderActiveDataSet;
  private GCSample previousSample;
  private boolean inGC = false;
  private boolean inEdGC = false;
  private boolean inTnGC = false;
  private long edenGCStart;
  private long tenuredGCStart;
  private long maxFinalizerQLength;
  private boolean inCL = false;
  private long clStart;
  private boolean inComp = false;
  private long compStart;
  private boolean run;

  public GraphGC(GCSample paramGCSample)
  {
    this.previousSample = paramGCSample;
    this.edenGCStart = paramGCSample.edenGCTime;
    this.tenuredGCStart = paramGCSample.tenuredGCTime;

    setTitle("Graph");

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent paramWindowEvent) {
        Window localWindow = paramWindowEvent.getWindow();
        localWindow.dispose();
        localWindow.hide();
      }
    });
    this.heapPanel = new JPanel();
    this.heapPanel.setBackground(Color.BLACK);

    Font localFont = new Font("Dialog", 1, 12);

    this.gcPanel = new JPanel();
    this.gcPanel.setBackground(Color.BLACK);
    this.gcPanel.setLayout(new GridLayout(1, 1));
    Color localColor1 = Color.getColor("graphgc.gc.color", Color.YELLOW);
    this.gcActiveDataSet = new FIFOList(1000, 0.0D, 1.0D);
    Line localLine = new Line(this.gcActiveDataSet, localColor1);
    Border localBorder = BorderFactory.createEtchedBorder(localColor1, Color.GRAY);
    TitledBorder localTitledBorder = BorderFactory.createTitledBorder(localBorder, "", 0, 0, localFont, localColor1);

    this.gcPanel.setBorder(localTitledBorder);
    this.gcPanel.add(localLine);

    this.classPanel = new JPanel();
    this.classPanel.setBackground(Color.BLACK);
    this.classPanel.setLayout(new GridLayout(1, 1));
    Color localColor2 = Color.getColor("graphgc.class.color", Color.CYAN);
    this.classLoaderActiveDataSet = new FIFOList(1000, 0.0D, 1.0D);
    localLine = new Line(this.classLoaderActiveDataSet, localColor2);
    localBorder = BorderFactory.createEtchedBorder(localColor2, Color.GRAY);
    localTitledBorder = BorderFactory.createTitledBorder(localBorder, "", 0, 0, localFont, localColor2);

    this.classPanel.setBorder(localTitledBorder);
    this.classPanel.add(localLine);

    this.compilePanel = new JPanel();
    this.compilePanel.setBackground(Color.BLACK);
    this.compilePanel.setLayout(new GridLayout(1, 1));
    Color localColor3 = Color.getColor("graphgc.compile.color", Color.WHITE);
    this.compilerActiveDataSet = new FIFOList(1000, 0.0D, 1.0D);
    localLine = new Line(this.compilerActiveDataSet, localColor3);
    localBorder = BorderFactory.createEtchedBorder(localColor3, Color.GRAY);
    localTitledBorder = BorderFactory.createTitledBorder(localBorder, "", 0, 0, localFont, localColor3);

    this.compilePanel.setBorder(localTitledBorder);
    this.compilePanel.add(localLine);

    this.finalizerPanel = new JPanel();
    this.finalizerPanel.setBackground(Color.BLACK);
    this.finalizerPanel.setLayout(new GridLayout(1, 1));
    Color localColor4 = Color.getColor("graphgc.finalizer.color", Color.WHITE);

    this.finalizerActiveDataSet = new FIFOList(1000, 0.0D, 1.0D);
    localLine = new Line(this.finalizerActiveDataSet, localColor4);
    localBorder = BorderFactory.createEtchedBorder(localColor4, Color.GRAY);
    localTitledBorder = BorderFactory.createTitledBorder(localBorder, "", 0, 0, localFont, localColor4);

    this.finalizerPanel.setBorder(localTitledBorder);
    this.finalizerPanel.add(localLine);

    this.finalizerQPanel = new JPanel();
    this.finalizerQPanel.setBackground(Color.BLACK);
    this.finalizerQPanel.setLayout(new GridLayout(1, 1));
    this.finalizerQLengthDataSet = new FIFOList(1000);
    localLine = new Line(this.finalizerQLengthDataSet, localColor4);
    localBorder = BorderFactory.createEtchedBorder(localColor4, Color.GRAY);
    localTitledBorder = BorderFactory.createTitledBorder(localBorder, "", 0, 0, localFont, localColor4);

    this.finalizerQPanel.setBorder(localTitledBorder);
    this.finalizerQPanel.add(localLine);

    this.timePanel = new JPanel();
    this.timePanel.setBackground(Color.BLACK);
    this.timePanel.setLayout(new GridLayout(5, 1));
    if (paramGCSample.finalizerInitialized) {
      this.timePanel.setLayout(new GridLayout(5, 1));
      this.timePanel.add(this.finalizerQPanel);
      this.timePanel.add(this.finalizerPanel);
    }
    else {
      this.timePanel.setLayout(new GridLayout(3, 1));
    }
    this.timePanel.add(this.compilePanel);
    this.timePanel.add(this.classPanel);
    this.timePanel.add(this.gcPanel);

    Color localColor5 = Color.getColor("eden.color", new Color(255, 150, 0));
    this.edenPanel = new GCSpacePanel("Eden Space", paramGCSample.edenSize, paramGCSample.edenCapacity, localColor5);

    Color localColor6 = Color.getColor("survivor.color", new Color(255, 204, 102));

    this.s0Panel = new GCSpacePanel("Survivor 0", paramGCSample.survivor0Size, paramGCSample.survivor0Capacity, localColor6);

    this.s1Panel = new GCSpacePanel("Survivor 1", paramGCSample.survivor1Size, paramGCSample.survivor1Capacity, localColor6);

    Color localColor7 = Color.getColor("old.color", new Color(204, 102, 0));
    this.oldPanel = new GCSpacePanel("Old Gen", paramGCSample.tenuredSize, paramGCSample.tenuredCapacity, localColor7);

    Color localColor8 = Color.getColor("perm.color", new Color(240, 200, 150));
    this.permPanel = new GCSpacePanel("Perm Gen", paramGCSample.permSize, paramGCSample.permCapacity, localColor8);

    addComponentListener(this);

    update(paramGCSample);
    resetPanel(paramGCSample);
  }

  public void componentHidden(ComponentEvent paramComponentEvent) {
  }

  public void componentMoved(ComponentEvent paramComponentEvent) {
  }

  public void componentShown(ComponentEvent paramComponentEvent) {
  }

  public void componentResized(ComponentEvent paramComponentEvent) {
    resetPanel(this.previousSample);
    validate();
  }

  public boolean getRun() {
    return this.run;
  }

  public void setRun(boolean paramBoolean) {
    this.run = paramBoolean;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    if (paramActionEvent.getActionCommand().equals("Close")) {
      dispose();
      hide();
      setRun(false);
    }
  }

  public void update(GCSample paramGCSample)
  {
    if (paramGCSample.lastModificationTime != this.previousSample.lastModificationTime) {
      resetPanel(paramGCSample);
      validate();
    }

    updateGraph(paramGCSample);

    updateTextComponents(paramGCSample);

    refreshPanels();

    this.previousSample = paramGCSample;
  }

  private void refreshPanels() {
    this.heapPanel.repaint();
    this.timePanel.repaint();
  }

  private void updateGraph(GCSample paramGCSample)
  {
    this.permPanel.updateGraph(paramGCSample.permSize, paramGCSample.permCapacity, paramGCSample.permUsed);
    this.oldPanel.updateGraph(paramGCSample.tenuredSize, paramGCSample.tenuredCapacity, paramGCSample.tenuredUsed);
    this.edenPanel.updateGraph(paramGCSample.edenSize, paramGCSample.edenCapacity, paramGCSample.edenUsed, paramGCSample.newGenMaxSize - paramGCSample.newGenCurSize);

    this.s0Panel.updateGraph(paramGCSample.survivor0Size, paramGCSample.survivor0Capacity, paramGCSample.survivor0Used);

    this.s1Panel.updateGraph(paramGCSample.survivor1Size, paramGCSample.survivor1Capacity, paramGCSample.survivor1Used);

    long l = 0L;
    if (!this.inGC) {
      this.inEdGC = (paramGCSample.edenGCEvents != this.previousSample.edenGCEvents);
      this.inTnGC = (paramGCSample.tenuredGCEvents != this.previousSample.tenuredGCEvents);

      if ((this.inEdGC) || (this.inTnGC))
      {
        this.inGC = true;

        if (this.inEdGC) this.edenGCStart = this.previousSample.edenGCTime;
        if (this.inTnGC) this.tenuredGCStart = this.previousSample.tenuredGCTime;
      }
    }

    if (this.inGC)
    {
      l = 1L;

      if ((this.inEdGC) && (paramGCSample.edenGCTime != this.edenGCStart)) {
        this.inEdGC = false;
      }

      if ((this.inTnGC) && (paramGCSample.tenuredGCTime != this.tenuredGCStart)) {
        this.inTnGC = false;
      }

      this.inGC = ((this.inEdGC) || (this.inTnGC));
    }
    this.gcActiveDataSet.add(new Double(l));

    this.finalizerQLengthDataSet.add(new Double(paramGCSample.finalizerQLength));

    int i = paramGCSample.finalizerTime - this.previousSample.finalizerTime == 0L ? 0 : 1;

    this.finalizerActiveDataSet.add(new Double(i));

    int j = paramGCSample.classLoadTime - this.previousSample.classLoadTime == 0L ? 0 : 1;

    this.classLoaderActiveDataSet.add(new Double(j));

    int k = paramGCSample.totalCompileTime - this.previousSample.totalCompileTime == 0L ? 0 : 1;

    this.compilerActiveDataSet.add(new Double(k));
  }

  private void updateTextComponents(GCSample paramGCSample)
  {
    this.maxFinalizerQLength = Math.max(this.maxFinalizerQLength, paramGCSample.finalizerQLength);

    TitledBorder localTitledBorder = (TitledBorder)this.finalizerQPanel.getBorder();
    String str = "Finalizer Queue Length: Maximum " + paramGCSample.finalizerQMaxLength + " Current " + paramGCSample.finalizerQLength + " Local Maximum " + this.maxFinalizerQLength;

    localTitledBorder.setTitle(str);

    localTitledBorder = (TitledBorder)this.finalizerPanel.getBorder();
    str = "Finalizer Time: " + paramGCSample.finalizerCount + " objects - " + Converter.longToTimeString(paramGCSample.finalizerTime, GCSample.osFrequency);
    localTitledBorder.setTitle(str);

    localTitledBorder = (TitledBorder)this.compilePanel.getBorder();
    str = "Compile Time: " + paramGCSample.totalCompile + " compiles - " + Converter.longToTimeString(paramGCSample.totalCompileTime, GCSample.osFrequency);
    localTitledBorder.setTitle(str);

    localTitledBorder = (TitledBorder)this.classPanel.getBorder();
    str = "Class Loader Time: " + paramGCSample.classesLoaded + " loaded, " + paramGCSample.classesUnloaded + " unloaded - " + Converter.longToTimeString(paramGCSample.classLoadTime, GCSample.osFrequency);
    localTitledBorder.setTitle(str);

    localTitledBorder = (TitledBorder)this.gcPanel.getBorder();
    str = "GC Time: " + (paramGCSample.edenGCEvents + paramGCSample.tenuredGCEvents) + " collections, " + Converter.longToTimeString(paramGCSample.edenGCTime + paramGCSample.tenuredGCTime, GCSample.osFrequency);
    if ((paramGCSample.lastGCCause != null) && (paramGCSample.lastGCCause.length() != 0))
    {
      str = str + " Last Cause: " + paramGCSample.lastGCCause;
    }
    localTitledBorder.setTitle(str);

    this.permPanel.updateTextComponents(paramGCSample.permCapacity, paramGCSample.permUsed);

    this.oldPanel.updateTextComponents(paramGCSample.tenuredCapacity, paramGCSample.tenuredUsed, paramGCSample.tenuredGCEvents, paramGCSample.tenuredGCTime, GCSample.osFrequency);

    this.edenPanel.updateTextComponents(paramGCSample.edenCapacity, paramGCSample.edenUsed, paramGCSample.edenGCEvents, paramGCSample.edenGCTime, GCSample.osFrequency);

    this.s0Panel.updateTextComponents(paramGCSample.survivor0Capacity, paramGCSample.survivor0Used);

    this.s1Panel.updateTextComponents(paramGCSample.survivor1Capacity, paramGCSample.survivor1Used);
  }

  public void resetPanel(GCSample paramGCSample)
  {
    Container localContainer = getContentPane();
    localContainer.removeAll();
    this.heapPanel.removeAll();
    this.timePanel.removeAll();

    GridBagLayout localGridBagLayout1 = new GridBagLayout();
    this.heapPanel.setLayout(localGridBagLayout1);

    GridBagConstraints localGridBagConstraints1 = new GridBagConstraints();
    localGridBagConstraints1.fill = 1;
    localGridBagConstraints1.gridwidth = 0;
    localGridBagConstraints1.weightx = 1.0D;

    double d1 = 0.3D;
    double d2 = 0.1D;
    double d3 = 0.1D;
    double d4 = 0.3D;
    double d5 = 0.2D;

    localGridBagConstraints1.weighty = d5;
    localGridBagLayout1.setConstraints(this.permPanel, localGridBagConstraints1);

    localGridBagConstraints1.weighty = d4;
    localGridBagLayout1.setConstraints(this.oldPanel, localGridBagConstraints1);

    localGridBagConstraints1.weighty = d1;
    localGridBagLayout1.setConstraints(this.edenPanel, localGridBagConstraints1);

    localGridBagConstraints1.weighty = d2;
    localGridBagLayout1.setConstraints(this.s0Panel, localGridBagConstraints1);

    localGridBagConstraints1.weighty = d3;
    localGridBagLayout1.setConstraints(this.s1Panel, localGridBagConstraints1);

    this.heapPanel.add(this.edenPanel);
    this.heapPanel.add(this.s0Panel);
    this.heapPanel.add(this.s1Panel);
    this.heapPanel.add(this.oldPanel);
    this.heapPanel.add(this.permPanel);

    double d6 = 0.0D;
    double d7 = 0.0D;
    double d8 = 0.0D;
    double d9 = 0.0D;
    double d10 = 0.0D;

    if (paramGCSample.finalizerInitialized) {
      d6 = 0.2D;
      d7 = 0.2D;
      d8 = 0.2D;
      d9 = 0.2D;
      d10 = 0.2D;

      this.timePanel.setLayout(new GridLayout(5, 1));

      localGridBagConstraints1.weighty = d6;
      localGridBagLayout1.setConstraints(this.finalizerQPanel, localGridBagConstraints1);

      localGridBagConstraints1.weighty = d7;
      localGridBagLayout1.setConstraints(this.finalizerPanel, localGridBagConstraints1);

      localGridBagConstraints1.weighty = d8;
      localGridBagLayout1.setConstraints(this.compilePanel, localGridBagConstraints1);

      localGridBagConstraints1.weighty = d9;
      localGridBagLayout1.setConstraints(this.classPanel, localGridBagConstraints1);

      localGridBagConstraints1.weighty = d10;
      localGridBagLayout1.setConstraints(this.gcPanel, localGridBagConstraints1);

      this.timePanel.add(this.finalizerQPanel);
      this.timePanel.add(this.finalizerPanel);
    }
    else {
      d6 = 0.0D;
      d7 = 0.0D;
      d8 = 0.33D;
      d9 = 0.33D;
      d10 = 0.34D;

      this.timePanel.setLayout(new GridLayout(3, 1));
    }

    this.timePanel.add(this.compilePanel);
    this.timePanel.add(this.classPanel);
    this.timePanel.add(this.gcPanel);

    localContainer = getContentPane();
    GridBagLayout localGridBagLayout2 = new GridBagLayout();
    GridBagConstraints localGridBagConstraints2 = new GridBagConstraints();

    localContainer.setLayout(localGridBagLayout2);
    localGridBagConstraints2.fill = 1;
    localGridBagConstraints2.gridwidth = 0;
    localGridBagConstraints2.weighty = 0.2D;
    localGridBagConstraints2.weightx = 1.0D;

    localGridBagLayout2.setConstraints(this.timePanel, localGridBagConstraints2);

    localGridBagConstraints2.gridheight = 0;
    localGridBagConstraints2.weighty = 0.8D;
    localGridBagConstraints2.weightx = 1.0D;

    localGridBagLayout2.setConstraints(this.heapPanel, localGridBagConstraints2);

    localContainer.add(this.timePanel);
    localContainer.add(this.heapPanel);
  }
}