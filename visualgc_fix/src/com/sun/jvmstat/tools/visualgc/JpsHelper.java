package com.sun.jvmstat.tools.visualgc;

import beansoft.jvm.hotspot.util.GetProcessID;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.MonitoredVmUtil;
import sun.jvmstat.monitor.StringMonitor;
import sun.jvmstat.monitor.VmIdentifier;

public class JpsHelper {

   public static void main(String[] args) throws Exception {
      System.out.println(getJvmPSList());
   }

   /**
    * 获取本机上的所有进程列表.
    * @return List<JvmProcess>
    */
   public static List<String> getJvmPSList()  {
      List<String> vms = new ArrayList<String>();

      try {
         HostIdentifier hostId = new HostIdentifier((String)null);
         MonitoredHost monitoredHost = MonitoredHost.getMonitoredHost(hostId);

         // get the set active JVMs on the specified host.
         Set jvms = monitoredHost.activeVms();
         int curVMId = GetProcessID.getPid();
         for (Iterator j = jvms.iterator(); j.hasNext(); /* empty */) {

            Throwable lastError = null;

            int lvmid = ((Integer) j.next()).intValue();
            StringBuilder output;
//				System.out.println("lvmid=" + lvmid);
            MonitoredVm vm = null;
            String vmidString = "//" + lvmid + "?mode=r";
            output = new StringBuilder();

            output.append(String.valueOf(lvmid));
            try {
               VmIdentifier id = new VmIdentifier(vmidString);
               vm = monitoredHost.getMonitoredVm(id, 0);
//               vms.add(vmidString);
            } catch (URISyntaxException e) {
               e.printStackTrace();
               // unexpected as vmidString is based on a validated hostid
               lastError = e;
            } catch (Exception e) {
               lastError = e;
               e.printStackTrace();
            } finally {
               if (vm == null) {
                  /*
                   * we ignore most exceptions, as there are race conditions
                   * where a JVM in 'jvms' may terminate before we get a
                   * chance to list its information. Other errors, such as
                   * access and I/O exceptions should stop us from iterating
                   * over the complete set.
                   */
                  System.out.println(" -- process information unavailable");
                  continue;
               }
            }

            if (curVMId == lvmid) {
               output.append(" VisualGC Self");
            } else {
               output.append(" ");
               output.append(MonitoredVmUtil.mainClass(vm, false));
            }

            vms.add(output.toString());

            monitoredHost.detach(vm);
         }
      } catch (URISyntaxException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (MonitorException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return vms;
   }


   public static String commandLine(MonitoredVm vm) throws MonitorException {
      StringMonitor cmd = (StringMonitor)vm.findByName("sun.rt.javaCommand");
      return cmd == null ? "Unknown" : cmd.stringValue();
   }

   public static String mainClass(MonitoredVm vm, boolean fullPath) throws MonitorException {
      String commandLine = commandLine(vm);
      String arg0 = commandLine;
      int firstSpace = commandLine.indexOf(32);
      if (firstSpace > 0) {
         arg0 = commandLine.substring(0, firstSpace);
      }

      if (!fullPath) {
         int lastFileSeparator = arg0.lastIndexOf(47);
         if (lastFileSeparator > 0) {
            return arg0.substring(lastFileSeparator + 1);
         }

         lastFileSeparator = arg0.lastIndexOf(92);
         if (lastFileSeparator > 0) {
            return arg0.substring(lastFileSeparator + 1);
         }

         int lastPackageSeparator = arg0.lastIndexOf(46);
         if (lastPackageSeparator > 0) {
            return arg0.substring(lastPackageSeparator + 1);
         }
      }

      return arg0;
   }
}
