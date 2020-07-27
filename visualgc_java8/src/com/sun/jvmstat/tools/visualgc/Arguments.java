package com.sun.jvmstat.tools.visualgc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import sun.jvmstat.monitor.VmIdentifier;

public class Arguments {
   private static boolean debug = Boolean.getBoolean("Arguments.debug");
   private static final int DEFAULT_INTERVAL = 500;
   private static final String VERSION_FILE = "version";
   private boolean help;
   private boolean version;
   private int interval = -1;
   private String vmIdString;
   private VmIdentifier vmId;

   public static void printUsage(PrintStream var0) {
      printVersion(var0);
      var0.println("usage: visualgc -help");
      var0.println("       visualgc <vmid> [<interval>]");
      var0.println();
      var0.println("Definitions:");
      var0.println("  <vmid>        Virtual Machine Identifier. A vmid takes the following form:");
      var0.println("                     <lvmid>[@<hostname>[:<port>]]");
      var0.println("                Where <lvmid> is the local vm identifier for the target");
      var0.println("                Java virtual machine, typically a process id; <hostname> is");
      var0.println("                the name of the host running the target Java virtual machine;");
      var0.println("                and <port> is the port number for the rmiregistry on the");
      var0.println("                target host. See the visualgc documentation for a more complete");
      var0.println("                description of a <vmid>.");
      var0.println("  <interval>    Sampling interval. The following forms are allowed:");
      var0.println("                    <n>[\"ms\"|\"s\"]");
      var0.println("                Where <n> is an integer and the suffix specifies the units as ");
      var0.println("                milliseconds(\"ms\") or seconds(\"s\"). The default units are \"ms\".");
      var0.println("                The default interval is 500ms");
   }

   public static void printVersion(PrintStream var0) {
      URL var1 = Arguments.class.getClassLoader().getResource("version");

      try {
         BufferedReader var2 = new BufferedReader(new InputStreamReader(var1.openStream()));
         String var3 = null;

         while((var3 = var2.readLine()) != null) {
            var0.println(var3);
         }
      } catch (Exception var4) {
         System.err.println("Unexpected exception: " + var4.getMessage());
         var4.printStackTrace();
         System.exit(1);
      }

   }

   private static int toMillis(String var0) throws IllegalArgumentException {
      String[] var1 = new String[]{"ms", "s"};
      String var2 = null;
      String var3 = null;

      int var4;
      for(var4 = 0; var4 < var1.length; ++var4) {
         int var5 = var0.indexOf(var1[var4]);
         if (var5 > 0) {
            var2 = var0.substring(var5);
            var3 = var0.substring(0, var5);
            break;
         }
      }

      if (var2 == null) {
         var3 = var0;
      }

      try {
         var4 = Integer.parseInt(var3);
         if (var2 != null && var2.compareTo("ms") != 0) {
            if (var2.compareTo("s") == 0) {
               return var4 * 1000;
            } else {
               throw new IllegalArgumentException("Unsupported interval time unit: " + var2);
            }
         } else {
            return var4;
         }
      } catch (NumberFormatException var6) {
         throw new IllegalArgumentException("Could not convert interval: " + var0);
      }
   }

   public Arguments(String[] var1) throws IllegalArgumentException {
      boolean var2 = false;
      if (var1.length >= 1 && var1.length <= 2) {
         if (var1[0].compareTo("-?") != 0 && var1[0].compareTo("-help") != 0) {
            if (var1[0].compareTo("-v") != 0 && var1[0].compareTo("-version") != 0) {
               byte var9 = 0;
               if (var9 < var1.length && var1[var9].startsWith("-")) {
                  String var10000 = var1[var9];
                  String var4 = null;
                  int var5 = var1[var9].indexOf(64);
                  if (var5 < 0) {
                     var4 = var1[var9];
                  } else {
                     var4 = var1[var9].substring(0, var5);
                  }

                  try {
                     int var6 = Integer.parseInt(var4);
                  } catch (NumberFormatException var8) {
                     throw new IllegalArgumentException("illegal argument: " + var1[var9]);
                  }
               }

               switch(var1.length - var9) {
               case 1:
                  this.vmIdString = var1[var1.length - 1];
                  break;
               case 2:
                  this.interval = toMillis(var1[var1.length - 1]);
                  this.vmIdString = var1[var1.length - 2];
               }

               if (this.interval == -1) {
                  this.interval = 500;
               }

               try {
                  this.vmId = new VmIdentifier(this.vmIdString);
               } catch (URISyntaxException var7) {
                  IllegalArgumentException var10 = new IllegalArgumentException("Malformed VM Identifier: " + this.vmIdString);
                  var10.initCause(var7);
                  throw var10;
               }
            } else {
               this.version = true;
            }
         } else if (var1.length != 1) {
            throw new IllegalArgumentException("invalid argument count");
         } else {
            this.help = true;
         }
      } else {
         throw new IllegalArgumentException("invalid argument count");
      }
   }

   public boolean isHelp() {
      return this.help;
   }

   public boolean isVersion() {
      return this.version;
   }

   public String vmIdString() {
      return this.vmIdString;
   }

   public VmIdentifier vmId() {
      return this.vmId;
   }

   public int samplingInterval() {
      return this.interval;
   }
}
