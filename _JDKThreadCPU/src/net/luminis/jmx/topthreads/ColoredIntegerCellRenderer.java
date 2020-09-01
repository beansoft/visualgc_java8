/*
 * Copyright 2007-2016 Peter Doornbosch
 *
 * This file is part of TopThreads, a JConsole plugin to analyse CPU-usage per thread.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * TopThreads is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package net.luminis.jmx.topthreads;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Cell renderer for the current value (percentage). Background of this cell is a colored box,
 * color depends on value.
 */
public class ColoredIntegerCellRenderer extends CustomTableCellRendererBase
{
    @Override
    protected void paintComponent(Graphics g) {
        Rectangle bounds = g.getClipBounds();
        g.setColor(getBackground());
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        int value = (Integer) table.getValueAt(row, column);
        Color color = determineColor(value);
        g.setColor(color);
        int width = getWidth();
        int height = getHeight();
        int colorHeight = Math.round(Math.min(value, 100) * height / 100);
        g.fillRect(0, height - colorHeight, width, height);
        g.setColor(Color.BLACK);
        g.drawString("" + value, 0 + 3, height - 5);
    }
}
