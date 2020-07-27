package com.sun.jvmstat.util;

import java.text.NumberFormat;

public class Converter
{

	private static final long K = 1024L;
	private static final long M = 0x100000L;
	private static final long G = 0x40000000L;
	private static final long T = 0x10000000000L;
	private static NumberFormat nf;

	private Converter()
	{
	}

	public static String longToKMGString(Number number)
	{
		return longToKMGString(number.longValue());
	}

	public static String longToKMGString(long l)
	{
		float f = l;
		float f1 = f / 1.073742E+009F;
		if ((long)f1 > 0L)
			return nf.format(f1) + "G";
		f1 = f / 1048576F;
		if ((long)f1 > 0L)
			return nf.format(f1) + "M";
		f1 = f / 1024F;
		if ((long)f1 > 0L)
			return nf.format(f1) + "K";
		else
			return String.valueOf(l);
	}

	public static String longToTimeString(Number number, Number number1)
	{
		return longToTimeString(number.longValue(), number1.longValue());
	}

	public static String longToTimeString(long l, long l1)
	{
		double d = l1;
		double d1 = d / 1000000000D;
		double d2 = d / 1000000D;
		double d3 = d / 1000D;
		double d4 = d * 60D;
		double d5 = d4 * 60D;
		double d6 = d5 * 24D;
		if (l == 0L)
			return "0s";
		StringBuffer stringbuffer = new StringBuffer();
		boolean flag = false;
		double d10 = l;
		double d7 = d10 / d6;
		long l2 = (long)d7;
		double d9 = d10 - (double)l2 * d6;
		if (l2 > 0L || flag)
		{
			stringbuffer.append(l2).append("d ");
			flag = true;
			if (l2 > 0L)
				d10 = d9;
		}
		d7 = d10 / d5;
		l2 = (long)d7;
		d9 = d10 - (double)l2 * d5;
		if (l2 > 0L || flag)
		{
			stringbuffer.append(l2).append("h ");
			flag = true;
			if (l2 > 0L)
				d10 = d9;
		}
		d7 = d10 / d4;
		l2 = (long)d7;
		d9 = d10 - (double)l2 * d4;
		if (l2 > 0L || flag)
		{
			stringbuffer.append(l2).append("m ");
			flag = true;
			if (l2 > 0L)
				d10 = d9;
		}
		d7 = d10 / d;
		l2 = (long)d7;
		d9 = d10 - (double)l2 * d;
		if (l2 > 0L || flag)
		{
			stringbuffer.append(nf.format(d7)).append("s ");
			flag = true;
			double d11;
			if (l2 > 0L)
				d11 = d9;
		}
		if (flag)
			return stringbuffer.toString();
		d7 = (double)l / d3;
		if (d7 >= 1.0D)
			return nf.format(d7) + "ms";
		d7 = (double)l / d2;
		if (d7 >= 1.0D)
		{
			return nf.format(d7) + "us";
		} else
		{
			double d8 = (double)l / d1;
			return nf.format(d8) + "ns";
		}
	}

	static
	{
		nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(3);
		nf.setMinimumFractionDigits(3);
		nf.setMinimumIntegerDigits(1);
	}
}
