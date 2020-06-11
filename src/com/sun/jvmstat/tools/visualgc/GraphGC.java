package com.sun.jvmstat.tools.visualgc;

import com.sun.jvmstat.graph.FIFOList;
import com.sun.jvmstat.graph.Line;
import com.sun.jvmstat.tools.visualgc.resource.Res;
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
import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

class GraphGC extends JFrame implements ActionListener, ComponentListener {
   public JPanel heapPanel;
   public JPanel timePanel;
   public JPanel edenGcPanel;
   public JPanel edenGcTimePanel, tenuredGCTimePanel, stopGCTimePane;// eden, old, stop gc time pane
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
   public FIFOList edenGcTimeDataSet;// eden gc time dataset
   public FIFOList tenuredGCTimeDataSet;// tenure gc time dataset
   public FIFOList stopGCTimeDataSet;// tenure gc time dataset
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

   private long lastEdenGCTimeDelta = 0;
   private long lastOldGCTimeDelta = 0;

   public GraphGC(GCSample gcSample) {
      this.previousSample = gcSample;
      this.edenGCStart = gcSample.edenGCTime;
      this.tenuredGCStart = gcSample.tenuredGCTime;
      this.setTitle(Res.getString("graph"));
      this.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent windowEvent) {
            Window window = windowEvent.getWindow();
            window.dispose();
            window.hide();
         }
      });
      this.heapPanel = new JPanel();
      this.heapPanel.setBackground(Color.BLACK);
      Font font = new Font("Dialog", 1, 12);
      this.edenGcPanel = new JPanel();
      this.edenGcPanel.setBackground(Color.BLACK);
      this.edenGcPanel.setLayout(new GridLayout(1, 1));
      Color gcColor = Color.getColor("graphgc.gc.color", Color.YELLOW);
      this.gcActiveDataSet = new FIFOList(Line.DEFAULT_DATASET_SIZE, 0.0D, 1.0D);


      Line line = new Line(this.gcActiveDataSet, gcColor);
      Border border = BorderFactory.createEtchedBorder(gcColor, Color.GRAY);
      TitledBorder titledBorder = BorderFactory.createTitledBorder(border, "", 0, 0, font, gcColor);
      this.edenGcPanel.setBorder(titledBorder);
      this.edenGcPanel.add(line);

      edenGcTimePanel= new JPanel();
      this.edenGcTimePanel.setBackground(Color.BLACK);
      this.edenGcTimePanel.setLayout(new GridLayout(1, 1));
      this.edenGcTimeDataSet = new FIFOList(1000);
      Line edenGcTimeLine = new Line(this.edenGcTimeDataSet, gcColor);
      edenGcTimeLine.setTimeMode(true);
      border = BorderFactory.createEtchedBorder(gcColor, Color.GRAY);
      titledBorder = BorderFactory.createTitledBorder(border, "Eden GC Time", 0, 0, font, gcColor);
      this.edenGcTimePanel.setBorder(titledBorder);
      this.edenGcTimePanel.add(edenGcTimeLine);

      tenuredGCTimePanel= new JPanel();
      this.tenuredGCTimePanel.setBackground(Color.BLACK);
      this.tenuredGCTimePanel.setLayout(new GridLayout(1, 1));
      this.tenuredGCTimeDataSet = new FIFOList(1000);
      Line tenuredGcTimeLine = new Line(this.tenuredGCTimeDataSet, gcColor);
      tenuredGcTimeLine.setTimeMode(true);
      border = BorderFactory.createEtchedBorder(gcColor, Color.GRAY);
      titledBorder = BorderFactory.createTitledBorder(border, "Old GC Time", 0, 0, font, gcColor);
      this.tenuredGCTimePanel.setBorder(titledBorder);
      this.tenuredGCTimePanel.add(tenuredGcTimeLine);

      if(GCSample.collector1name == null) {
         tenuredGCTimePanel.setVisible(false);
      }

      stopGCTimePane= new JPanel();
      this.stopGCTimePane.setBackground(Color.BLACK);
      this.stopGCTimePane.setLayout(new GridLayout(1, 1));
      this.stopGCTimeDataSet = new FIFOList(1000);
      Line stopGcTimeLine = new Line(this.stopGCTimeDataSet, gcColor);
      stopGcTimeLine.setTimeMode(true);
      border = BorderFactory.createEtchedBorder(gcColor, Color.GRAY);
      titledBorder = BorderFactory.createTitledBorder(border, "Stop GC Time", 0, 0, font, gcColor);
      this.stopGCTimePane.setBorder(titledBorder);
      this.stopGCTimePane.add(stopGcTimeLine);


      this.classPanel = new JPanel();
      this.classPanel.setBackground(Color.BLACK);
      this.classPanel.setLayout(new GridLayout(1, 1));
      Color classColor = Color.getColor("graphgc.class.color", Color.CYAN);
      this.classLoaderActiveDataSet = new FIFOList(1000, 0.0D, 1.0D);
      line = new Line(this.classLoaderActiveDataSet, classColor);
      border = BorderFactory.createEtchedBorder(classColor, Color.GRAY);
      titledBorder = BorderFactory.createTitledBorder(border, "", 0, 0, font, classColor);
      this.classPanel.setBorder(titledBorder);
      this.classPanel.add(line);
      this.compilePanel = new JPanel();
      this.compilePanel.setBackground(Color.BLACK);
      this.compilePanel.setLayout(new GridLayout(1, 1));
      Color compileColor = Color.getColor("graphgc.compile.color", Color.WHITE);
      this.compilerActiveDataSet = new FIFOList(1000, 0.0D, 1.0D);
      line = new Line(this.compilerActiveDataSet, compileColor);
      border = BorderFactory.createEtchedBorder(compileColor, Color.GRAY);
      titledBorder = BorderFactory.createTitledBorder(border, "", 0, 0, font, compileColor);
      this.compilePanel.setBorder(titledBorder);
      this.compilePanel.add(line);
      this.finalizerPanel = new JPanel();
      this.finalizerPanel.setBackground(Color.BLACK);
      this.finalizerPanel.setLayout(new GridLayout(1, 1));
      Color finalizerColor = Color.getColor("graphgc.finalizer.color", Color.WHITE);
      this.finalizerActiveDataSet = new FIFOList(1000, 0.0D, 1.0D);
      line = new Line(this.finalizerActiveDataSet, finalizerColor);
      border = BorderFactory.createEtchedBorder(finalizerColor, Color.GRAY);
      titledBorder = BorderFactory.createTitledBorder(border, "", 0, 0, font, finalizerColor);
      this.finalizerPanel.setBorder(titledBorder);
      this.finalizerPanel.add(line);
      this.finalizerQPanel = new JPanel();
      this.finalizerQPanel.setBackground(Color.BLACK);
      this.finalizerQPanel.setLayout(new GridLayout(1, 1));
      this.finalizerQLengthDataSet = new FIFOList(1000);
      line = new Line(this.finalizerQLengthDataSet, finalizerColor);
      border = BorderFactory.createEtchedBorder(finalizerColor, Color.GRAY);
      titledBorder = BorderFactory.createTitledBorder(border, "", 0, 0, font, finalizerColor);
      this.finalizerQPanel.setBorder(titledBorder);
      this.finalizerQPanel.add(line);
      this.timePanel = new JPanel();
      this.timePanel.setBackground(Color.BLACK);
      this.timePanel.setLayout(new GridLayout(0, 1));
      if (gcSample.finalizerInitialized) {
         this.timePanel.setLayout(new GridLayout(0, 1));
         this.timePanel.add(this.finalizerQPanel);
         this.timePanel.add(this.finalizerPanel);
      } else {
         this.timePanel.setLayout(new GridLayout(0, 1));
      }

      this.timePanel.add(this.compilePanel);
      this.timePanel.add(this.classPanel);
      this.timePanel.add(this.edenGcPanel);

      if(GCSample.collector0name != null) {
         this.timePanel.add(this.edenGcTimePanel);
      }

      if(GCSample.collector1name != null) {
         this.timePanel.add(this.tenuredGCTimePanel);
      }
      if(GCSample.collector2name != null) {
         this.timePanel.add(this.stopGCTimePane);
      }


      Color edenColor = Color.getColor("eden.color", new Color(255, 150, 0));
      this.edenPanel = new GCSpacePanel(Res.getString("eden.space"), gcSample.edenSize, gcSample.edenCapacity, edenColor);
      Color survivorColor = Color.getColor("survivor.color", new Color(255, 204, 102));
      this.s0Panel = new GCSpacePanel(Res.getString("survivor.0"), gcSample.survivor0Size, gcSample.survivor0Capacity, survivorColor);
      this.s1Panel = new GCSpacePanel(Res.getString("survivor.1"), gcSample.survivor1Size, gcSample.survivor1Capacity, survivorColor);
      Color oldColor = Color.getColor("old.color", new Color(204, 102, 0));
      this.oldPanel = new GCSpacePanel(Res.getString("old.gen"), gcSample.tenuredSize, gcSample.tenuredCapacity, oldColor);
      Color permColor = Color.getColor("perm.color", new Color(240, 200, 150));
      this.permPanel = new GCSpacePanel(Res.getString("perm.gen"), gcSample.permSize, gcSample.permCapacity, permColor);
      this.addComponentListener(this);
      this.update(gcSample);
      this.resetPanel(gcSample);
   }

   public void componentHidden(ComponentEvent var1) {
   }

   public void componentMoved(ComponentEvent var1) {
   }

   public void componentShown(ComponentEvent var1) {
   }

   public void componentResized(ComponentEvent var1) {
      this.resetPanel(this.previousSample);
      this.validate();
   }

   public boolean getRun() {
      return this.run;
   }

   public void setRun(boolean var1) {
      this.run = var1;
   }

   public void actionPerformed(ActionEvent var1) {
      if (var1.getActionCommand().equals("Close")) {
         this.dispose();
         this.hide();
         this.setRun(false);
      }

   }

   public void update(GCSample gcSample) {
      if (gcSample.lastModificationTime != this.previousSample.lastModificationTime) {
         this.resetPanel(gcSample);
         this.validate();
      }

      this.updateGraph(gcSample);
      this.updateTextComponents(gcSample);
      this.refreshPanels();
      this.previousSample = gcSample;
   }

   private void refreshPanels() {
      this.heapPanel.repaint();
      this.timePanel.repaint();
   }

   private void updateGraph(GCSample gcSample) {
      this.permPanel.updateGraph(gcSample.permSize, gcSample.permCapacity, gcSample.permUsed);
      this.oldPanel.updateGraph(gcSample.tenuredSize, gcSample.tenuredCapacity, gcSample.tenuredUsed);
      this.edenPanel.updateGraph(gcSample.edenSize, gcSample.edenCapacity, gcSample.edenUsed, gcSample.newGenMaxSize - gcSample.newGenCurSize);
      this.s0Panel.updateGraph(gcSample.survivor0Size, gcSample.survivor0Capacity, gcSample.survivor0Used);
      this.s1Panel.updateGraph(gcSample.survivor1Size, gcSample.survivor1Capacity, gcSample.survivor1Used);
      long gcCount = 0L;
      long edenGCTimeDelta = 0L;
      long oldGCTimeDelta = 0L;

      if (!this.inGC) {
         this.inEdGC = gcSample.edenGCEvents != this.previousSample.edenGCEvents;
         this.inTnGC = gcSample.tenuredGCEvents != this.previousSample.tenuredGCEvents;
         if (this.inEdGC || this.inTnGC) {
            this.inGC = true;
            if (this.inEdGC) {
               this.edenGCStart = this.previousSample.edenGCTime;
            }

            if (this.inTnGC) {
               this.tenuredGCStart = this.previousSample.tenuredGCTime;
            }
         }
      }

      if (this.inGC) {
         gcCount = 1L;
         if (this.inEdGC && gcSample.edenGCTime != this.edenGCStart) {
//            System.out.println("edenGCTime Delta:" + (gcSample.edenGCTime - this.edenGCStart));
            edenGCTimeDelta = gcSample.edenGCTime - this.edenGCStart;
            lastEdenGCTimeDelta = edenGCTimeDelta;

            this.inEdGC = false;
         }

         if (this.inTnGC && gcSample.tenuredGCTime != this.tenuredGCStart) {
            oldGCTimeDelta = gcSample.tenuredGCTime - this.tenuredGCStart;

//            System.out.println("tenuredGCTime Delta:" + oldGCTimeDelta);
            lastOldGCTimeDelta = oldGCTimeDelta;
            this.inTnGC = false;
         }

         this.inGC = this.inEdGC || this.inTnGC;
      }

      this.gcActiveDataSet.add( gcCount);
      this.edenGcTimeDataSet.add(edenGCTimeDelta);
      this.tenuredGCTimeDataSet.add(oldGCTimeDelta);
      if(GCSample.collector2name != null) {
         this.stopGCTimeDataSet.add(gcSample.stopGCTime);
      }

      this.finalizerQLengthDataSet.add((double) gcSample.finalizerQLength);
      int var4 = gcSample.finalizerTime - this.previousSample.finalizerTime == 0L ? 0 : 1;
      this.finalizerActiveDataSet.add((double) var4);
      int var5 = gcSample.classLoadTime - this.previousSample.classLoadTime == 0L ? 0 : 1;
      this.classLoaderActiveDataSet.add((double) var5);
      int var6 = gcSample.totalCompileTime - this.previousSample.totalCompileTime == 0L ? 0 : 1;
      this.compilerActiveDataSet.add((double) var6);
   }

   private void updateTextComponents(GCSample gcSample) {
      this.maxFinalizerQLength = Math.max(this.maxFinalizerQLength, gcSample.finalizerQLength);
      TitledBorder titledBorder = (TitledBorder)this.finalizerQPanel.getBorder();
      String title = MessageFormat.format(Res.getString("finalizer.queue.length.maximum.0.current.1.local.maximum.2"), gcSample.finalizerQMaxLength, gcSample.finalizerQLength, this.maxFinalizerQLength);
      titledBorder.setTitle(title);
      titledBorder = (TitledBorder)this.finalizerPanel.getBorder();
      title = MessageFormat.format(Res.getString("finalizer.time.0.objects.1"), gcSample.finalizerCount, Converter.longToTimeString(gcSample.finalizerTime, GCSample.osFrequency));
      titledBorder.setTitle(title);
      titledBorder = (TitledBorder)this.compilePanel.getBorder();
      title = MessageFormat.format(Res.getString("compile.time.0.compiles.1"), gcSample.totalCompile, Converter.longToTimeString(gcSample.totalCompileTime, GCSample.osFrequency));
      titledBorder.setTitle(title);
      titledBorder = (TitledBorder)this.classPanel.getBorder();
      title = MessageFormat.format(Res.getString("class.loader.time.0.loaded.1.unloaded.2"), gcSample.classesLoaded, gcSample.classesUnloaded, Converter.longToTimeString(gcSample.classLoadTime, GCSample.osFrequency));
      titledBorder.setTitle(title);
      titledBorder = (TitledBorder)this.edenGcPanel.getBorder();
      title = MessageFormat.format(Res.getString("gc.time.0.collections.1"), gcSample.edenGCEvents + gcSample.tenuredGCEvents, Converter.longToTimeString(gcSample.edenGCTime + gcSample.tenuredGCTime, GCSample.osFrequency));
      if (gcSample.lastGCCause != null && gcSample.lastGCCause.length() != 0) {
         title = MessageFormat.format(Res.getString("0.last.cause.1"), title, gcSample.lastGCCause);
      }
      titledBorder.setTitle(title);

      titledBorder = (TitledBorder)this.edenGcTimePanel.getBorder();
      title = GCSample.collector0name + " Eden GC Time( Last: " + Converter.longToTimeString(lastEdenGCTimeDelta, GCSample.osFrequency) + " Max:" + Converter.longToTimeString(edenGcTimeDataSet.getMaxValue(), GCSample.osFrequency) + ")";
      titledBorder.setTitle(title);

      titledBorder = (TitledBorder)this.tenuredGCTimePanel.getBorder();
      title = GCSample.collector1name + " Tenured GC Time( Last: " + Converter.longToTimeString(lastOldGCTimeDelta, GCSample.osFrequency) + " Max:" + Converter.longToTimeString(this.tenuredGCTimeDataSet.getMaxValue(), GCSample.osFrequency) + ")";
      titledBorder.setTitle(title);

      if(GCSample.collector2name != null) {
         titledBorder = (TitledBorder)this.stopGCTimePane.getBorder();
         title = GCSample.collector2name + " Total Time: " + Converter.longToTimeString(gcSample.stopGCTime, GCSample.osFrequency) ;
         titledBorder.setTitle(title);
      }

      this.permPanel.updateTextComponents(gcSample.permCapacity, gcSample.permUsed);
      this.oldPanel.updateTextComponents(gcSample.tenuredCapacity, gcSample.tenuredUsed, gcSample.tenuredGCEvents, gcSample.tenuredGCTime, GCSample.osFrequency);
      this.edenPanel.updateTextComponents(gcSample.edenCapacity, gcSample.edenUsed, gcSample.edenGCEvents, gcSample.edenGCTime, GCSample.osFrequency);
      this.s0Panel.updateTextComponents(gcSample.survivor0Capacity, gcSample.survivor0Used);
      this.s1Panel.updateTextComponents(gcSample.survivor1Capacity, gcSample.survivor1Used);
   }

   public void resetPanel(GCSample gcSample) {
      Container container = this.getContentPane();
      container.removeAll();
      this.heapPanel.removeAll();
      this.timePanel.removeAll();
      GridBagLayout gridBagLayout1 = new GridBagLayout();
      this.heapPanel.setLayout(gridBagLayout1);
      GridBagConstraints gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.fill = 1;
      gridBagConstraints.gridwidth = 0;
      gridBagConstraints.weightx = 1.0D;
      double d1 = 0.3D;
      double d2 = 0.1D;
      double d3 = 0.1D;
      double d4 = 0.3D;
      double d5 = 0.2D;
      gridBagConstraints.weighty = d5;
      gridBagLayout1.setConstraints(this.permPanel, gridBagConstraints);
      gridBagConstraints.weighty = d4;
      gridBagLayout1.setConstraints(this.oldPanel, gridBagConstraints);
      gridBagConstraints.weighty = d1;
      gridBagLayout1.setConstraints(this.edenPanel, gridBagConstraints);
      gridBagConstraints.weighty = d2;
      gridBagLayout1.setConstraints(this.s0Panel, gridBagConstraints);
      gridBagConstraints.weighty = d3;
      gridBagLayout1.setConstraints(this.s1Panel, gridBagConstraints);
      this.heapPanel.add(this.edenPanel);
      this.heapPanel.add(this.s0Panel);
      this.heapPanel.add(this.s1Panel);
      this.heapPanel.add(this.oldPanel);
      this.heapPanel.add(this.permPanel);
      double var15 = 0.0D;
      double var17 = 0.0D;
      double var19 = 0.0D;
      double var21 = 0.0D;
      double var23 = 0.0D;
      if (gcSample.finalizerInitialized) {
         var15 = 0.2D;
         var17 = 0.2D;
         var19 = 0.2D;
         var21 = 0.2D;
         var23 = 0.2D;
         this.timePanel.setLayout(new GridLayout(5, 1));
         gridBagConstraints.weighty = var15;
         gridBagLayout1.setConstraints(this.finalizerQPanel, gridBagConstraints);
         gridBagConstraints.weighty = var17;
         gridBagLayout1.setConstraints(this.finalizerPanel, gridBagConstraints);
         gridBagConstraints.weighty = var19;
         gridBagLayout1.setConstraints(this.compilePanel, gridBagConstraints);
         gridBagConstraints.weighty = var21;
         gridBagLayout1.setConstraints(this.classPanel, gridBagConstraints);
         gridBagConstraints.weighty = var23;
         gridBagLayout1.setConstraints(this.edenGcPanel, gridBagConstraints);
         this.timePanel.add(this.finalizerQPanel);
         this.timePanel.add(this.finalizerPanel);
      } else {
         var15 = 0.0D;
         var17 = 0.0D;
         var19 = 0.33D;
         var21 = 0.33D;
         var23 = 0.34D;
         this.timePanel.setLayout(new GridLayout(0, 1));
      }

      this.timePanel.add(this.compilePanel);
      this.timePanel.add(this.classPanel);
      this.timePanel.add(this.edenGcPanel);
      if(GCSample.collector0name != null) {
         this.timePanel.add(this.edenGcTimePanel);
      }

      if(GCSample.collector1name != null) {
         this.timePanel.add(this.tenuredGCTimePanel);
      }
      if(GCSample.collector2name != null) {
         this.timePanel.add(this.stopGCTimePane);
      }

      container = this.getContentPane();
      GridBagLayout gridBagLayout = new GridBagLayout();
      GridBagConstraints mainPaneConstraints = new GridBagConstraints();
      container.setLayout(gridBagLayout);
      mainPaneConstraints.fill = 1;
      mainPaneConstraints.gridwidth = 0;
      mainPaneConstraints.weighty = 0.35D;
      mainPaneConstraints.weightx = 1.0D;
      gridBagLayout.setConstraints(this.timePanel, mainPaneConstraints);
      mainPaneConstraints.gridheight = 0;
      mainPaneConstraints.weighty = 0.65D;
      mainPaneConstraints.weightx = 1.0D;
      gridBagLayout.setConstraints(this.heapPanel, mainPaneConstraints);
      container.add(this.timePanel);
      container.add(this.heapPanel);
   }
}
