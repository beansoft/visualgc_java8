package com.sun.jvmstat.tools.visualgc;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.VmIdentifier;
import sun.jvmstat.monitor.event.HostEvent;
import sun.jvmstat.monitor.event.HostListener;
import sun.jvmstat.monitor.event.VmStatusChangeEvent;

public class VisualGC {
   private static volatile boolean active = true;
   private static volatile boolean terminated = false;
   private static Arguments arguments;

   public static void main(String[] var0) {
      try {
         arguments = new Arguments(var0);
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

      String var1 = arguments.vmIdString();
      int var2 = arguments.samplingInterval();
      MonitoredVmModel var3 = null;
      MonitoredHost var4 = null;
      MonitoredVm var5 = null;

      try {
         VmIdentifier var6 = arguments.vmId();
         var4 = MonitoredHost.getMonitoredHost(var6);
         var5 = var4.getMonitoredVm(var6, var2);
         var3 = new MonitoredVmModel(var5);
         if (var6.getLocalVmId() != 0) {
            class TerminationHandler implements HostListener {
               Integer lvmid;
               MonitoredHost host;

               TerminationHandler(int var1, MonitoredHost var2) {
                  this.lvmid = new Integer(var1);
                  this.host = var2;
               }

               public void vmStatusChanged(VmStatusChangeEvent var1) {
                  if (var1.getTerminated().contains(this.lvmid) || !var1.getActive().contains(this.lvmid)) {
                     VisualGC.terminated = true;
                  }

               }

               public void disconnected(HostEvent var1) {
                  if (this.host == var1.getMonitoredHost()) {
                     VisualGC.terminated = true;
                  }

               }
            }

            var4.addHostListener(new TerminationHandler(var6.getLocalVmId(), var4));
         }
      } catch (MonitorException var30) {
         if (var30.getMessage() != null) {
            System.err.println(var30.getMessage());
         } else {
            Throwable var7 = var30.getCause();
            if (var7 != null && var7.getMessage() != null) {
               System.err.println(var7.getMessage());
            } else {
               var30.printStackTrace();
            }
         }

         if (var4 != null && var5 != null) {
            try {
               var4.detach(var5);
            } catch (Exception var28) {
            }
         }

         System.exit(1);
      }

      GCSample var31 = new GCSample(var3);
      int var32 = Integer.getInteger("visualheap.x", 0);
      int var8 = Integer.getInteger("visualheap.y", 0);
      int var9 = Integer.getInteger("visualheap.width", 450);
      int var10 = Integer.getInteger("visualheap.height", 600);
      int var11 = Integer.getInteger("graphgc.x", var32 + var9);
      int var12 = Integer.getInteger("graphgc.y", var8);
      int var13 = Integer.getInteger("graphgc.width", 450);
      int var14 = Integer.getInteger("graphgc.height", 600);
      int var15 = Integer.getInteger("agetable.x", var32);
      int var16 = Integer.getInteger("agetable.y", var8 + var10);
      int var17 = Integer.getInteger("agetable.width", var13 + var9);
      int var18 = Integer.getInteger("agetable.height", 200);
      final GraphGC var19 = new GraphGC(var31);
      var19.setBounds(var11, var12, var13, var14);
      VisualAgeHistogram var20 = null;
      if (var31.ageTableSizes != null) {
         var20 = new VisualAgeHistogram(var31);
         var20.setBounds(var15, var16, var17, var18);
      }

      final VisualAgeHistogram var21 = var20;
      final VisualHeap var22 = new VisualHeap(var19, var20, var31);
      var22.setBounds(var32, var8, var9, var10);
      var22.show();
      var19.show();
      if (var20 != null) {
         var20.show();
      }

      boolean var23 = false;
      GCSample var24 = null;

      while(active) {
         try {
            Thread.sleep((long)var2);
         } catch (InterruptedException var27) {
         }

         if (terminated) {
            if (!var23) {
               var23 = true;

               try {
                  SwingUtilities.invokeAndWait(new Runnable() {
                     public void run() {
                        String[] var1 = new String[]{"Monitored Java Virtual Machine Terminated", " ", "Exit visualgc?", " "};
                        int var2 = JOptionPane.showConfirmDialog(var22, var1, "Target Terminated", 0, 1);
                        if (var2 == 0) {
                           System.exit(0);
                        }

                     }
                  });
               } catch (Exception var26) {
                  var26.printStackTrace();
               }

               var24 = new GCSample(var3);
            }
         } else {
            final GCSample var25 = var24 != null ? var24 : new GCSample(var3);
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  var22.update(var25);
                  var19.update(var25);
                  if (var21 != null) {
                     var21.update(var25);
                  }

               }
            });
         }
      }

   }

}
