package com.sun.jvmstat.util.gc;

import sun.jvmstat.monitor.LongMonitor;

public class GCCollector
{

	private String name;
	private int id;
	private LongMonitor invocations;
	private LongMonitor time;
	private LongMonitor lastEntry;
	private LongMonitor lastExit;
	private GCGeneration generations[];
	private long hrtFrequency;
	private long reservedSize;

	public GCCollector(String s, int i, long l, LongMonitor longmonitor, LongMonitor longmonitor1, LongMonitor longmonitor2,
			LongMonitor longmonitor3, GCGeneration agcgeneration[])
	{
		name = s;
		id = i;
		invocations = longmonitor;
		time = longmonitor1;
		lastEntry = longmonitor2;
		lastExit = longmonitor3;
		generations = agcgeneration;
		hrtFrequency = l;
	}

	public String getName()
	{
		return name;
	}

	public int getId()
	{
		return id;
	}

	public Number getInvocations()
	{
		return (Number)invocations.getValue();
	}

	public Number getTime()
	{
		return (Number)time.getValue();
	}

	public Number getLastEntry()
	{
		return (Number)lastEntry.getValue();
	}

	public Number getLastExit()
	{
		return (Number)lastExit.getValue();
	}

	public long getHrtFrequency()
	{
		return hrtFrequency;
	}

	private long computeReservedSize()
	{
		long l = 0L;
		for (int i = 0; i < generations.length; i++)
			l += generations[i].getSize();

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
		for (int i = 0; i < generations.length; i++)
			l += generations[i].getCapacity().longValue();

		return new Long(l);
	}

	public Number getUsed()
	{
		long l = 0L;
		for (int i = 0; i < generations.length; i++)
			l += generations[i].getUsed().longValue();

		return new Long(l);
	}

	public int generations()
	{
		return generations.length;
	}

	public GCGeneration[] getGCGenerations()
	{
		return generations;
	}

	public GCGeneration getGCGeneration(int i)
	{
		return generations[i];
	}
}
