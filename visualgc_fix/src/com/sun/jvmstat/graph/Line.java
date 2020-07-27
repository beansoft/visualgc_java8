package com.sun.jvmstat.graph;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JComponent;

public class Line extends JComponent
{

	private static final int DEFAULT_DATASET_SIZE = 1000;
	private static final int XSCALE = 2;
	private static final Color SPLIT_GRID_COLOR;
	private Color color;
	private GridDrawer gridDrawer;
	private FIFOList dataset;

	public Line(Color color1)
	{
		this(new FIFOList(1000), color1);
	}

	public Line(FIFOList fifolist)
	{
		this(fifolist, Color.LIGHT_GRAY);
	}

	public Line(FIFOList fifolist, Color color1)
	{
		dataset = fifolist;
		color = color1;
		gridDrawer = new GridDrawer(10, 10);
		gridDrawer.setSecondaryColor(SPLIT_GRID_COLOR);
		setPreferredSize(new Dimension(600, 100));
		addComponentListener(new ComponentAdapter() {

			public void componentResized(ComponentEvent componentevent)
			{
				super.componentResized(componentevent);
				validate();
			}


		}
);
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color1)
	{
		color = color1;
	}

	public Color getGridPrimaryColor()
	{
		return gridDrawer.getPrimaryColor();
	}

	public void setGridPrimaryColor(Color color1)
	{
		gridDrawer.setPrimaryColor(color1);
	}

	public Color getGridSecondaryColor()
	{
		return gridDrawer.getSecondaryColor();
	}

	public void setGridSecondaryColor(Color color1)
	{
		gridDrawer.setSecondaryColor(color1);
	}

	protected void drawGraph(Graphics g)
	{
		if (dataset.isEmpty())
			return;
		int i = getWidth();
		int j = getHeight();
		double d = dataset.getMaxValue();
		int k = dataset.size();
		int l = Math.min(k, i / 2);
		double d1 = (double)j / d;
		int ai[] = new int[l + 2];
		int ai1[] = new int[l + 2];
		int i1 = i - l * 2;
		int j1 = k - l;
		ai[0] = i1;
		ai1[0] = j;
		for (int k1 = 0; k1 < l; k1++)
		{
			double d2 = ((Number)(Number)dataset.get(j1 + k1)).doubleValue();
			int l1 = j - (int)(d2 * d1);
			if (d2 > 0.0D && l1 < 0)
				l1 = 0;
			if (d2 > 0.0D && l1 >= j)
				l1 = j - 1;
			ai[k1 + 1] = i1 + k1 * 2;
			ai1[k1 + 1] = l1;
		}

		ai[l + 1] = i;
		ai1[l + 1] = j;
		g.setColor(color);
		g.fillPolygon(ai, ai1, l + 2);
	}

	public void updateGrayLevel(double d)
	{
		gridDrawer.splitRange(d);
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		gridDrawer.draw(g, getWidth(), getHeight());
		drawGraph(g);
	}

	static
	{
		SPLIT_GRID_COLOR = Color.DARK_GRAY;
	}
}
