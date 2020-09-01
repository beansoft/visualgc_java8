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

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;

public class CustomTableCellRendererBase extends JComponent implements TableCellRenderer
{
    protected int row;
    protected int column;
    protected JTable table;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column) {
        this.table = table;
        this.row = row;
        this.column = column;
        return this;
    }

    protected Color determineColor(int percentage) {
        // Guard against out-of-range values
        if (percentage < 0)
            percentage = 0;
        if (percentage > 100)
            percentage = 100;

        int red, green, blue;
        if (percentage < 50) {
            // Green to yellow gradient
            red = (int) (5.1 * percentage);
            green = 255;
        } else {
            // Yellow to read gradient
            red = 255;
            green = 255 - (int) ((percentage - 50) * 5.1);
        }
        blue = 0;
        return new Color(red, green, blue);
    }
}
