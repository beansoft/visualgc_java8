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

import java.lang.Thread.State;

public abstract class AbstractInfoStats implements InfoStats
{
    static int defaultHistoryLength = 10;

    protected int nrOfValidValues;    // number of values set, including (current) percentage
    protected int percentage;
    protected int[] usageHistory;
    protected long lastCpuUsage;
    protected int historyIndex;
    protected int historyLength;

    public AbstractInfoStats()
    {
        historyLength = defaultHistoryLength;
        usageHistory = new int[historyLength];
    }

    @Override
    public int getPercentage()
    {
        return percentage;
    }

    @Override
    public long getCpuUsage()
    {
        return lastCpuUsage;
    }

    @Override
    public int getAverageUsage()
    {
        if (nrOfValidValues > 0) {
            int sum = percentage;
            // Invalid values are 0, so don't bother about using valid values only
            // (which would be tricky, because valid values are not necessarily positioned
            // at the first positions in the array)
            for (int value: usageHistory) {
                sum += value;
            }
            return sum / nrOfValidValues;
        }
        else
            return 0;
    }

    @Override
    public int[] getHistory()
    {
        int[] values = new int[historyLength];
        for (int count = 0; count < historyLength; count++) {
            int index = (historyIndex + count) % historyLength;
            values[count] = usageHistory[index];
        }
        return values;
    }

    @Override
    public boolean getSelect()
    {
        return false;
    }

    @Override
    public State getState()
    {
        return null;
    }

    @Override
    public void setIndex(int index)
    {
    }

    @Override
    public void setSelect(boolean on)
    {
    }



}
