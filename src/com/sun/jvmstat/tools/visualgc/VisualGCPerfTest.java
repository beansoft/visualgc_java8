package com.sun.jvmstat.tools.visualgc;

import beansoft.swing.OptionPane;
import com.sun.jvmstat.graph.FIFOList;
import com.sun.jvmstat.graph.GridDrawer;
import com.sun.jvmstat.graph.Level;
import com.sun.jvmstat.graph.Line;
import com.sun.jvmstat.tools.visualgc.resource.Res;
import github.beansoftapp.visualgc.Exceptions;
import github.beansoftapp.visualgc.GetProcessID;
import github.beansoftapp.visualgc.JpsHelper;
import org.graalvm.visualvm.core.ui.components.DataViewComponent;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

// Memory Performance Test
public class VisualGCPerfTest {
  private static final Logger LOGGER = Logger.getLogger(VisualGCPerfTest.class.getName());
  private static volatile boolean active = true;
  private static volatile boolean terminated = false;
  private static Arguments arguments;
  // Begin >>>
  private static final boolean ORIGINAL_UI = Boolean.getBoolean("org.graalvm.visualvm.modules.visualgc.originalUI");
  private static final Color NORMAL_GRAY = new Color(165, 165, 165);
  private static final Color LIGHTER_GRAY = new Color(220, 220, 220);
  private static final Color EVEN_LIGHTER_GRAY = new Color(242, 242, 242);

  private Timer timer;
  private boolean modelAvailable = false;
  public static void main(String args[]) {
//		args = new String[]{"412"};
//		args = new String[]{ GetProcessID.getPid() + ""};
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
      boolean hasMetaspace = ModelFixer.fixMetaspace(monitoredvmmodel, monitoredvm);

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
            terminated = true;
        }

        public void disconnected(HostEvent hostevent) {
          if (host == hostevent.getMonitoredHost())
            terminated = true;
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


    while (true) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException interruptedexception) {
      }
      GCSample gcsample2 = new GCSample(monitoredvmmodel);
      long data = gcsample2.edenCapacity;
//        System.out.println(LocalDateTime.now() + " " +  gcsample2.edenCapacity);
    }
  }

}