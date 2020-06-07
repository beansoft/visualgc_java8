package com.sun.jvmstat.graph;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FIFOList extends ArrayList<Number> {
  private int maxSize;
  private double maxValue;
  private double minValue;
  private boolean autoRange;

  private List<Long> timeList;

  public static transient long timeStamp = 0;

  public FIFOList(int maxSize, double minValue, double maxValue) {
    super(maxSize);
    this.maxSize = maxSize;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.autoRange = false;

    timeList = new ArrayList<Long>(maxSize);
  }

  public FIFOList(int maxSize, boolean autoRange) {
    this(maxSize, Double.MAX_VALUE, Double.MIN_VALUE);

    this.autoRange = autoRange;
  }

  public FIFOList(int maxSize) {
    this(maxSize, true);
  }

  public boolean add(Number value) {
    if(timeStamp == 0) {
      timeStamp = System.currentTimeMillis();
    }
    timeList.add(timeStamp);

    Number removed = null;
    if (this.size() > this.maxSize) {
      removed = this.get(0);
      super.remove(0);
      timeList.remove(0);
    }

    boolean rv = super.add(value);

    if (this.autoRange) {
      if (removed == null) {
        this.recomputeRange(value.doubleValue());
      } else {
        this.recomputeRange(removed.doubleValue(), value.doubleValue());
      }
    }

    return rv;
  }

  public Long getTimestamp(int idx) {
    return timeList.get(idx);
  }

  private void recomputeRange(double newValue) {
    if (newValue > this.maxValue) {
      this.maxValue = newValue;
    } else if (newValue < this.minValue) {
      this.minValue = newValue;
    }

  }

  private void recomputeRange(double oldValue, double newValue) {
    this.maxValue = recomputeMaxValue(oldValue, newValue);
    this.minValue = recomputeMinValue(oldValue, newValue);
  }


  private double recomputeMaxValue(double oldValue, double newValue) {
    if (newValue >= this.maxValue)
      return newValue;
    if (oldValue == this.maxValue)
      return findMaxValue();
    return this.maxValue;
  }

  private double recomputeMinValue(double oldValue, double newValue) {
    if (newValue <= this.minValue)
      return newValue;
    if (oldValue == this.minValue)
      return findMinValue();
    return this.minValue;
  }

  private double findMinValue() {
    double min = Double.MAX_VALUE;
    for (Iterator i = iterator(); i.hasNext(); ) {
      Number n = (Number)i.next();
      double value = n.doubleValue();
      if (value < min)
        min = value;
    }
    return min;
  }

  private double findMaxValue() {
    double max = Double.MIN_VALUE;
    for (Iterator i = iterator(); i.hasNext(); ) {
      Number n = (Number)i.next();
      double value = n.doubleValue();
      if (value > max)
        max = value;
    }
    return max;
  }

  public boolean isAutoRange() {
    return this.autoRange;
  }

  public void setAutoRange(boolean autoRange) {
    if (!this.autoRange && autoRange) {
      this.maxValue = this.findMaxValue();
      this.minValue = this.findMinValue();
    }

    this.autoRange = autoRange;
  }

  public double getMaxValue() {
    return this.maxValue;
  }

  public double getMinValue() {
    return this.minValue;
  }

  public void setMaxValue(double maxValue) {
    if (this.autoRange) {
      throw new IllegalStateException();
    } else {
      this.maxValue = maxValue;
    }
  }

  public void setMinValue(double minValue) {
    if (this.autoRange) {
      throw new IllegalStateException();
    } else {
      this.minValue = minValue;
    }
  }

  public Object clone() {
    FIFOList var1 = (FIFOList)super.clone();
    var1.maxSize = this.maxSize;
    var1.maxValue = this.maxValue;
    var1.minValue = this.minValue;
    var1.autoRange = this.autoRange;
    return var1;
  }
}
