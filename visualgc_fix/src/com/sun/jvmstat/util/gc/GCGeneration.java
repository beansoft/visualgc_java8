// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space
// Source File Name:   GCGeneration.java

package com.sun.jvmstat.util.gc;

import sun.jvmstat.monitor.LongMonitor;

public class GCGeneration
{

	private String name;
	private int id;
	private long size;
	private long minCapacity;
	private LongMonitor capacity;
	private GCSpace gcSpaces[];

	public GCGeneration(String s, int i, long l, long l1, LongMonitor longmonitor,
			GCSpace agcspace[])
	{
		name = s;
		id = i;
		size = l;
		minCapacity = l1;
		capacity = longmonitor;
		gcSpaces = agcspace;
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

	public long getMinCapacity()
	{
		return minCapacity;
	}

	public Number getCapacity()
	{
		return (Number)capacity.getValue();
	}

	public Number getUsed()
	{
		long l = 0L;
		for (int i = 0; i < gcSpaces.length; i++)
			l += gcSpaces[i].getUsed().longValue();

		return new Long(l);
	}

	public int spaces()
	{
		return gcSpaces.length;
	}

	public GCSpace[] getGCSpaces()
	{
		return gcSpaces;
	}

	public GCSpace getGCSpace(int i)
	{
		return gcSpaces[i];
	}
}
