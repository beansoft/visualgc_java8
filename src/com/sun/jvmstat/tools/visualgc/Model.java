package com.sun.jvmstat.tools.visualgc;

interface Model {
   long getNewGenMinSize();

   long getNewGenMaxSize();

   long getNewGenCurSize();

   long getEdenUsed();

   long getSurvivor0Used();

   long getSurvivor1Used();

   long getTenuredUsed();

   long getPermUsed();

   long getEdenSize();

   long getSurvivor0Size();

   long getSurvivor1Size();

   long getTenuredSize();

   long getPermSize();

   long getEdenCapacity();

   long getSurvivor0Capacity();

   long getSurvivor1Capacity();

   long getTenuredCapacity();

   long getPermCapacity();

   long getEdenGCEvents();

   long getTenuredGCEvents();

   long getEdenGCTime();

   long getTenuredGCTime();

   long getTenuringThreshold();

   long getMaxTenuringThreshold();

   long getDesiredSurvivorSize();

   long getAgeTableSize();

   String getLastGCCause();

   String getCurrentGCCause();

   long[] getAgeTableSizes();

   void getAgeTableSizes(long[] var1);

   long getClassLoadTime();

   long getClassesLoaded();

   long getClassesUnloaded();

   long getClassBytesLoaded();

   long getClassBytesUnloaded();

   long getTotalCompileTime();

   long getTotalCompile();

   void initializeFinalizer();

   boolean isFinalizerInitialized();

   long getFinalizerTime();

   long getFinalizerCount();

   long getFinalizerQLength();

   long getFinalizerQMaxLength();

   long getOsElapsedTime();

   long getOsFrequency();

   String getJavaCommand();

   String getJavaHome();

   String getVmArgs();

   String getVmFlags();

   String getClassPath();

   String getEndorsedDirs();

   String getExtDirs();

   String getLibraryPath();

   String getBootClassPath();

   String getBootLibraryPath();

   String getVmInfo();

   String getVmName();

   String getVmVersion();

   String getVmVendor();

   String getVmSpecName();

   String getVmSpecVersion();

   String getVmSpecVendor();

   long getLastModificationTime();
   String getCollector0name();
   String getCollector1name();
   String getCollector2name();
   String getGcPolicyName();

   long getCollector2GCTime();
}
