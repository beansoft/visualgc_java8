package com.sun.jvmstat.graph;

import java.awt.Color;
import java.awt.Graphics;

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
    int var4 = (int)((double)(width * height) * this.splitLevel);
    int var5 = var4 / width;
    if (var5 > height) {
      var5 = height;
    }

    if (this.splitLevel != 0.0D && var5 == 0) {
      var5 = 1;
    }

    int var6;
    if (var5 != 0) {
      g.setColor(this.secondaryColor);

      for(var6 = 0; var6 < width; var6 += this.xIncrement) {
        g.drawLine(var6, 0, var6, var5 - 1);
      }

      g.drawLine(width - 1, 0, width - 1, var5 - 1);

      for(var6 = 0; var6 <= var5 - 1; var6 += this.yIncrement) {
        g.drawLine(0, var6, width - 1, var6);
      }
    }

    g.setColor(this.color);

    for(var6 = 0; var6 < width; var6 += this.xIncrement) {
      g.drawLine(var6, var5, var6, height - 1);
    }

    g.drawLine(width - 1, var5, width - 1, height - 1);
    if (this.splitLevel == 0.0D || var5 % this.yIncrement == 0) {
      g.drawLine(0, var5, width - 1, var5);
    }

    var5 = (var5 + this.yIncrement) / this.yIncrement * this.yIncrement;

    for(var6 = var5; var6 < height; var6 += this.yIncrement) {
      g.drawLine(0, var6, width - 1, var6);
    }

    g.drawLine(0, height - 1, width, height - 1);
  }
}
