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
 * Cell renderer for the history of values. Each value is represented by a colored box, color
 * depends on value.
 */
public  class ColoredHistoryRenderer extends CustomTableCellRendererBase
{
    @Override
    protected void paintComponent(Graphics g) {
        Rectangle bounds = g.getClipBounds();
        g.setColor(getBackground());
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        int[] values = (int[]) table.getValueAt(row, column);
        int sum = 0;
        for (int value: values)
            sum += value;

        int height = getHeight();
        int width = getWidth();
        int partWidth = 25;
        for (int index = values.length - 1; index >= 0; index--) {
            Color color = determineColor(values[index]);
            int x0 = width - partWidth;
            int x1 = partWidth;
            width -= partWidth;
            float percentage = (float) Math.min(values[index], 100) / 100;
            int realHeigth = Math.round(percentage * height);
            int y0 = height - realHeigth;
            int y1 = height;
            g.setColor(color);
            g.fillRect(x0, y0, x1, y1);
            if (sum > 0) {
                g.setColor(Color.BLACK);
                g.drawString("" + values[index], x0 + 3, y1 - 5);
            }
            if (x0 <= 0)
                break;
        }
    }
}