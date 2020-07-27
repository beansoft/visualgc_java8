package com.sun.jvmstat.util.gc;

import sun.jvmstat.monitor.*;

public class GCPolicy
{

	private MonitoredVm vm;
	private String name;
	private GCCollector gcCollectors[];
	private GCGeneration gcGenerations[];
	private StringMonitor gcCause;
	private StringMonitor gcLastCause;
	private long hrtFrequency;
	private long reservedSize;

	public GCPolicy(MonitoredVm monitoredvm)
		throws MonitorException
	{
		vm = monitoredvm;
		buildPolicy();
	}

	public MonitoredVm getMonitoredVm()
	{
		return vm;
	}

	public String getName()
	{
		return name;
	}

	public int collectors()
	{
		return gcCollectors.length;
	}

	public GCCollector[] getGCCollectors()
	{
		return gcCollectors;
	}

	public GCCollector getGCCollector(int i)
	{
		return gcCollectors[i];
	}

	public int generations()
	{
		return gcGenerations.length;
	}

	public GCGeneration[] getGCGenerations()
	{
		return gcGenerations;
	}

	public GCGeneration getGCGeneration(int i)
	{
		return gcGenerations[i];
	}

	public String getGCCause()
	{
		return gcCause.stringValue();
	}

	public String getGCLastCause()
	{
		return gcLastCause.stringValue();
	}

	private long computeReservedSize()
	{
		long l = 0L;
		for (int i = 0; i < gcCollectors.length; i++)
			l += gcCollectors[i].getReservedSize();

		return l;
	}

	public long getReservedSize()
	{
		if (reservedSize == 0L)
			reservedSize = computeReservedSize();
		return reservedSize;
	}

	public Number getCommittedSize()
	{
		long l = 0L;
		for (int i = 0; i < gcCollectors.length; i++)
			l += gcCollectors[i].getCommittedSize().longValue();

		return new Long(l);
	}

	public Number getUsed()
	{
		long l = 0L;
		for (int i = 0; i < gcCollectors.length; i++)
			l += gcCollectors[i].getUsed().longValue();

		return new Long(l);
	}

	public Number getInvocations()
	{
		long l = 0L;
		for (int i = 0; i < gcCollectors.length; i++)
			l += gcCollectors[i].getInvocations().longValue();

		return new Long(l);
	}

	public Number getTime()
	{
		long l = 0L;
		for (int i = 0; i < gcCollectors.length; i++)
			l += gcCollectors[i].getTime().longValue();

		return new Long(l);
	}

	private void buildPolicy()
		throws MonitorException
	{
		StringMonitor stringmonitor = (StringMonitor)vm.findByName("java.property.java.vm.version");
		String s = stringmonitor.stringValue();
		if (s.startsWith("1.4.1"))
		{
			throw new MonitorException("1.4.1 not supported");
		} else
		{
			buildPolicy1_4_2();
			return;
		}
	}

	private void buildPolicy1_4_2()
		throws MonitorException
	{
		StringMonitor stringmonitor = (StringMonitor)vm.findByName("sun.gc.policy.name");
		LongMonitor longmonitor = (LongMonitor)vm.findByName("sun.gc.policy.collectors");
		LongMonitor longmonitor1 = (LongMonitor)vm.findByName("sun.gc.policy.generations");
		LongMonitor longmonitor2 = (LongMonitor)vm.findByName("sun.os.hrt.frequency");
		gcCause = (StringMonitor)vm.findByName("sun.gc.cause");
		gcLastCause = (StringMonitor)vm.findByName("sun.gc.lastCause");
		name = stringmonitor.stringValue();
		gcCollectors = new GCCollector[(int)longmonitor.longValue()];
		gcGenerations = new GCGeneration[(int)longmonitor1.longValue()];
		hrtFrequency = longmonitor2.longValue();
		for (int i = 0; i < gcGenerations.length; i++)
		{
			String s = "sun.gc.generation." + i + ".";
			StringMonitor stringmonitor1 = (StringMonitor)vm.findByName(s + "name");
			String s2 = stringmonitor1.stringValue();
			LongMonitor longmonitor3 = (LongMonitor)vm.findByName(s + "maxCapacity");
			LongMonitor longmonitor4 = (LongMonitor)vm.findByName(s + "minCapacity");
			LongMonitor longmonitor6 = (LongMonitor)vm.findByName(s + "capacity");
			LongMonitor longmonitor8 = (LongMonitor)vm.findByName(s + "spaces");
			GCSpace agcspace[] = new GCSpace[(int)longmonitor8.longValue()];
			for (int k = 0; k < agcspace.length; k++)
			{
				String s4 = s + "space." + k + ".";
				StringMonitor stringmonitor3 = (StringMonitor)vm.findByName(s4 + "name");
				String s5 = stringmonitor3.stringValue();
				LongMonitor longmonitor11 = (LongMonitor)vm.findByName(s4 + "maxCapacity");
				LongMonitor longmonitor12 = (LongMonitor)vm.findByName(s4 + "capacity");
				LongMonitor longmonitor13 = (LongMonitor)vm.findByName(s4 + "used");
				agcspace[k] = new GCSpace(s5, k, longmonitor11.longValue(), longmonitor12, longmonitor13);
			}

			gcGenerations[i] = new GCGeneration(s2, i, longmonitor3.longValue(), longmonitor4.longValue(), longmonitor6, agcspace);
		}

		GCGeneration agcgeneration[][] = {
			{
				gcGenerations[0]
			}, {
				gcGenerations[1], gcGenerations[2]
			}
		};
		for (int j = 0; j < gcCollectors.length; j++)
		{
			String s1 = "sun.gc.collector." + j + ".";
			StringMonitor stringmonitor2 = (StringMonitor)vm.findByName(s1 + "name");
			String s3 = stringmonitor2.stringValue();
			LongMonitor longmonitor5 = (LongMonitor)vm.findByName(s1 + "invocations");
			LongMonitor longmonitor7 = (LongMonitor)vm.findByName(s1 + "time");
			LongMonitor longmonitor9 = (LongMonitor)vm.findByName(s1 + "lastEntryTime");
			LongMonitor longmonitor10 = (LongMonitor)vm.findByName(s1 + "lastExitTime");
			gcCollectors[j] = new GCCollector(s3, j, hrtFrequency, longmonitor5, longmonitor7, longmonitor9, longmonitor10, agcgeneration[j]);
		}

	}
}
