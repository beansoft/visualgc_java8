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

import java.lang.management.ThreadInfo;
import java.util.Comparator;


class ThreadInfoStats extends AbstractInfoStats
{
	private ThreadInfo threadInfo;
	private long threadId;
	private long previousCpuTime;
	private int updateCount;
	private boolean showTrace;
	private int index;	            // for fixing table order

	public ThreadInfoStats(long id, ThreadInfo current, long cpuTime)
	{
		threadId = id;
		threadInfo = current;
		this.previousCpuTime = cpuTime;
		usageHistory = new int[historyLength];
	}

    @Override
    public long getId() {
        return threadId;
    }

    @Override
    public String getName() {
        return threadInfo.getThreadName();
    }

	@Override
    public Thread.State getState()
	{
		return threadInfo.getThreadState();
	}

    @Override
    public boolean getSelect() {
        return showTrace;
    }

    @Override
    public void setSelect(boolean on) {
        showTrace = on;
    }

    @Override
    public void setIndex(int i) {
        index = i;
    }

    /**
     *
     * @param current
     * @param cpuTime
     * @return   the cpu usage of the thread since the last update
     */
	public long update(ThreadInfo current, long cpuTime)
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

        // Update current values
		threadInfo = current;
		this.lastCpuUsage = cpuTime - previousCpuTime;
        if (lastCpuUsage < 0) {
            // Can happen when reconnecting to an existing process, JVM may "choose" to restart counters
            lastCpuUsage = 0;
        }
		this.previousCpuTime = cpuTime;
		this.updateCount++;

		return this.lastCpuUsage;
	}

	public boolean checkUpdate(int currentUpdate) {
		if (updateCount != currentUpdate) {
			//System.out.println("No update for thread " + threadInfo.getThreadName());
			assert updateCount == currentUpdate - 1 || updateCount == 0;
			this.lastCpuUsage = 0;
			updateCount = currentUpdate;
			// is this thread dead? According to the javadoc it should be, but in practice its not.
			return false;
		}
		return true;
	}

	public void computePercentage(long totalCpuTime) {
		if (totalCpuTime != 0)
			this.percentage = (int) (100 * lastCpuUsage / totalCpuTime);
	}

	public String getHistoryAsString() {
		StringBuilder history = new StringBuilder();
		boolean notAllZero = false;
		for (int count = 0; count < historyLength; count++) {
			int index = (historyIndex + count) % historyLength;
			int value = usageHistory[index];
			if (value > 0)
				notAllZero = true;

			history.append(value);
			history.append(" ");
		}
		if (notAllZero)
			return history.toString();
		else
			return "";
	}

	static Comparator<InfoStats> lastUsageComparator()
	{
		return new Comparator<InfoStats>()
		{
			public int compare(InfoStats i1, InfoStats i2) {
                ThreadInfoStats o1 = null;
                ThreadInfoStats o2 = null;
                if (i1 instanceof ThreadInfoStats)
                    o1 = (ThreadInfoStats) i1;
                if (i2 instanceof ThreadInfoStats)
                    o2 = (ThreadInfoStats) i2;
                if (o1 == null && o2 == null)
                    return 0;
                if (o1 == null)
                    return -1;
                if (o2 == null)
                    return 1;

				if (o1.lastCpuUsage < o2.lastCpuUsage)
					return 1;
				else if (o1.lastCpuUsage > o2.lastCpuUsage)
					return -1;
				else {
					if (o1.previousCpuTime < o2.previousCpuTime)
						return 1;
					else if (o1.previousCpuTime > o2.previousCpuTime)
						return -1;
					else
						return 0;
				}
			}
		};
	}

	public static Comparator<InfoStats> fixOrderComparator() {
		return new Comparator<InfoStats>()
		{
			public int compare(InfoStats i1, InfoStats i2) {
                ThreadInfoStats o1 = null;
                ThreadInfoStats o2 = null;
                if (i1 instanceof ThreadInfoStats)
                    o1 = (ThreadInfoStats) i1;
                if (i2 instanceof ThreadInfoStats)
                    o2 = (ThreadInfoStats) i2;
                if (o1 == null && o2 == null)
                    return 0;
                if (o1 == null)
                    return -1;
                if (o2 == null)
                    return 1;

                // If both are new (newer than when fixing the order), sort on usage
				if (o1.index == 0 && o2.index == 0)
					return new Long(o1.lastCpuUsage).compareTo(o2.lastCpuUsage);
				// Else if one of them is null, consider it '>', so it will sort later
				if (o1.index == 0)
					return 1;
				if (o2.index == 0)
					return -1;
				return new Integer(o1.index).compareTo(o2.index);
			}
		};
	}

}
