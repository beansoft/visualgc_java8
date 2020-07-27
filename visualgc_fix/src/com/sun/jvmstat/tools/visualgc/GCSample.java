package com.sun.jvmstat.tools.visualgc;

public class GCSample
{

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
	long ageTableSizes[];
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

	static synchronized void initStaticCounters(Model model)
	{
		if (!initialized)
		{
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

	GCSample(Model model)
	{
		initStaticCounters(model);
		newGenMaxSize = model.getNewGenMaxSize();
		newGenMinSize = model.getNewGenMinSize();
		newGenCurSize = model.getNewGenCurSize();
		edenUsed = model.getEdenUsed();
		survivor0Used = model.getSurvivor0Used();
		survivor1Used = model.getSurvivor1Used();
		tenuredUsed = model.getTenuredUsed();
		permUsed = model.getPermUsed();
		tenuringThreshold = model.getTenuringThreshold();
		edenSize = model.getEdenSize();
		survivor0Size = model.getSurvivor0Size();
		survivor1Size = model.getSurvivor1Size();
		tenuredSize = model.getTenuredSize();
		permSize = model.getPermSize();
		edenCapacity = model.getEdenCapacity();
		survivor0Capacity = model.getSurvivor0Capacity();
		survivor1Capacity = model.getSurvivor1Capacity();
		tenuredCapacity = model.getTenuredCapacity();
		permCapacity = model.getPermCapacity();
		edenGCEvents = model.getEdenGCEvents();
		edenGCTime = model.getEdenGCTime();
		tenuredGCEvents = model.getTenuredGCEvents();
		tenuredGCTime = model.getTenuredGCTime();
		tenuringThreshold = model.getTenuringThreshold();
		desiredSurvivorSize = model.getDesiredSurvivorSize();
		ageTableSizes = model.getAgeTableSizes();
		lastGCCause = model.getLastGCCause();
		currentGCCause = model.getCurrentGCCause();
		classLoadTime = model.getClassLoadTime();
		classesLoaded = model.getClassesLoaded();
		classesUnloaded = model.getClassesUnloaded();
		classBytesLoaded = model.getClassBytesLoaded();
		classBytesUnloaded = model.getClassBytesUnloaded();
		totalCompileTime = model.getTotalCompileTime();
		totalCompile = model.getTotalCompile();
		model.initializeFinalizer();
		finalizerInitialized = model.isFinalizerInitialized();
		finalizerTime = model.getFinalizerTime();
		finalizerCount = model.getFinalizerCount();
		finalizerQLength = model.getFinalizerQLength();
		finalizerQMaxLength = model.getFinalizerQMaxLength();
		osElapsedTime = model.getOsElapsedTime();
		lastModificationTime = model.getLastModificationTime();
	}

	public double getAdjustedEdenSize()
	{
		if (edenCapacity + survivor0Capacity + survivor1Capacity == newGenMaxSize)
		{
			return (double)edenCapacity;
		} else
		{
			long l = newGenMaxSize - newGenCurSize;
			return (double)(edenCapacity + l);
		}
	}

	public double getEdenLiveRatio()
	{
		return (double)edenUsed / (double)edenSize;
	}

	public double getAdjustedEdenLiveRatio()
	{
		return (double)edenUsed / getAdjustedEdenSize();
	}

	public double getSurvivor0LiveRatio()
	{
		return (double)survivor0Used / (double)survivor0Size;
	}

	public double getSurvivor1LiveRatio()
	{
		return (double)survivor1Used / (double)survivor1Size;
	}

	public double getTenuredLiveRatio()
	{
		return (double)tenuredUsed / (double)tenuredSize;
	}

	public double getPermLiveRatio()
	{
		return (double)permUsed / (double)permSize;
	}

	public double getAdjustedEdenCommittedRatio()
	{
		return (double)edenCapacity / getAdjustedEdenSize();
	}

	public double getEdenCommittedRatio()
	{
		return (double)edenCapacity / (double)edenSize;
	}

	public double getSurvivor0CommittedRatio()
	{
		return (double)survivor0Capacity / (double)survivor0Size;
	}

	public double getSurvivor1CommittedRatio()
	{
		return (double)survivor1Capacity / (double)survivor1Size;
	}

	public double getTenuredCommittedRatio()
	{
		return (double)tenuredCapacity / (double)tenuredSize;
	}

	public double getPermCommittedRatio()
	{
		return (double)permCapacity / (double)permSize;
	}

	public long getTotalGCTime(GCSample gcsample)
	{
		return Math.abs(edenGCTime - gcsample.edenGCTime) + Math.abs(tenuredGCTime - gcsample.tenuredGCTime);
	}

	public boolean heapSizeChanged(GCSample gcsample)
	{
		return edenCapacity != gcsample.edenCapacity || survivor0Capacity != gcsample.survivor0Capacity || survivor1Capacity != gcsample.survivor1Capacity || tenuredCapacity != gcsample.tenuredCapacity || permCapacity != gcsample.permCapacity;
	}

}
