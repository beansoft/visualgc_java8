//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.sun.jvmstat.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JComponent;

public class Line extends JComponent {
  private static final int DEFAULT_DATASET_SIZE = 1000;
  private static final int XSCALE = 2;
  private static final Color SPLIT_GRID_COLOR;
  private Color color;
  private GridDrawer gridDrawer;
  private FIFOList dataset;

  public Line(Color var1) {
    this(new FIFOList(1000), var1);
  }

  public Line(FIFOList var1) {
    this(var1, Color.LIGHT_GRAY);
  }

  public Line(FIFOList var1, Color var2) {
    this.dataset = var1;
    this.color = var2;
    this.gridDrawer = new GridDrawer(10, 10);
    this.gridDrawer.setSecondaryColor(SPLIT_GRID_COLOR);
    this.setPreferredSize(new Dimension(600, 100));
    this.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent var1) {
        super.componentResized(var1);
        Line.this.validate();
      }
    });
  }

  public Color getColor() {
    return this.color;
  }

  public void setColor(Color var1) {
    this.color = var1;
  }

  public Color getGridPrimaryColor() {
    return this.gridDrawer.getPrimaryColor();
  }

  public void setGridPrimaryColor(Color var1) {
    this.gridDrawer.setPrimaryColor(var1);
  }

  public Color getGridSecondaryColor() {
    return this.gridDrawer.getSecondaryColor();
  }

  public void setGridSecondaryColor(Color var1) {
    this.gridDrawer.setSecondaryColor(var1);
  }

  protected void drawGraph(Graphics var1) {
    if (!this.dataset.isEmpty()) {
      int var2 = this.getWidth();
      int var3 = this.getHeight();
      double var4 = this.dataset.getMaxValue();
      int var6 = this.dataset.size();
      int var7 = Math.min(var6, var2 / 2);
      double var8 = (double)var3 / var4;
      int[] var10 = new int[var7 + 2];
      int[] var11 = new int[var7 + 2];
      int var12 = var2 - var7 * 2;
      int var13 = var6 - var7;
      var10[0] = var12;
      var11[0] = var3;

      for(int var14 = 0; var14 < var7; ++var14) {
        double var15 = ((Number)((Number)this.dataset.get(var13 + var14))).doubleValue();
        int var17 = var3 - (int)(var15 * var8);
        if (var15 > 0.0D && var17 < 0) {
          var17 = 0;
        }

        if (var15 > 0.0D && var17 >= var3) {
          var17 = var3 - 1;
        }

        var10[var14 + 1] = var12 + var14 * 2;
        var11[var14 + 1] = var17;
      }

      var10[var7 + 1] = var2;
      var11[var7 + 1] = var3;
      var1.setColor(this.color);
      var1.fillPolygon(var10, var11, var7 + 2);
    }
  }

  public void updateGrayLevel(double var1) {
    this.gridDrawer.splitRange(var1);
  }

  public void paintComponent(Graphics var1) {
    super.paintComponent(var1);
    this.gridDrawer.draw(var1, this.getWidth(), this.getHeight());
    this.drawGraph(var1);
  }

  static {
    SPLIT_GRID_COLOR = Color.DARK_GRAY;
  }
}
