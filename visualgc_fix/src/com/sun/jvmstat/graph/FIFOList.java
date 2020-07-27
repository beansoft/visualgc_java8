package com.sun.jvmstat.graph;

import java.util.ArrayList;
import java.util.Iterator;

public class FIFOList extends ArrayList
{

	private int maxSize;
	private double maxValue;
	private double minValue;
	private boolean autoRange;

	public FIFOList(int i, double d, double d1)
	{
		super(i);
		maxSize = i;
		minValue = d;
		maxValue = d1;
		autoRange = false;
	}

	public FIFOList(int i, boolean flag)
	{
		this(i, 1.7976931348623157E+308D, 4.9406564584124654E-324D);
		autoRange = flag;
	}

	public FIFOList(int i)
	{
		this(i, true);
	}

	public boolean add(Number number)
	{
		Number number1 = null;
		if (size() > maxSize)
		{
			number1 = (Number)get(0);
			super.remove(0);
		}
		boolean flag = super.add(number);
		if (autoRange)
			if (number1 == null)
				recomputeRange(number.doubleValue());
			else
				recomputeRange(number1.doubleValue(), number.doubleValue());
		return flag;
	}

	private void recomputeRange(double d)
	{
		if (d > maxValue)
			maxValue = d;
		else
		if (d < minValue)
			minValue = d;
	}

	private void recomputeRange(double d, double d1)
	{
		maxValue = recomputeMaxValue(d, d1);
		minValue = recomputeMinValue(d, d1);
	}

	private double recomputeMaxValue(double d, double d1)
	{
		if (d1 >= maxValue)
			return d1;
		if (d == maxValue)
			return findMaxValue();
		else
			return maxValue;
	}

	private double recomputeMinValue(double d, double d1)
	{
		if (d1 <= minValue)
			return d1;
		if (d == minValue)
			return findMinValue();
		else
			return minValue;
	}

	private double findMinValue()
	{
		double d = 1.7976931348623157E+308D;
		Iterator iterator = iterator();
		do
		{
			if (!iterator.hasNext())
				break;
			Number number = (Number)iterator.next();
			double d1 = number.doubleValue();
			if (d1 < d)
				d = d1;
		} while (true);
		return d;
	}

	private double findMaxValue()
	{
		double d = 4.9406564584124654E-324D;
		Iterator iterator = iterator();
		do
		{
			if (!iterator.hasNext())
				break;
			Number number = (Number)iterator.next();
			double d1 = number.doubleValue();
			if (d1 > d)
				d = d1;
		} while (true);
		return d;
	}

	public boolean isAutoRange()
	{
		return autoRange;
	}

	public void setAutoRange(boolean flag)
	{
		if (!autoRange && flag)
		{
			maxValue = findMaxValue();
			minValue = findMinValue();
		}
		autoRange = flag;
	}

	public double getMaxValue()
	{
		return maxValue;
	}

	public double getMinValue()
	{
		return minValue;
	}

	public void setMaxValue(double d)
	{
		if (autoRange)
		{
			throw new IllegalStateException();
		} else
		{
			maxValue = d;
			return;
		}
	}

	public void setMinValue(double d)
	{
		if (autoRange)
		{
			throw new IllegalStateException();
		} else
		{
			minValue = d;
			return;
		}
	}

	public Object clone()
	{
		FIFOList fifolist = (FIFOList)super.clone();
		fifolist.maxSize = maxSize;
		fifolist.maxValue = maxValue;
		fifolist.minValue = minValue;
		fifolist.autoRange = autoRange;
		return fifolist;
	}
}
