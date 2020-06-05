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
   static long maxTenuringThreshold;
   static long osFrequency;
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
      }

   }

   GCSample(Model var1) {
      initStaticCounters(var1);
      this.newGenMaxSize = var1.getNewGenMaxSize();
      this.newGenMinSize = var1.getNewGenMinSize();
      this.newGenCurSize = var1.getNewGenCurSize();
      this.edenUsed = var1.getEdenUsed();
      this.survivor0Used = var1.getSurvivor0Used();
      this.survivor1Used = var1.getSurvivor1Used();
      this.tenuredUsed = var1.getTenuredUsed();
      this.permUsed = var1.getPermUsed();
      this.tenuringThreshold = var1.getTenuringThreshold();
      this.edenSize = var1.getEdenSize();
      this.survivor0Size = var1.getSurvivor0Size();
      this.survivor1Size = var1.getSurvivor1Size();
      this.tenuredSize = var1.getTenuredSize();
      this.permSize = var1.getPermSize();
      this.edenCapacity = var1.getEdenCapacity();
      this.survivor0Capacity = var1.getSurvivor0Capacity();
      this.survivor1Capacity = var1.getSurvivor1Capacity();
      this.tenuredCapacity = var1.getTenuredCapacity();
      this.permCapacity = var1.getPermCapacity();
      this.edenGCEvents = var1.getEdenGCEvents();
      this.edenGCTime = var1.getEdenGCTime();
      this.tenuredGCEvents = var1.getTenuredGCEvents();
      this.tenuredGCTime = var1.getTenuredGCTime();
      this.tenuringThreshold = var1.getTenuringThreshold();
      this.desiredSurvivorSize = var1.getDesiredSurvivorSize();
      this.ageTableSizes = var1.getAgeTableSizes();
      this.lastGCCause = var1.getLastGCCause();
      this.currentGCCause = var1.getCurrentGCCause();
      this.classLoadTime = var1.getClassLoadTime();
      this.classesLoaded = var1.getClassesLoaded();
      this.classesUnloaded = var1.getClassesUnloaded();
      this.classBytesLoaded = var1.getClassBytesLoaded();
      this.classBytesUnloaded = var1.getClassBytesUnloaded();
      this.totalCompileTime = var1.getTotalCompileTime();
      this.totalCompile = var1.getTotalCompile();
      var1.initializeFinalizer();
      this.finalizerInitialized = var1.isFinalizerInitialized();
      this.finalizerTime = var1.getFinalizerTime();
      this.finalizerCount = var1.getFinalizerCount();
      this.finalizerQLength = var1.getFinalizerQLength();
      this.finalizerQMaxLength = var1.getFinalizerQMaxLength();
      this.osElapsedTime = var1.getOsElapsedTime();
      this.lastModificationTime = var1.getLastModificationTime();
   }

   public double getAdjustedEdenSize() {
      if (this.edenCapacity + this.survivor0Capacity + this.survivor1Capacity == this.newGenMaxSize) {
         return (double)this.edenCapacity;
      } else {
         long var1 = this.newGenMaxSize - this.newGenCurSize;
         return (double)(this.edenCapacity + var1);
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

   public long getTotalGCTime(GCSample var1) {
      return Math.abs(this.edenGCTime - var1.edenGCTime) + Math.abs(this.tenuredGCTime - var1.tenuredGCTime);
   }

   public boolean heapSizeChanged(GCSample var1) {
      return this.edenCapacity != var1.edenCapacity || this.survivor0Capacity != var1.survivor0Capacity || this.survivor1Capacity != var1.survivor1Capacity || this.tenuredCapacity != var1.tenuredCapacity || this.permCapacity != var1.permCapacity;
   }
}
