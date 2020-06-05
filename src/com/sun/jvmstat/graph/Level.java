package com.sun.jvmstat.graph;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JComponent;

public class Level extends JComponent {
  private Color color;
  private double level;
  private GridDrawer gridDrawer;

  public Level(Color var1) {
    this.color = var1;
    this.gridDrawer = new GridDrawer(10, 10);
  }

  public Level() {
    this(Color.WHITE);
  }

  public void draw() {
    this.repaint();
  }

  public void update(Graphics g) {
    this.paint(g);
  }

  public void updateLevel(double level) {
    this.level = level;
  }

  public void updateGrayLevel(double grayLevel) {
    this.gridDrawer.splitRange(grayLevel, Color.DARK_GRAY);
  }

  public void drawLevel(Graphics var1) {
    int var2 = this.getHeight();
    int var3 = this.getWidth();
    int var4 = (int)((double)(var3 * var2) * this.level);
    int var5 = var4 / var3;
    if (var5 > var2) {
      var5 = var2;
    }

    if (this.level != 0.0D && var5 == 0) {
      var5 = 1;
    }

    var1.setColor(this.color);
    var1.fill3DRect(0, var2 - var5, var3, var2, true);
  }

  public Color getColor() {
    return this.color;
  }

  public void paint(Graphics g) {
    this.gridDrawer.draw(g, this.getWidth(), this.getHeight());
    this.drawLevel(g);
    super.paint(g);
  }
}
