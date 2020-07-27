package com.sun.jvmstat.util;

import java.text.NumberFormat;

public class Converter {
  private static final long K = 1024L;

  private static final long M = 1048576L;

  private static final long G = 1073741824L;

  private static final long T = 1099511627776L;

  private static NumberFormat nf = NumberFormat.getNumberInstance();

  static {
    nf.setMaximumFractionDigits(3);
    nf.setMinimumFractionDigits(3);
    nf.setMinimumIntegerDigits(1);
  }

  public static String longToKMGString(Number value) {
    return longToKMGString(value.longValue());
  }

  public static String longToKMGString(long value) {
    float fvalue = (float)value;
    float rep = fvalue / 1.07374182E9F;
    if ((long)rep > 0L)
      return nf.format(rep) + "G";
    rep = fvalue / 1048576.0F;
    if ((long)rep > 0L)
      return nf.format(rep) + "M";
    rep = fvalue / 1024.0F;
    if ((long)rep > 0L)
      return nf.format(rep) + "K";
    return String.valueOf(value);
  }

  public static String longToTimeString(Number ticks, Number frequency) {
    return longToTimeString(ticks.longValue(), frequency.longValue());
  }

  public static String longToTimeString(long ticks, long frequency) {
    double S = frequency;
    double NS = S / 1.0E9D;
    double US = S / 1000000.0D;
    double MS = S / 1000.0D;
    double MIN = S * 60.0D;
    double HOUR = MIN * 60.0D;
    double DAY = HOUR * 24.0D;
    if (ticks == 0L)
      return "0s";
    StringBuffer sb = new StringBuffer();
    boolean hasPredecessor = false;
    double previous = ticks;
    double rep = previous / DAY;
    long longValue = (long)rep;
    double rem = previous - longValue * DAY;
    if (longValue > 0L || hasPredecessor) {
      sb.append(longValue).append("d ");
      hasPredecessor = true;
      if (longValue > 0L)
        previous = rem;
    }
    rep = previous / HOUR;
    longValue = (long)rep;
    rem = previous - longValue * HOUR;
    if (longValue > 0L || hasPredecessor) {
      sb.append(longValue).append("h ");
      hasPredecessor = true;
      if (longValue > 0L)
        previous = rem;
    }
    rep = previous / MIN;
    longValue = (long)rep;
    rem = previous - longValue * MIN;
    if (longValue > 0L || hasPredecessor) {
      sb.append(longValue).append("m ");
      hasPredecessor = true;
      if (longValue > 0L)
        previous = rem;
    }
    rep = previous / S;
    longValue = (long)rep;
    rem = previous - longValue * S;
    if (longValue > 0L || hasPredecessor) {
      sb.append(nf.format(rep)).append("s ");
      hasPredecessor = true;
      if (longValue > 0L)
        previous = rem;
    }
    if (hasPredecessor)
      return sb.toString();
    rep = ticks / MS;
    if (rep >= 1.0D)
      return nf.format(rep) + "ms";
    rep = ticks / US;
    if (rep >= 1.0D)
      return nf.format(rep) + "us";
    rep = ticks / NS;
    return nf.format(rep) + "ns";
  }
}
