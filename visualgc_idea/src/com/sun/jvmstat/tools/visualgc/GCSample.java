package com.sun.jvmstat.tools.visualgc;

public class GCSample {
   long newGenMaxSize;
   long newGenMinSize;
   long newGenCurSize;
   long edenSize;
   long edenCapacity;
   long edenUsed;
   long edenGCEvents;
   long edenGCTime;
   long survivor0Size;
   long survivor0Capacity;
   long survivor0Used;
   long survivor1Size;
   long survivor1Capacity;
   long survivor1Used;
   long tenuredSize;
   long tenuredCapacity;
   long tenuredUsed;
   long tenuredGCEvents;
   long tenuredGCTime;
   long permSize;
   long permCapacity;
   long permUsed;
   long tenuringThreshold;
   long desiredSurvivorSize;
   long[] ageTableSizes;
   long classLoadTime;
   long classesLoaded;
   long classesUnloaded;
   long classBytesLoaded;
   long classBytesUnloaded;
   long totalCompileTime;
   long totalCompile;
   boolean finalizerInitialized;
   long finalizerTime;
   long finalizerCount;
   long finalizerQLength;
   long finalizerQMaxLength;
   long osElapsedTime;
   long lastModificationTime;
   String lastGCCause;
   String currentGCCause;

   long stopGCTime;
   long stopGCEvents;

   static long maxTenuringThreshold;
   public static long osFrequency;
   static String javaCommand;
   static String javaHome;
   static String vmArgs;
   static String vmFlags;
   static String vmInfo;
   static String vmName;
   static String vmVersion;
   static String vmVendor;
   static String vmSpecName;
   static String vmSpecVersion;
   static String vmSpecVendor;
   static String classPath;
   static String bootClassPath;
   static String libraryPath;
   static String bootLibraryPath;
   static String endorsedDirs;
   static String extDirs;
   static String gcPolicyName, collector0name, collector1name, collector2name;


   private static boolean initialized = false;

   static synchronized void initStaticCounters(Model model) {
      if (!initialized) {
         maxTenuringThreshold = model.getMaxTenuringThreshold();
         osFrequency = model.getOsFrequency();
         javaCommand = model.getJavaCommand();
         javaHome = model.getJavaHome();
         vmArgs = model.getVmArgs();
         vmFlags = model.getVmFlags();
         vmInfo = model.getVmInfo();
         vmName = model.getVmName();
         vmVersion = model.getVmVersion();
         vmVendor = model.getVmVendor();
         vmSpecName = model.getVmSpecName();
         vmSpecVersion = model.getVmSpecVersion();
         vmSpecVendor = model.getVmSpecVendor();
         classPath = model.getClassPath();
         bootClassPath = model.getBootClassPath();
         libraryPath = model.getLibraryPath();
         bootLibraryPath = model.getBootLibraryPath();
         endorsedDirs = model.getEndorsedDirs();
         extDirs = model.getExtDirs();
         gcPolicyName = model.getGcPolicyName();
         collector0name = model.getCollector0name();
         collector1name = model.getCollector1name();
         collector2name = model.getCollector2name();
      }

   }

   GCSample(Model model) {
      initStaticCounters(model);
      this.newGenMaxSize = model.getNewGenMaxSize();
      this.newGenMinSize = model.getNewGenMinSize();
      this.newGenCurSize = model.getNewGenCurSize();
      this.edenUsed = model.getEdenUsed();
      this.survivor0Used = model.getSurvivor0Used();
      this.survivor1Used = model.getSurvivor1Used();
      this.tenuredUsed = model.getTenuredUsed();
      this.permUsed = model.getPermUsed();
      this.tenuringThreshold = model.getTenuringThreshold();
      this.edenSize = model.getEdenSize();
      this.survivor0Size = model.getSurvivor0Size();
      this.survivor1Size = model.getSurvivor1Size();
      this.tenuredSize = model.getTenuredSize();
      this.permSize = model.getPermSize();
      this.edenCapacity = model.getEdenCapacity();
      this.survivor0Capacity = model.getSurvivor0Capacity();
      this.survivor1Capacity = model.getSurvivor1Capacity();
      this.tenuredCapacity = model.getTenuredCapacity();
      this.permCapacity = model.getPermCapacity();
      this.edenGCEvents = model.getEdenGCEvents();
      this.edenGCTime = model.getEdenGCTime();
      this.tenuredGCEvents = model.getTenuredGCEvents();
      this.tenuredGCTime = model.getTenuredGCTime();
      this.tenuringThreshold = model.getTenuringThreshold();
      this.desiredSurvivorSize = model.getDesiredSurvivorSize();
      this.ageTableSizes = model.getAgeTableSizes();
      this.lastGCCause = model.getLastGCCause();
      this.currentGCCause = model.getCurrentGCCause();
      this.classLoadTime = model.getClassLoadTime();
      this.classesLoaded = model.getClassesLoaded();
      this.classesUnloaded = model.getClassesUnloaded();
      this.classBytesLoaded = model.getClassBytesLoaded();
      this.classBytesUnloaded = model.getClassBytesUnloaded();
      this.totalCompileTime = model.getTotalCompileTime();
      this.totalCompile = model.getTotalCompile();
      model.initializeFinalizer();
      this.finalizerInitialized = model.isFinalizerInitialized();
      this.finalizerTime = model.getFinalizerTime();
      this.finalizerCount = model.getFinalizerCount();
      this.finalizerQLength = model.getFinalizerQLength();
      this.finalizerQMaxLength = model.getFinalizerQMaxLength();
      this.osElapsedTime = model.getOsElapsedTime();
      this.lastModificationTime = model.getLastModificationTime();

      this.stopGCTime = model.getCollector2GCTime();
      this.stopGCEvents = model.getCollector2Events();
   }

   public double getAdjustedEdenSize() {
      if (this.edenCapacity + this.survivor0Capacity + this.survivor1Capacity == this.newGenMaxSize) {
         return (double)this.edenCapacity;
      } else {
         long l = this.newGenMaxSize - this.newGenCurSize;
         return (double)(this.edenCapacity + l);
      }
   }

   public double getEdenLiveRatio() {
      return (double)this.edenUsed / (double)this.edenSize;
   }

   public double getAdjustedEdenLiveRatio() {
      return (double)this.edenUsed / this.getAdjustedEdenSize();
   }

   public double getSurvivor0LiveRatio() {
      return (double)this.survivor0Used / (double)this.survivor0Size;
   }

   public double getSurvivor1LiveRatio() {
      return (double)this.survivor1Used / (double)this.survivor1Size;
   }

   public double getTenuredLiveRatio() {
      return (double)this.tenuredUsed / (double)this.tenuredSize;
   }

   public double getPermLiveRatio() {
      return (double)this.permUsed / (double)this.permSize;
   }

   public double getAdjustedEdenCommittedRatio() {
      return (double)this.edenCapacity / this.getAdjustedEdenSize();
   }

   public double getEdenCommittedRatio() {
      return (double)this.edenCapacity / (double)this.edenSize;
   }

   public double getSurvivor0CommittedRatio() {
      return (double)this.survivor0Capacity / (double)this.survivor0Size;
   }

   public double getSurvivor1CommittedRatio() {
      return (double)this.survivor1Capacity / (double)this.survivor1Size;
   }

   public double getTenuredCommittedRatio() {
      return (double)this.tenuredCapacity / (double)this.tenuredSize;
   }

   public double getPermCommittedRatio() {
      return (double)this.permCapacity / (double)this.permSize;
   }

   public long getTotalGCTime(GCSample sample) {
      return Math.abs(this.edenGCTime - sample.edenGCTime) + Math.abs(this.tenuredGCTime - sample.tenuredGCTime);
   }

   public boolean heapSizeChanged(GCSample sample) {
      return this.edenCapacity != sample.edenCapacity || this.survivor0Capacity != sample.survivor0Capacity || this.survivor1Capacity != sample.survivor1Capacity || this.tenuredCapacity != sample.tenuredCapacity || this.permCapacity != sample.permCapacity;
   }
}
