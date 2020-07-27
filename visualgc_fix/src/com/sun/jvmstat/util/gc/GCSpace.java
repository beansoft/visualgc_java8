package com.sun.jvmstat.util.gc;

import sun.jvmstat.monitor.LongMonitor;

public class GCSpace
{

	private String name;
	private int id;
	private long size;
	private LongMonitor capacity;
	private LongMonitor used;

	public GCSpace(String s, int i, long l, LongMonitor longmonitor, LongMonitor longmonitor1)
	{
		name = s;
		id = i;
		size = l;
		capacity = longmonitor;
		used = longmonitor1;
	}

	public String getName()
	{
		return name;
	}

	public int getId()
	{
		return id;
	}

	public long getSize()
	{
		return size;
	}

	public Number getCapacity()
	{
		return (Number)capacity.getValue();
	}

	public Number getUsed()
	{
		return (Number)used.getValue();
	}
}
