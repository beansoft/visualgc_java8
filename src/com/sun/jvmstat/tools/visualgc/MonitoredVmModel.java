package com.sun.jvmstat.tools.visualgc;

import com.sun.jvmstat.util.Converter;
import com.yworks.util.annotation.Obfuscation;
import sun.jvmstat.monitor.LongMonitor;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.StringMonitor;

class MonitoredVmModel implements Model {
   private MonitoredVm vm;
   private LongMonitor newGenMinSize;
   private LongMonitor newGenMaxSize;
   private LongMonitor newGenCurSize;
   private LongMonitor edenSize;
   private LongMonitor edenCapacity;
   private LongMonitor edenUsed;
   private LongMonitor edenGCTime;
   private LongMonitor edenGCEvents;
   private LongMonitor survivor0Size;
   private LongMonitor survivor0Capacity;
   private LongMonitor survivor0Used;
   private LongMonitor survivor1Size;
   private LongMonitor survivor1Capacity;
   private LongMonitor survivor1Used;
   private LongMonitor tenuredSize;
   private LongMonitor tenuredCapacity;
   private LongMonitor tenuredUsed;
   private LongMonitor tenuredGCTime;
   private LongMonitor tenuredGCEvents;

   @Obfuscation
   private LongMonitor permSize;
   @Obfuscation
   private LongMonitor permCapacity;
   @Obfuscation
   private LongMonitor permUsed;

   private LongMonitor tenuringThreshold;
   private LongMonitor maxTenuringThreshold;
   private LongMonitor desiredSurvivorSize;
   private LongMonitor ageTableSize;
   private LongMonitor[] ageTableSizes;
   private StringMonitor lastGCCause;
   private StringMonitor currentGCCause;
   private StringMonitor collector0name;
   private StringMonitor collector1name;

   private boolean finalizerInitialized = false;
   private LongMonitor finalizerTime;
   private LongMonitor finalizerQLength;
   private LongMonitor finalizerQMaxLength;
   private LongMonitor finalizerCount;
   private LongMonitor classLoadTime;
   private LongMonitor classesLoaded;
   private LongMonitor classesUnloaded;
   private LongMonitor classBytesLoaded;
   private LongMonitor classBytesUnloaded;
   private LongMonitor totalCompileTime;
   private LongMonitor totalCompile;
   private LongMonitor osElapsedTime;
   private LongMonitor osFrequency;
   private StringMonitor javaCommand;
   private StringMonitor javaHome;
   private StringMonitor vmArgs;
   private StringMonitor vmFlags;
   private StringMonitor vmInfo;
   private StringMonitor vmName;
   private StringMonitor vmVersion;
   private StringMonitor vmVendor;
   private StringMonitor vmSpecName;
   private StringMonitor vmSpecVersion;
   private StringMonitor vmSpecVendor;
   private StringMonitor classPath;
   private StringMonitor bootClassPath;
   private StringMonitor libraryPath;
   private StringMonitor bootLibraryPath;
   private StringMonitor endorsedDirs;
   private StringMonitor extDirs;
   private LongMonitor lastModificationTime;


   private LongMonitor stopGCEvents;
   private LongMonitor stopGCTime;
   private StringMonitor gcPolicyName;
   private StringMonitor collector2name;

   synchronized void initialize_finalizer() {
      if (!this.finalizerInitialized) {
         try {
            this.finalizerQLength = (LongMonitor)this.vm.findByName("sun.gc.finalizer.queue.length");
            this.finalizerQMaxLength = (LongMonitor)this.vm.findByName("sun.gc.finalizer.queue.maxLength");
            this.finalizerTime = (LongMonitor)this.vm.findByName("sun.gc.finalizer.time");
            this.finalizerCount = (LongMonitor)this.vm.findByName("sun.gc.finalizer.objects");
            if (this.finalizerQLength == null) {
               return;
            }

            this.finalizerInitialized = true;
         } catch (MonitorException var2) {
         }

      }
   }

   private void initialize_post_1_4_1() throws MonitorException {
      this.newGenMaxSize = (LongMonitor)this.vm.findByName("sun.gc.generation.0.maxCapacity");
      this.newGenMinSize = (LongMonitor)this.vm.findByName("sun.gc.generation.0.minCapacity");
      this.newGenCurSize = (LongMonitor)this.vm.findByName("sun.gc.generation.0.capacity");
      this.lastGCCause = (StringMonitor)this.vm.findByName("sun.gc.lastCause");
      this.currentGCCause = (StringMonitor)this.vm.findByName("sun.gc.cause");
      this.collector0name = (StringMonitor)this.vm.findByName("sun.gc.collector.0.name");
      this.collector1name = (StringMonitor)this.vm.findByName("sun.gc.collector.1.name");
      this.collector2name = (StringMonitor)this.vm.findByName("sun.gc.collector.2.name");
      this.gcPolicyName =  (StringMonitor)this.vm.findByName("sun.gc.policy.name");

//      try {
//         System.out.println("collector0name=" + collector0name.stringValue());
//         System.out.println("collector1name=" + collector1name.stringValue());
//
//         if(collector2name != null) {
//            System.out.println("collector2name=" + collector2name.stringValue());
//         }
//         if(gcPolicyName != null) {
//            System.out.println("gc policy name=" + gcPolicyName.stringValue());
//         }
//         // collector0name=PCopy
//         //collector1name=CMS
//         //collector2name=CMS stop-the-world phases
//      } catch (Exception e) {
//         e.printStackTrace();
//      }
   }

   private void initialize_common() throws MonitorException {
      this.survivor0Size = (LongMonitor)this.vm.findByName("sun.gc.generation.0.space.1.maxCapacity");
      this.survivor0Capacity = (LongMonitor)this.vm.findByName("sun.gc.generation.0.space.1.capacity");
      this.survivor0Used = (LongMonitor)this.vm.findByName("sun.gc.generation.0.space.1.used");
      this.survivor1Size = (LongMonitor)this.vm.findByName("sun.gc.generation.0.space.2.maxCapacity");
      this.survivor1Capacity = (LongMonitor)this.vm.findByName("sun.gc.generation.0.space.2.capacity");
      this.survivor1Used = (LongMonitor)this.vm.findByName("sun.gc.generation.0.space.2.used");
      this.edenSize = (LongMonitor)this.vm.findByName("sun.gc.generation.0.space.0.maxCapacity");
      this.edenCapacity = (LongMonitor)this.vm.findByName("sun.gc.generation.0.space.0.capacity");
      this.edenUsed = (LongMonitor)this.vm.findByName("sun.gc.generation.0.space.0.used");
      this.tenuredSize = (LongMonitor)this.vm.findByName("sun.gc.generation.1.space.0.maxCapacity");
      this.tenuredCapacity = (LongMonitor)this.vm.findByName("sun.gc.generation.1.space.0.capacity");
      this.tenuredUsed = (LongMonitor)this.vm.findByName("sun.gc.generation.1.space.0.used");
      this.permSize = (LongMonitor)this.vm.findByName("sun.gc.generation.2.space.0.maxCapacity");
      this.permCapacity = (LongMonitor)this.vm.findByName("sun.gc.generation.2.space.0.capacity");
      this.permUsed = (LongMonitor)this.vm.findByName("sun.gc.generation.2.space.0.used");
      this.edenGCEvents = (LongMonitor)this.vm.findByName("sun.gc.collector.0.invocations");
      this.edenGCTime = (LongMonitor)this.vm.findByName("sun.gc.collector.0.time");
      this.tenuredGCEvents = (LongMonitor)this.vm.findByName("sun.gc.collector.1.invocations");
      this.tenuredGCTime = (LongMonitor)this.vm.findByName("sun.gc.collector.1.time");
      this.stopGCEvents =  (LongMonitor)this.vm.findByName("sun.gc.collector.2.invocations");
      this.stopGCTime = (LongMonitor)this.vm.findByName("sun.gc.collector.2.time");

      LongMonitor collectors = (LongMonitor)this.vm.findByName("sun.gc.policy.generations"); //"sun.gc.policy.collectors");
//      System.out.println("collectors=" + collectors.longValue());//sun.gc.policy.generations
//
//      if(stopGCTime != null) {
//         System.out.println("stopGCTime=" + stopGCTime.longValue());
//         System.out.println("stopGCEvents=" + stopGCEvents.longValue());
//      }

      this.ageTableSize = (LongMonitor)this.vm.findByName("sun.gc.generation.0.agetable.size");
      if (this.ageTableSize != null) {
         this.maxTenuringThreshold = (LongMonitor)this.vm.findByName("sun.gc.policy.maxTenuringThreshold");
         this.tenuringThreshold = (LongMonitor)this.vm.findByName("sun.gc.policy.tenuringThreshold");
         this.desiredSurvivorSize = (LongMonitor)this.vm.findByName("sun.gc.policy.desiredSurvivorSize");
         int ageTableSizeValue = (int)this.ageTableSize.longValue();
         this.ageTableSizes = new LongMonitor[ageTableSizeValue];
         String agetableBytesMonitorName = "sun.gc.generation.0.agetable.bytes.";

         for(int i = 0; i < ageTableSizeValue; ++i) {
            if (i < 10) {
               this.ageTableSizes[i] = (LongMonitor)this.vm.findByName(agetableBytesMonitorName + "0" + i);
            } else {
               this.ageTableSizes[i] = (LongMonitor)this.vm.findByName(agetableBytesMonitorName + i);
            }
         }
      }

      this.classLoadTime = (LongMonitor)this.vm.findByName("sun.cls.time");
      this.classesLoaded = (LongMonitor)this.vm.findByName("java.cls.loadedClasses");
      this.classesUnloaded = (LongMonitor)this.vm.findByName("java.cls.unloadedClasses");
      this.classBytesLoaded = (LongMonitor)this.vm.findByName("sun.cls.loadedBytes");
      this.classBytesUnloaded = (LongMonitor)this.vm.findByName("sun.cls.unloadedBytes");
      this.totalCompileTime = (LongMonitor)this.vm.findByName("java.ci.totalTime");
      this.totalCompile = (LongMonitor)this.vm.findByName("sun.ci.totalCompiles");

      try {
         this.initialize_finalizer();
      } catch (Throwable var4) {
      }

      this.osElapsedTime = (LongMonitor)this.vm.findByName("sun.os.hrt.ticks");
      this.osFrequency = (LongMonitor)this.vm.findByName("sun.os.hrt.frequency");
      this.javaCommand = (StringMonitor)this.vm.findByName("sun.rt.javaCommand");
      this.javaHome = (StringMonitor)this.vm.findByName("java.property.java.home");
      this.vmArgs = (StringMonitor)this.vm.findByName("java.rt.vmArgs");
      this.vmFlags = (StringMonitor)this.vm.findByName("java.rt.vmFlags");
      this.vmInfo = (StringMonitor)this.vm.findByName("java.property.java.vm.info");
      this.vmName = (StringMonitor)this.vm.findByName("java.property.java.vm.name");
      this.vmVersion = (StringMonitor)this.vm.findByName("java.property.java.vm.version");
      this.vmVendor = (StringMonitor)this.vm.findByName("java.property.java.vm.vendor");
      this.vmSpecName = (StringMonitor)this.vm.findByName("java.property.java.vm.specification.name");
      this.vmSpecVersion = (StringMonitor)this.vm.findByName("java.property.java.vm.specification.version");
      this.vmSpecVendor = (StringMonitor)this.vm.findByName("java.property.java.vm.specification.vendor");
      this.classPath = (StringMonitor)this.vm.findByName("java.property.java.class.path");
      this.bootClassPath = (StringMonitor)this.vm.findByName("sun.property.sun.boot.class.path");
      this.libraryPath = (StringMonitor)this.vm.findByName("java.property.java.library.path");
      this.bootLibraryPath = (StringMonitor)this.vm.findByName("sun.property.sun.boot.library.path");
      this.endorsedDirs = (StringMonitor)this.vm.findByName("java.property.java.endorsed.dirs");
      this.extDirs = (StringMonitor)this.vm.findByName("java.property.java.ext.dirs");
      this.lastModificationTime = (LongMonitor)this.vm.findByName("sun.perfdata.timestamp");
   }

   void initialize() throws MonitorException {
      this.initialize_common();
      if (!this.vmVersion.stringValue().startsWith("1.4.1")) {
         this.initialize_post_1_4_1();
      }
   }

   public MonitoredVmModel(MonitoredVm monitoredVm) throws MonitorException {
      this.vm = monitoredVm;
      this.initialize();
   }

   public long getNewGenMaxSize() {
      return this.newGenMaxSize != null ? this.newGenMaxSize.longValue() : this.edenSize.longValue() + this.survivor0Size.longValue() + this.survivor1Size.longValue();
   }

   public long getNewGenMinSize() {
      return this.newGenMinSize != null ? this.newGenMinSize.longValue() : this.edenSize.longValue() + this.survivor0Size.longValue() + this.survivor1Size.longValue();
   }

   public long getNewGenCurSize() {
      return this.newGenCurSize != null ? this.newGenCurSize.longValue() : this.edenSize.longValue() + this.survivor0Size.longValue() + this.survivor1Size.longValue();
   }

   public String getLastGCCause() {
      return this.lastGCCause == null ? null : this.lastGCCause.stringValue();
   }

   public String getCurrentGCCause() {
      return this.currentGCCause == null ? null : this.currentGCCause.stringValue();
   }

   public long getEdenUsed() {
      return this.edenUsed.longValue();
   }

   public long getTenuredUsed() {
      return this.tenuredUsed.longValue();
   }

   public long getSurvivor0Used() {
      return this.survivor0Used.longValue();
   }

   public long getSurvivor1Used() {
      return this.survivor1Used.longValue();
   }

   public long getPermUsed() {
      return this.permUsed.longValue();
   }

   public long getEdenSize() {
      return this.edenSize.longValue();
   }

   public long getSurvivor0Size() {
      return this.survivor0Size.longValue();
   }

   public long getSurvivor1Size() {
      return this.survivor1Size.longValue();
   }

   public long getTenuredSize() {
      return this.tenuredSize.longValue();
   }

   public long getPermSize() {
      return this.permSize.longValue();
   }

   public long getEdenCapacity() {
      return this.edenCapacity.longValue();
   }

   public long getSurvivor0Capacity() {
      return this.survivor0Capacity.longValue();
   }

   public long getSurvivor1Capacity() {
      return this.survivor1Capacity.longValue();
   }

   public long getTenuredCapacity() {
      return this.tenuredCapacity.longValue();
   }

   public long getPermCapacity() {
      return this.permCapacity.longValue();
   }

   public long getEdenGCEvents() {
      return this.edenGCEvents.longValue();
   }

   public long getTenuredGCEvents() {
      return this.tenuredGCEvents.longValue();
   }

   public long getEdenGCTime() {
      return this.edenGCTime.longValue();
   }

   public long getTenuredGCTime() {
      return this.tenuredGCTime.longValue();
   }


   public long getTenuringThreshold() {
      return this.tenuringThreshold == null ? 0L : this.tenuringThreshold.longValue();
   }

   public long getMaxTenuringThreshold() {
      return this.maxTenuringThreshold == null ? 0L : this.maxTenuringThreshold.longValue();
   }

   public long getDesiredSurvivorSize() {
      return this.desiredSurvivorSize == null ? 0L : this.desiredSurvivorSize.longValue();
   }

   public long getAgeTableSize() {
      return this.ageTableSize == null ? 0L : this.ageTableSize.longValue();
   }

   public long[] getAgeTableSizes() {
      if (this.ageTableSize == null) {
         return null;
      } else {
         long[] sizes = new long[this.ageTableSizes.length];

         for(int i = 0; i < this.ageTableSizes.length; ++i) {
            sizes[i] = this.ageTableSizes[i].longValue();
         }

         return sizes;
      }
   }

   public void getAgeTableSizes(long[] var1) {
      if (this.ageTableSize != null) {
         for(int var2 = 0; var2 < this.ageTableSizes.length; ++var2) {
            var1[var2] = this.ageTableSizes[var2].longValue();
         }

      }
   }

   public long getClassLoadTime() {
      return this.classLoadTime.longValue();
   }

   public long getClassesLoaded() {
      return this.classesLoaded.longValue();
   }

   public long getClassesUnloaded() {
      return this.classesUnloaded.longValue();
   }

   public long getClassBytesLoaded() {
      return this.classBytesLoaded.longValue();
   }

   public long getClassBytesUnloaded() {
      return this.classBytesUnloaded.longValue();
   }

   public long getTotalCompileTime() {
      return this.totalCompileTime.longValue();
   }

   public long getTotalCompile() {
      return this.totalCompile.longValue();
   }

   public boolean isFinalizerInitialized() {
      return this.finalizerInitialized;
   }

   public void initializeFinalizer() {
      this.initialize_finalizer();
   }

   public long getFinalizerTime() {
      synchronized(this) {
         if (this.finalizerInitialized && this.finalizerTime == null) {
            try {
               this.finalizerTime = (LongMonitor)this.vm.findByName("sun.gc.finalizer.time");
            } catch (MonitorException var4) {
            }
         }

         return this.finalizerTime == null ? 0L : this.finalizerTime.longValue();
      }
   }

   public long getFinalizerCount() {
      synchronized(this) {
         if (this.finalizerInitialized && this.finalizerCount == null) {
            try {
               this.finalizerCount = (LongMonitor)this.vm.findByName("sun.gc.finalizer.objects");
            } catch (MonitorException var4) {
            }
         }

         return this.finalizerCount == null ? 0L : this.finalizerCount.longValue();
      }
   }

   public long getFinalizerQLength() {
      return this.finalizerQLength == null ? 0L : this.finalizerQLength.longValue();
   }

   public long getFinalizerQMaxLength() {
      return this.finalizerQMaxLength == null ? 0L : this.finalizerQMaxLength.longValue();
   }

   public long getOsElapsedTime() {
      return this.osElapsedTime.longValue();
   }

   public long getOsFrequency() {
      return this.osFrequency.longValue();
   }

   public String getJavaCommand() {
      return this.javaCommand == null ? null : this.javaCommand.stringValue();
   }

   public String getJavaHome() {
      return this.javaHome == null ? null : this.javaHome.stringValue();
   }

   public String getVmArgs() {
      return this.vmArgs == null ? null : this.vmArgs.stringValue();
   }

   public String getVmFlags() {
      return this.vmFlags == null ? null : this.vmFlags.stringValue();
   }

   public String getClassPath() {
      return this.classPath == null ? null : this.classPath.stringValue();
   }

   public String getEndorsedDirs() {
      return this.endorsedDirs == null ? null : this.endorsedDirs.stringValue();
   }

   public String getExtDirs() {
      return this.extDirs == null ? null : this.extDirs.stringValue();
   }

   public String getLibraryPath() {
      return this.libraryPath == null ? null : this.libraryPath.stringValue();
   }

   public String getBootClassPath() {
      return this.bootClassPath == null ? null : this.bootClassPath.stringValue();
   }

   public String getBootLibraryPath() {
      return this.bootLibraryPath == null ? null : this.bootLibraryPath.stringValue();
   }

   public String getVmInfo() {
      return this.vmInfo == null ? null : this.vmInfo.stringValue();
   }

   public String getVmName() {
      return this.vmName == null ? null : this.vmName.stringValue();
   }

   public String getVmVersion() {
      return this.vmVersion == null ? null : this.vmVersion.stringValue();
   }

   public String getVmVendor() {
      return this.vmVendor == null ? null : this.vmVendor.stringValue();
   }

   public String getVmSpecName() {
      return this.vmSpecName == null ? null : this.vmSpecName.stringValue();
   }

   public String getVmSpecVersion() {
      return this.vmSpecVersion == null ? null : this.vmSpecVersion.stringValue();
   }

   public String getVmSpecVendor() {
      return this.vmSpecVendor == null ? null : this.vmSpecVendor.stringValue();
   }

   public long getLastModificationTime() {
      return this.lastModificationTime.longValue();
   }

   public String getCollector0name() {
      return safeMonitorValue(collector0name);
   }


   public String getCollector1name() {
      return safeMonitorValue(collector1name);
   }

   public String getCollector2name() {
      return safeMonitorValue(collector2name);
   }

   public String getGcPolicyName() {
      return safeMonitorValue(gcPolicyName);
   }

   public long getCollector2GCTime() {
      return safeMonitorValue(stopGCTime);
   }

   public long getCollector2Events() {
      return safeMonitorValue(stopGCEvents);
   }

   private long safeMonitorValue(LongMonitor monitor) {
      return monitor == null ? 0 : monitor.longValue();
   }

   private String safeMonitorValue(StringMonitor monitor) {
      return monitor == null ? null : monitor.stringValue();
   }
}
