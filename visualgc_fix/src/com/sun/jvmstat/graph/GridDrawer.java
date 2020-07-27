package com.sun.jvmstat.graph;

import java.awt.Color;
import java.awt.Graphics;

public class GridDrawer
{

	private static Color defaultColor = new Color(0, 130, 66);
	private Color color;
	private Color secondaryColor;
	private double splitLevel;
	private int xIncrement;
	private int yIncrement;

	public GridDrawer(int i, int j, Color color1)
	{
		xIncrement = i;
		yIncrement = j;
		color = color1;
	}

	public GridDrawer(int i, int j)
	{
		this(i, j, defaultColor);
	}

	public GridDrawer()
	{
		this(10, 10);
	}

	public Color getPrimaryColor()
	{
		return color;
	}

	public void setPrimaryColor(Color color1)
	{
		color = color1;
	}

	public Color getSecondaryColor()
	{
		return secondaryColor;
	}

	public void setSecondaryColor(Color color1)
	{
		secondaryColor = color1;
	}

	public void splitRange(double d, Color color1)
	{
		secondaryColor = color1;
		splitRange(d);
	}

	public void splitRange(double d)
	{
		splitLevel = d;
	}

	protected void draw(Graphics g, int i, int j)
	{
		int k = (int)((double)(i * j) * splitLevel);
		int l = k / i;
		if (l > j)
			l = j;
		if (splitLevel != 0.0D && l == 0)
			l = 1;
		if (l != 0)
		{
			g.setColor(secondaryColor);
			for (int i1 = 0; i1 < i; i1 += xIncrement)
				g.drawLine(i1, 0, i1, l - 1);

			g.drawLine(i - 1, 0, i - 1, l - 1);
			for (int j1 = 0; j1 <= l - 1; j1 += yIncrement)
				g.drawLine(0, j1, i - 1, j1);

		}
		g.setColor(color);
		for (int k1 = 0; k1 < i; k1 += xIncrement)
			g.drawLine(k1, l, k1, j - 1);

		g.drawLine(i - 1, l, i - 1, j - 1);
		if (splitLevel == 0.0D || l % yIncrement == 0)
			g.drawLine(0, l, i - 1, l);
		l = ((l + yIncrement) / yIncrement) * yIncrement;
		for (int l1 = l; l1 < j; l1 += yIncrement)
			g.drawLine(0, l1, i - 1, l1);

		g.drawLine(0, j - 1, i, j - 1);
	}

}
