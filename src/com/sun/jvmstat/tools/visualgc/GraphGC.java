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
import java.awt.event.WindowEvent;import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

class GraphGC extends JFrame implements ActionListener, ComponentListener {
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
      Font var2 = new Font("Dialog", 1, 12);
      this.gcPanel = new JPanel();
      this.gcPanel.setBackground(Color.BLACK);
      this.gcPanel.setLayout(new GridLayout(1, 1));
      Color var5 = Color.getColor("graphgc.gc.color", Color.YELLOW);
      this.gcActiveDataSet = new FIFOList(1000, 0.0D, 1.0D);
      Line var6 = new Line(this.gcActiveDataSet, var5);
      Border var3 = BorderFactory.createEtchedBorder(var5, Color.GRAY);
      TitledBorder var4 = BorderFactory.createTitledBorder(var3, "", 0, 0, var2, var5);
      this.gcPanel.setBorder(var4);
      this.gcPanel.add(var6);
      this.classPanel = new JPanel();
      this.classPanel.setBackground(Color.BLACK);
      this.classPanel.setLayout(new GridLayout(1, 1));
      Color var7 = Color.getColor("graphgc.class.color", Color.CYAN);
      this.classLoaderActiveDataSet = new FIFOList(1000, 0.0D, 1.0D);
      var6 = new Line(this.classLoaderActiveDataSet, var7);
      var3 = BorderFactory.createEtchedBorder(var7, Color.GRAY);
      var4 = BorderFactory.createTitledBorder(var3, "", 0, 0, var2, var7);
      this.classPanel.setBorder(var4);
      this.classPanel.add(var6);
      this.compilePanel = new JPanel();
      this.compilePanel.setBackground(Color.BLACK);
      this.compilePanel.setLayout(new GridLayout(1, 1));
      Color var8 = Color.getColor("graphgc.compile.color", Color.WHITE);
      this.compilerActiveDataSet = new FIFOList(1000, 0.0D, 1.0D);
      var6 = new Line(this.compilerActiveDataSet, var8);
      var3 = BorderFactory.createEtchedBorder(var8, Color.GRAY);
      var4 = BorderFactory.createTitledBorder(var3, "", 0, 0, var2, var8);
      this.compilePanel.setBorder(var4);
      this.compilePanel.add(var6);
      this.finalizerPanel = new JPanel();
      this.finalizerPanel.setBackground(Color.BLACK);
      this.finalizerPanel.setLayout(new GridLayout(1, 1));
      Color var9 = Color.getColor("graphgc.finalizer.color", Color.WHITE);
      this.finalizerActiveDataSet = new FIFOList(1000, 0.0D, 1.0D);
      var6 = new Line(this.finalizerActiveDataSet, var9);
      var3 = BorderFactory.createEtchedBorder(var9, Color.GRAY);
      var4 = BorderFactory.createTitledBorder(var3, "", 0, 0, var2, var9);
      this.finalizerPanel.setBorder(var4);
      this.finalizerPanel.add(var6);
      this.finalizerQPanel = new JPanel();
      this.finalizerQPanel.setBackground(Color.BLACK);
      this.finalizerQPanel.setLayout(new GridLayout(1, 1));
      this.finalizerQLengthDataSet = new FIFOList(1000);
      var6 = new Line(this.finalizerQLengthDataSet, var9);
      var3 = BorderFactory.createEtchedBorder(var9, Color.GRAY);
      var4 = BorderFactory.createTitledBorder(var3, "", 0, 0, var2, var9);
      this.finalizerQPanel.setBorder(var4);
      this.finalizerQPanel.add(var6);
      this.timePanel = new JPanel();
      this.timePanel.setBackground(Color.BLACK);
      this.timePanel.setLayout(new GridLayout(5, 1));
      if (gcSample.finalizerInitialized) {
         this.timePanel.setLayout(new GridLayout(5, 1));
         this.timePanel.add(this.finalizerQPanel);
         this.timePanel.add(this.finalizerPanel);
      } else {
         this.timePanel.setLayout(new GridLayout(3, 1));
      }

      this.timePanel.add(this.compilePanel);
      this.timePanel.add(this.classPanel);
      this.timePanel.add(this.gcPanel);
      Color var10 = Color.getColor("eden.color", new Color(255, 150, 0));
      this.edenPanel = new GCSpacePanel(Res.getString("eden.space"), gcSample.edenSize, gcSample.edenCapacity, var10);
      Color var11 = Color.getColor("survivor.color", new Color(255, 204, 102));
      this.s0Panel = new GCSpacePanel(Res.getString("survivor.0"), gcSample.survivor0Size, gcSample.survivor0Capacity, var11);
      this.s1Panel = new GCSpacePanel(Res.getString("survivor.1"), gcSample.survivor1Size, gcSample.survivor1Capacity, var11);
      Color var12 = Color.getColor("old.color", new Color(204, 102, 0));
      this.oldPanel = new GCSpacePanel(Res.getString("old.gen"), gcSample.tenuredSize, gcSample.tenuredCapacity, var12);
      Color var13 = Color.getColor("perm.color", new Color(240, 200, 150));
      this.permPanel = new GCSpacePanel(Res.getString("perm.gen"), gcSample.permSize, gcSample.permCapacity, var13);
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

   public void update(GCSample var1) {
      if (var1.lastModificationTime != this.previousSample.lastModificationTime) {
         this.resetPanel(var1);
         this.validate();
      }

      this.updateGraph(var1);
      this.updateTextComponents(var1);
      this.refreshPanels();
      this.previousSample = var1;
   }

   private void refreshPanels() {
      this.heapPanel.repaint();
      this.timePanel.repaint();
   }

   private void updateGraph(GCSample var1) {
      this.permPanel.updateGraph(var1.permSize, var1.permCapacity, var1.permUsed);
      this.oldPanel.updateGraph(var1.tenuredSize, var1.tenuredCapacity, var1.tenuredUsed);
      this.edenPanel.updateGraph(var1.edenSize, var1.edenCapacity, var1.edenUsed, var1.newGenMaxSize - var1.newGenCurSize);
      this.s0Panel.updateGraph(var1.survivor0Size, var1.survivor0Capacity, var1.survivor0Used);
      this.s1Panel.updateGraph(var1.survivor1Size, var1.survivor1Capacity, var1.survivor1Used);
      long var2 = 0L;
      if (!this.inGC) {
         this.inEdGC = var1.edenGCEvents != this.previousSample.edenGCEvents;
         this.inTnGC = var1.tenuredGCEvents != this.previousSample.tenuredGCEvents;
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
         var2 = 1L;
         if (this.inEdGC && var1.edenGCTime != this.edenGCStart) {
            this.inEdGC = false;
         }

         if (this.inTnGC && var1.tenuredGCTime != this.tenuredGCStart) {
            this.inTnGC = false;
         }

         this.inGC = this.inEdGC || this.inTnGC;
      }

      this.gcActiveDataSet.add(new Double((double)var2));
      this.finalizerQLengthDataSet.add(new Double((double)var1.finalizerQLength));
      int var4 = var1.finalizerTime - this.previousSample.finalizerTime == 0L ? 0 : 1;
      this.finalizerActiveDataSet.add(new Double((double)var4));
      int var5 = var1.classLoadTime - this.previousSample.classLoadTime == 0L ? 0 : 1;
      this.classLoaderActiveDataSet.add(new Double((double)var5));
      int var6 = var1.totalCompileTime - this.previousSample.totalCompileTime == 0L ? 0 : 1;
      this.compilerActiveDataSet.add(new Double((double)var6));
   }

   private void updateTextComponents(GCSample var1) {
      this.maxFinalizerQLength = Math.max(this.maxFinalizerQLength, var1.finalizerQLength);
      TitledBorder var2 = (TitledBorder)this.finalizerQPanel.getBorder();
      String var3 = MessageFormat.format(Res.getString("finalizer.queue.length.maximum.0.current.1.local.maximum.2"), var1.finalizerQMaxLength, var1.finalizerQLength, this.maxFinalizerQLength);
      var2.setTitle(var3);
      var2 = (TitledBorder)this.finalizerPanel.getBorder();
      var3 = MessageFormat.format(Res.getString("finalizer.time.0.objects.1"), var1.finalizerCount, Converter.longToTimeString(var1.finalizerTime, GCSample.osFrequency));
      var2.setTitle(var3);
      var2 = (TitledBorder)this.compilePanel.getBorder();
      var3 = MessageFormat.format(Res.getString("compile.time.0.compiles.1"), var1.totalCompile, Converter.longToTimeString(var1.totalCompileTime, GCSample.osFrequency));
      var2.setTitle(var3);
      var2 = (TitledBorder)this.classPanel.getBorder();
      var3 = MessageFormat.format(Res.getString("class.loader.time.0.loaded.1.unloaded.2"), var1.classesLoaded, var1.classesUnloaded, Converter.longToTimeString(var1.classLoadTime, GCSample.osFrequency));
      var2.setTitle(var3);
      var2 = (TitledBorder)this.gcPanel.getBorder();
      var3 = MessageFormat.format(Res.getString("gc.time.0.collections.1"), var1.edenGCEvents + var1.tenuredGCEvents, Converter.longToTimeString(var1.edenGCTime + var1.tenuredGCTime, GCSample.osFrequency));
      if (var1.lastGCCause != null && var1.lastGCCause.length() != 0) {
         var3 = MessageFormat.format(Res.getString("0.last.cause.1"), var3, var1.lastGCCause);
      }

      var2.setTitle(var3);
      this.permPanel.updateTextComponents(var1.permCapacity, var1.permUsed);
      this.oldPanel.updateTextComponents(var1.tenuredCapacity, var1.tenuredUsed, var1.tenuredGCEvents, var1.tenuredGCTime, GCSample.osFrequency);
      this.edenPanel.updateTextComponents(var1.edenCapacity, var1.edenUsed, var1.edenGCEvents, var1.edenGCTime, GCSample.osFrequency);
      this.s0Panel.updateTextComponents(var1.survivor0Capacity, var1.survivor0Used);
      this.s1Panel.updateTextComponents(var1.survivor1Capacity, var1.survivor1Used);
   }

   public void resetPanel(GCSample var1) {
      Container var2 = this.getContentPane();
      var2.removeAll();
      this.heapPanel.removeAll();
      this.timePanel.removeAll();
      GridBagLayout var3 = new GridBagLayout();
      this.heapPanel.setLayout(var3);
      GridBagConstraints var4 = new GridBagConstraints();
      var4.fill = 1;
      var4.gridwidth = 0;
      var4.weightx = 1.0D;
      double var5 = 0.3D;
      double var7 = 0.1D;
      double var9 = 0.1D;
      double var11 = 0.3D;
      double var13 = 0.2D;
      var4.weighty = var13;
      var3.setConstraints(this.permPanel, var4);
      var4.weighty = var11;
      var3.setConstraints(this.oldPanel, var4);
      var4.weighty = var5;
      var3.setConstraints(this.edenPanel, var4);
      var4.weighty = var7;
      var3.setConstraints(this.s0Panel, var4);
      var4.weighty = var9;
      var3.setConstraints(this.s1Panel, var4);
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
      if (var1.finalizerInitialized) {
         var15 = 0.2D;
         var17 = 0.2D;
         var19 = 0.2D;
         var21 = 0.2D;
         var23 = 0.2D;
         this.timePanel.setLayout(new GridLayout(5, 1));
         var4.weighty = var15;
         var3.setConstraints(this.finalizerQPanel, var4);
         var4.weighty = var17;
         var3.setConstraints(this.finalizerPanel, var4);
         var4.weighty = var19;
         var3.setConstraints(this.compilePanel, var4);
         var4.weighty = var21;
         var3.setConstraints(this.classPanel, var4);
         var4.weighty = var23;
         var3.setConstraints(this.gcPanel, var4);
         this.timePanel.add(this.finalizerQPanel);
         this.timePanel.add(this.finalizerPanel);
      } else {
         var15 = 0.0D;
         var17 = 0.0D;
         var19 = 0.33D;
         var21 = 0.33D;
         var23 = 0.34D;
         this.timePanel.setLayout(new GridLayout(3, 1));
      }

      this.timePanel.add(this.compilePanel);
      this.timePanel.add(this.classPanel);
      this.timePanel.add(this.gcPanel);
      var2 = this.getContentPane();
      GridBagLayout var25 = new GridBagLayout();
      GridBagConstraints var26 = new GridBagConstraints();
      var2.setLayout(var25);
      var26.fill = 1;
      var26.gridwidth = 0;
      var26.weighty = 0.2D;
      var26.weightx = 1.0D;
      var25.setConstraints(this.timePanel, var26);
      var26.gridheight = 0;
      var26.weighty = 0.8D;
      var26.weightx = 1.0D;
      var25.setConstraints(this.heapPanel, var26);
      var2.add(this.timePanel);
      var2.add(this.heapPanel);
   }
}
