package com.sun.jvmstat.graph;

import java.awt.*;

public class GridDrawer {
  private static Color defaultColor = new Color(0, 130, 66);
  private Color color;
  private Color secondaryColor;
  private double splitLevel;
  private int xIncrement;
  private int yIncrement;

  public GridDrawer(int xIncrement, int yIncrement, Color color) {
    this.xIncrement = xIncrement;
    this.yIncrement = yIncrement;
    this.color = color;
  }

  public GridDrawer(int xIncrement, int yIncrement) {
    this(xIncrement, yIncrement, defaultColor);
  }

  public GridDrawer() {
    this(10, 10);
  }

  public Color getPrimaryColor() {
    return this.color;
  }

  public void setPrimaryColor(Color color) {
    this.color = color;
  }

  public Color getSecondaryColor() {
    return this.secondaryColor;
  }

  public void setSecondaryColor(Color secondaryColor) {
    this.secondaryColor = secondaryColor;
  }

  public void splitRange(double splitLevel, Color secondaryColor) {
    this.secondaryColor = secondaryColor;
    this.splitRange(splitLevel);
  }

  public void splitRange(double splitLevel) {
    this.splitLevel = splitLevel;
  }

  protected void draw(Graphics g, int width, int height) {
    int splitArea = (int)((double)(width * height) * this.splitLevel);
    int splitHeight = splitArea / width;
    if (splitHeight > height) {
      splitHeight = height;
    }

    if (this.splitLevel != 0.0D && splitHeight == 0) {
      splitHeight = 1;
    }

    int i;
    if (splitHeight != 0) {
      g.setColor(this.secondaryColor);

      for(i = 0; i < width; i += this.xIncrement) {
        g.drawLine(i, 0, i, splitHeight - 1);
      }

      g.drawLine(width - 1, 0, width - 1, splitHeight - 1);

      for(i = 0; i <= splitHeight - 1; i += this.yIncrement) {
        g.drawLine(0, i, width - 1, i);
      }
    }

    g.setColor(this.color);

    for(i = 0; i < width; i += this.xIncrement) {
      g.drawLine(i, splitHeight, i, height - 1);
    }

    g.drawLine(width - 1, splitHeight, width - 1, height - 1);
    if (this.splitLevel == 0.0D || splitHeight % this.yIncrement == 0) {
      g.drawLine(0, splitHeight, width - 1, splitHeight);
    }

    splitHeight = (splitHeight + this.yIncrement) / this.yIncrement * this.yIncrement;

    for(i = splitHeight; i < height; i += this.yIncrement) {
      g.drawLine(0, i, width - 1, i);
    }

    g.drawLine(0, height - 1, width, height - 1);
  }

}
