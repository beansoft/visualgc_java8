package com.sun.jvmstat.graph;

import com.yworks.util.annotation.Obfuscation;

import javax.swing.*;
import java.awt.*;

//  空间和存活直方图空间中的图表
public class Level extends JComponent {
  private Color color;
  private double level;
  @Obfuscation
  private GridDrawer gridDrawer;

  public Level(Color color) {
    this.color = color;
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

  public void updateLevel(double ratio) {
    this.level = ratio;
  }

  public void updateGrayLevel(double ratio) {
    this.gridDrawer.splitRange(ratio, Color.DARK_GRAY);
  }


  public void drawLevel(Graphics g) {
    int height = getHeight();
    int width = getWidth();
    int area = (int)((width * height) * this.level);
    int levelHeight = area / width;
    if (levelHeight > height)
      levelHeight = height;
    if (this.level != 0.0D && levelHeight == 0)
      levelHeight = 1;
    g.setColor(this.color);
    g.fill3DRect(0, height - levelHeight, width, height, true);
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
