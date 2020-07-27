package com.sun.jvmstat.graph;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JComponent;

public class Level extends JComponent
{

	private Color color;
	private double level;
	private GridDrawer gridDrawer;

	public Level(Color color1)
	{
		color = color1;
		gridDrawer = new GridDrawer(10, 10);
	}

	public Level()
	{
		this(Color.WHITE);
	}

	public void draw()
	{
		repaint();
	}

	public void update(Graphics g)
	{
		paint(g);
	}

	public void updateLevel(double d)
	{
		level = d;
	}

	public void updateGrayLevel(double d)
	{
		gridDrawer.splitRange(d, Color.DARK_GRAY);
	}

	public void drawLevel(Graphics g)
	{
		int i = getHeight();
		int j = getWidth();
		int k = (int)((double)(j * i) * level);
		int l = k / j;
		if (l > i)
			l = i;
		if (level != 0.0D && l == 0)
			l = 1;
		g.setColor(color);
		g.fill3DRect(0, i - l, j, i, true);
	}

	public Color getColor()
	{
		return color;
	}

	public void paint(Graphics g)
	{
		gridDrawer.draw(g, getWidth(), getHeight());
		drawLevel(g);
		super.paint(g);
	}
}
