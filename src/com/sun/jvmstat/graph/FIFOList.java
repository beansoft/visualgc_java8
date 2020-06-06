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

    Number firstElement = null;
    if (this.size() > this.maxSize) {
      firstElement = (Number)this.get(0);
      super.remove(0);
      timeList.remove(0);
    }

    boolean added = super.add(value);

    if (this.autoRange) {
      if (firstElement == null) {
        this.recomputeRange(value.doubleValue());
      } else {
        this.recomputeRange(firstElement.doubleValue(), value.doubleValue());
      }
    }

    return added;
  }

  public Long getTimestamp(int idx) {
    return timeList.get(idx);
  }

  private void recomputeRange(double value) {
    if (value > this.maxValue) {
      this.maxValue = value;
    } else if (value < this.minValue) {
      this.minValue = value;
    }

  }

  private void recomputeRange(double var1, double var3) {
    this.maxValue = this.recomputeMaxValue(var1, var3);
    this.minValue = this.recomputeMinValue(var1, var3);
  }

  private double recomputeMaxValue(double var1, double var3) {
    if (var3 >= this.maxValue) {
      return var3;
    } else {
      return var1 == this.maxValue ? this.findMaxValue() : this.maxValue;
    }
  }

  private double recomputeMinValue(double var1, double var3) {
    if (var3 <= this.minValue) {
      return var3;
    } else {
      return var1 == this.minValue ? this.findMinValue() : this.minValue;
    }
  }

  private double findMinValue() {
    double minValue = Double.MAX_VALUE;
    Iterator var3 = this.iterator();

    while(var3.hasNext()) {
      Number var4 = (Number)var3.next();
      double var5 = var4.doubleValue();
      if (var5 < minValue) {
        minValue = var5;
      }
    }

    return minValue;
  }

  private double findMaxValue() {
    double var1 = Double.MIN_VALUE;
    Iterator var3 = this.iterator();

    while(var3.hasNext()) {
      Number var4 = (Number)var3.next();
      double var5 = var4.doubleValue();
      if (var5 > var1) {
        var1 = var5;
      }
    }

    return var1;
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
