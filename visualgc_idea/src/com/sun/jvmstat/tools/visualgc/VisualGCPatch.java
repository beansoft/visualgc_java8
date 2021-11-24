package com.sun.jvmstat.tools.visualgc;

import com.sun.jvmstat.graph.GridDrawer;
import com.sun.jvmstat.graph.Level;
import com.sun.jvmstat.graph.Line;
import com.sun.jvmstat.tools.visualgc.resource.Res;
import github.beansoftapp.visualgc.Exceptions;
import github.beansoftapp.visualgc.GetProcessID;
import github.beansoftapp.visualgc.JpsHelper;
import org.graalvm.visualvm.core.ui.components.NotSupportedDisplayer;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.VmIdentifier;
import sun.jvmstat.monitor.event.HostEvent;
import sun.jvmstat.monitor.event.HostListener;
import sun.jvmstat.monitor.event.VmStatusChangeEvent;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class VisualGCPatch {
  private static volatile boolean active = true;
  private static volatile boolean terminated = false;
  private static Arguments arguments;
  // Begin >>>
  private static final boolean ORIGINAL_UI = Boolean.getBoolean("org.graalvm.visualvm.modules.visualgc.originalUI");
  private static final Color NORMAL_GRAY = new Color(165, 165, 165);
  private static final Color LIGHTER_GRAY = new Color(220, 220, 220);
  private static final Color EVEN_LIGHTER_GRAY = new Color(242, 242, 242);

  private static class ValuePanel extends JPanel {
    private Component c1;

    private Component c2;

    public ValuePanel(Component c1, Component c2) {
      this.c1 = c1;
      this.c2 = c2;
      setLayout((LayoutManager) null);
      add(c1);
      add(c2);
    }

    public void doLayout() {
      int width = getWidth();
      int height = getHeight();
      int c2width = (this.c2.getPreferredSize()).width;
      int c2x = Math.min((this.c1.getPreferredSize()).width, width - c2width);
      this.c2.setBounds(c2x, 0, c2width, height);
      this.c1.setBounds(0, 0, c2x, height);
    }

    public Dimension getPreferredSize() {
      Dimension c1pref = this.c1.getPreferredSize();
      Dimension c2pref = this.c2.getPreferredSize();
      return new Dimension(c1pref.width + c2pref.width,
          Math.max(c1pref.height, c2pref.height));
    }

    public Dimension getMinimumSize() {
      Dimension c1min = this.c1.getMinimumSize();
      Dimension c2min = this.c2.getMinimumSize();
      return new Dimension(c1min.width + c2min.width,
          Math.max(c1min.height, c2min.height));
    }
  }

  private static class ValuesPanel extends JPanel {
    private int tw;

    private int[] pw;

    public ValuesPanel(Component[] components) {
      setLayout((LayoutManager) null);
      this.pw = new int[components.length];
      for (int i = 0; i < components.length; i++) {
        this.pw[i] = (components[i].getPreferredSize()).width;
        this.tw += this.pw[i];
        add(components[i]);
      }
    }

    public void doLayout() {
      int x = 0;
      for (int i = 0; i < getComponentCount(); i++) {
        Component c = getComponent(i);
        int ww = (int) (getWidth() * this.pw[i] / this.tw);
        c.setBounds(x, 0, Math.min(ww, (c.getPreferredSize()).width),
            getHeight());
        x += ww;
      }
    }

    public Dimension getPreferredSize() {
      int prefWidth = 0;
      int prefHeight = 0;
      for (Component c : getComponents()) {
        Dimension prefC = c.getPreferredSize();
        prefWidth += prefC.width;
        prefHeight = Math.max(prefHeight, prefC.height);
      }
      return new Dimension(prefWidth, prefHeight);
    }

    public Dimension getMinimumSize() {
      int minWidth = 0;
      int minHeight = 0;
      for (Component c : getComponents()) {
        Dimension minC = c.getMinimumSize();
        minWidth += minC.width;
        minHeight = Math.max(minHeight, minC.height);
      }
      return new Dimension(minWidth, minHeight);
    }
  }


  private static boolean hasMetaspace;

//  static {
//    customizeColors();
//  }

  private static void customizeColors() {
    if (ORIGINAL_UI)
      return;
    System.setProperty("graphgc.gc.color", colorString(152, 178, 0));
    System.setProperty("graphgc.class.color", "" + colorString(81, 131, 160));
    System.setProperty("graphgc.compile.color", colorString(52, 87, 106));
    System.setProperty("eden.color", "" + colorString(200, 145, 1));
    System.setProperty("survivor.color", colorString(193, 101, 0));
    System.setProperty("old.color", "" + colorString(127, 122, 2));
    System.setProperty("perm.color", "" + colorString(235, 156, 8));
  }

  private static String colorString(int r, int g, int b) {
    return Integer.toString((new Color(r, g, b)).getRGB());
  }

  private static void fixMetaspace(GraphGC graphGC) {
    if (hasMetaspace)
      try {
        Field PERM_PANEL = GraphGC.class.getDeclaredField("permPanel");
        Field BORDER_STRING = GCSpacePanel.class.getDeclaredField("borderString");
        PERM_PANEL.setAccessible(true);
        BORDER_STRING.setAccessible(true);
        GCSpacePanel permPanel = (GCSpacePanel) PERM_PANEL.get(graphGC);
        String borderString = (String) BORDER_STRING.get(permPanel);
        BORDER_STRING.set(permPanel, borderString.replace(Res.getString("perm.gen"), Res.getString("metaspace")));
      } catch (NoSuchFieldException ex) {
        Exceptions.printStackTrace(ex);
      } catch (SecurityException ex) {
        Exceptions.printStackTrace(ex);
      } catch (IllegalArgumentException ex) {
        Exceptions.printStackTrace(ex);
      } catch (IllegalAccessException ex) {
        Exceptions.printStackTrace(ex);
      }
  }

  private static void fixMetaspace(VisualHeap visualHeap) {
    if (hasMetaspace)
      try {
        Field PERM_PANEL = VisualHeap.class.getDeclaredField("permPanel");
        PERM_PANEL.setAccessible(true);
        JPanel permPanel = (JPanel) PERM_PANEL.get(visualHeap);
        TitledBorder border = (TitledBorder) permPanel.getBorder();
        border.setTitle(Res.getString("metaspace"));
      } catch (NoSuchFieldException ex) {
        Exceptions.printStackTrace(ex);
      } catch (SecurityException ex) {
        Exceptions.printStackTrace(ex);
      } catch (IllegalArgumentException ex) {
        Exceptions.printStackTrace(ex);
      } catch (IllegalAccessException ex) {
        Exceptions.printStackTrace(ex);
      }
  }

  private static void customizeHistogram(VisualAgeHistogram histogram) {
    JPanel textPanel = histogram.textPanel;
    JLabel ttLabel = (JLabel) textPanel.getComponent(0);
    JLabel mttLabel = (JLabel) textPanel.getComponent(2);
    JLabel dssLabel = (JLabel) textPanel.getComponent(4);
    JLabel cssLabel = (JLabel) textPanel.getComponent(6);
    textPanel.removeAll();
    textPanel.setLayout(new BorderLayout());
    textPanel.add(new ValuesPanel(new Component[]{new ValuePanel(ttLabel, histogram.ttField), new ValuePanel(mttLabel, histogram.mttField), new ValuePanel(dssLabel, histogram.dssField), new ValuePanel(cssLabel, histogram.cssField)}), "Center");
  }

  // Fix by beansoft
  private static void customizeVisualHeap(VisualHeap heap) {
    try {
      Field fieldLivenessIndicator = VisualHeap.class.getDeclaredField("livenessIndicator");
      fieldLivenessIndicator.setAccessible(true);
      JLabel livenessIndicator = (JLabel) fieldLivenessIndicator.get(heap);

      if (livenessIndicator.getForeground() == Color.white) {
        livenessIndicator.setForeground(Color.black);
      }
    } catch (NoSuchFieldException ex) {
      Exceptions.printStackTrace(ex);
    } catch (SecurityException ex) {
      Exceptions.printStackTrace(ex);
    } catch (IllegalArgumentException ex) {
      Exceptions.printStackTrace(ex);
    } catch (IllegalAccessException ex) {
      Exceptions.printStackTrace(ex);
    }
  }

  // <<< End

  private static void customizeComponents(Component component, List<GridDrawer> gridDrawers) {
    if (ORIGINAL_UI)
      return;
    if (component == null)
      return;
    if (!(component instanceof JComponent))
      return;
    JComponent jComponent = (JComponent) component;
    if (jComponent.getBorder() instanceof TitledBorder) {
      TitledBorder titledBorder = (TitledBorder) jComponent.getBorder();
      TitledBorder newBorder = new TitledBorder(titledBorder.getTitle());
      newBorder.setBorder(BorderFactory.createLineBorder(NORMAL_GRAY, 1));
      newBorder.setTitleColor(titledBorder.getTitleColor());
      // Fix title border
      if (titledBorder.getTitleColor() == Color.white) {
        newBorder.setTitleColor(Color.black);
      }
      Font titleFont = newBorder.getTitleFont();
      if (titleFont == null)
        titleFont = UIManager.getFont("TitledBorder.font");
      if (titleFont == null)
        titleFont = UIManager.getFont("Label.font");
      newBorder.setTitleFont(titleFont.deriveFont(1));
      jComponent.setBorder(newBorder);
    } else if (jComponent instanceof Line) {
      Line line = (Line) jComponent;
      line.setGridPrimaryColor(LIGHTER_GRAY);
      line.setGridSecondaryColor(EVEN_LIGHTER_GRAY);
    } else if (jComponent instanceof Level) {
      Level level = (Level) jComponent;
      level.setPreferredSize(new Dimension(25, 15));
      level.setMinimumSize(level.getPreferredSize());
      try {
        Class<?> levelClass = level.getClass();
        Field gridDrawerField = levelClass.getDeclaredField("gridDrawer");
        gridDrawerField.setAccessible(true);
        GridDrawer gridDrawer = (GridDrawer) gridDrawerField.get(level);
        gridDrawer.setPrimaryColor(LIGHTER_GRAY);
        if (gridDrawers != null)
          gridDrawers.add(gridDrawer);
      } catch (Exception e) {
        Exceptions.printStackTrace(e);
      }
    } else if (jComponent instanceof JLabel) {
      JLabel label = (JLabel) jComponent;
      label.setFont(UIManager.getFont("Label.font"));
      if (label.getText().endsWith(": "))
        label.setFont(label.getFont().deriveFont(1));
      label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

      if (label.getForeground() == Color.white) {
        label.setForeground(Color.black);
      }
    } else if (jComponent instanceof JTextArea) {
      JTextArea label = (JTextArea) jComponent;
      label.setFont(UIManager.getFont("Label.font"));
      if (label.getText().endsWith(": "))
        label.setFont(label.getFont().deriveFont(1));
      label.setForeground(Color.BLACK);
    }
    jComponent.setOpaque(false);
    for (Component child : jComponent.getComponents())
      customizeComponents(child, gridDrawers);
  }

  public static void main(String args[]) {
//		args = new String[]{"412"};
//		args = new String[]{ GetProcessID.getPid() + ""};
    customizeColors();
    String pName = "";
    if (args == null || args.length == 0) {
      JList list = new JList(JpsHelper.getJvmPSList().toArray());
      String s = JOptionPane.showInputDialog(null, list,
          "Please choose a process", JOptionPane.QUESTION_MESSAGE);
      if (s != null && s.length() != 0) {
        args = new String[]{s};
        String info = JpsHelper.getVmInfo(s);
        pName = " - " + info;
      } else {
        String val = (list.getSelectedValue() != null) ? list.getSelectedValue().toString() : null;
        if (val == null) {
          int pid = GetProcessID.getPid();
          args = new String[]{String.valueOf(GetProcessID.getPid())};
          String info = JpsHelper.getVmInfo(pid + "");
          pName = " - " + info;
        } else {
          s = val.substring(0, val.indexOf(' '));
          pName = " - " + val;
          args = new String[]{s};
        }
      }

//      System.out.println(s);
    }

    try {
      arguments = new Arguments(args);
    } catch (IllegalArgumentException var29) {
      System.err.println(var29.getMessage());
      Arguments.printUsage(System.err);
      System.exit(1);
    }

    if (arguments.isHelp()) {
      Arguments.printUsage(System.out);
      System.exit(1);
    }

    if (arguments.isVersion()) {
      Arguments.printVersion(System.out);
      System.exit(1);
    }

    String s = arguments.vmIdString();
    int i = arguments.samplingInterval();
    MonitoredVmModel monitoredvmmodel = null;
    MonitoredHost monitoredhost = null;
    MonitoredVm monitoredvm = null;
    try {
      VmIdentifier vmidentifier = arguments.vmId();
      monitoredhost = MonitoredHost.getMonitoredHost(vmidentifier);
      monitoredvm = monitoredhost.getMonitoredVm(vmidentifier, i);
      monitoredvmmodel = new MonitoredVmModel(monitoredvm);
      hasMetaspace = ModelFixer.fixMetaspace(monitoredvmmodel, monitoredvm);

      class TerminationHandler
          implements HostListener {

        final int lvmid;
        final MonitoredHost host;

        TerminationHandler(int i, MonitoredHost monitoredhost) {
          lvmid = i;
          host = monitoredhost;
        }

        public void vmStatusChanged(VmStatusChangeEvent vmstatuschangeevent) {
          if (vmstatuschangeevent.getTerminated().contains(lvmid) || !vmstatuschangeevent.getActive().contains(lvmid))
            VisualGCPatch.terminated = true;
        }

        public void disconnected(HostEvent hostevent) {
          if (host == hostevent.getMonitoredHost())
            VisualGCPatch.terminated = true;
        }
      }

      if (vmidentifier.getLocalVmId() != 0)
        monitoredhost.addHostListener(new TerminationHandler(vmidentifier.getLocalVmId(), monitoredhost));
    } catch (MonitorException monitorexception) {
      if (monitorexception.getMessage() != null) {
        System.err.println(monitorexception.getMessage());
      } else {
        Throwable throwable = monitorexception.getCause();
        if (throwable != null && throwable.getMessage() != null)
          System.err.println(throwable.getMessage());
        else
          monitorexception.printStackTrace();
      }
      if (monitoredhost != null && monitoredvm != null)
        try {
          monitoredhost.detach(monitoredvm);
        } catch (Exception ignored) {
        }
      System.exit(1);
    }

    GCSample gcsample = new GCSample(monitoredvmmodel);
    int visualheap_x = Integer.getInteger("visualheap.x", 0);
    int k = Integer.getInteger("visualheap.y", 0);
    int l = Integer.getInteger("visualheap.width", 450);
    int i1 = Integer.getInteger("visualheap.height", 600);
    int j1 = Integer.getInteger("graphgc.x", visualheap_x + l);
    int k1 = Integer.getInteger("graphgc.y", k);
    int l1 = Integer.getInteger("graphgc.width", 450);
    int i2 = Integer.getInteger("graphgc.height", 600);
    int j2 = Integer.getInteger("agetable.x", visualheap_x);
    int k2 = Integer.getInteger("agetable.y", k + i1);
    int l2 = Integer.getInteger("agetable.width", l1 + l);
    int i3 = Integer.getInteger("agetable.height", 200);
    final GraphGC graphgc = new GraphGC(gcsample);
    graphgc.setBounds(j1, k1, l1, i2);
    fixMetaspace(graphgc);
    if (!ORIGINAL_UI)
      customizeComponents(graphgc.getContentPane(), null);

    VisualAgeHistogram visualHistogram = null;
    if (gcsample.ageTableSizes != null) {
      visualHistogram = new VisualAgeHistogram(gcsample);
      visualHistogram.setBounds(j2, k2, l2, i3);
      if (!ORIGINAL_UI) {
        customizeHistogram(visualHistogram);
        customizeComponents(visualHistogram.getContentPane(), null);
      }
    } else {
      JFrame frame = new JFrame();
      frame.setTitle(Res.getString("survivor.age.histogram"));
      frame.getContentPane().add((Component) new NotSupportedDisplayer(Res.getString("not.supported.for.this.jvm")), "Center");
      frame.setBounds(j2, k2, l2, i3);
      frame.setVisible(true);
    }
    final VisualAgeHistogram visualagehistogram1 = visualHistogram;
    final List<GridDrawer> gridDrawers = new ArrayList<GridDrawer>();
    final VisualHeap visualHeap = new VisualHeap(graphgc, visualagehistogram1, gcsample) {
      public void updateLevel(GCSample sample) {
        super.updateLevel(sample);
        for (GridDrawer gridDrawer : gridDrawers)
          gridDrawer.setSecondaryColor(EVEN_LIGHTER_GRAY);
      }

      public void updateTextFields(GCSample gcSample) {
        super.updateTextFields(gcSample);
        customizeVisualHeap(this);
      }
    };
    visualHeap.setTitle("VisualGC 3.0" + pName);

    visualHeap.setBounds(visualheap_x, k, l, i1);
    fixMetaspace(visualHeap);
    if (!ORIGINAL_UI) {
      customizeComponents(visualHeap.getContentPane(), gridDrawers);
    }
//		visualheap.getContentPane().getComponent(0).setVisible(false);// Hide cmd info pane
    if (visualagehistogram1 != null)
      visualagehistogram1.setVisible(true);
    visualHeap.setVisible(true);
    graphgc.setVisible(true);

    boolean flag = false;
    GCSample gcsample1 = null;
    while (active) {
      try {
        Thread.sleep(i);
      } catch (InterruptedException interruptedexception) {
      }
      if (terminated) {
        if (!flag) {
          flag = true;
          try {
            SwingUtilities.invokeAndWait(new Runnable() {
                                           public void run() {
                                             String as[] = {
                                                 "Monitored Java Virtual Machine Terminated", " ", "Exit visualgc?", " "
                                             };
                                             int j3 = JOptionPane.showConfirmDialog(visualHeap, as, "Target Terminated",
                                                 JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                                             if (j3 == 0)
                                               System.exit(0);
                                           }
                                         }
            );
          } catch (Exception exception1) {
            exception1.printStackTrace();
          }
          gcsample1 = new GCSample(monitoredvmmodel);
        }
      } else {
        final GCSample gcsample2 = gcsample1 == null ? new GCSample(monitoredvmmodel) : gcsample1;
        SwingUtilities.invokeLater(new Runnable() {
                                     public void run() {
                                       visualHeap.update(gcsample2);
                                       graphgc.update(gcsample2);
                                       if (visualagehistogram1 != null)
                                         visualagehistogram1.update(gcsample2);
                                     }
                                   }
        );
      }
    }
  }

}
