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

public class ProcessInfoStats extends AbstractInfoStats
{
    private String name;

    public ProcessInfoStats(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public boolean getSelect() {
        return false;
    }

    @Override
    public long getId() {
        return -1;
    }

    @Override
    public void setIndex(int index) {
    }

    @Override
    public void setSelect(boolean on) {
    }

    public long update(long cpuTime)
    {
        // First, save previous value in history.
        usageHistory[historyIndex] = percentage;
        historyIndex = ++historyIndex % historyLength;    // The first real value will have index 1, but what the hack.
        if (nrOfValidValues <= historyLength) {
            // Note that nrOfValidValues will eventually be 1 larger than history length,
            // because the (current) percentage is also a valid value, and used for computing
            // the average, see getAverageUsage()
            nrOfValidValues++;
        }

        lastCpuUsage = cpuTime;
        return lastCpuUsage;
    }

    public void setPercentage(int cpuUsagePercentage)
    {
        percentage = cpuUsagePercentage;
    }
}
