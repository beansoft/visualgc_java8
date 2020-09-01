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
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreadDataCollector
{
    private ThreadMXBean threadMXBean;
    /** Counts the number of updates, in order to be able to detect threads that died. */
    private int updateCount;
    private boolean debug;

    /**
     * The map that holds the stats, mapped by thread id. Doesn't have to be sync'd, because
     *  it's used in a sync'd method only.
     */
    private Map<Long, ThreadInfoStats> threadData = new HashMap<Long, ThreadInfoStats>();
    /** The one and only process info object */
    private ProcessInfoStats processInfo = new ProcessInfoStats("Process (% CPU of all threads together)");
    /** Start time of measurement period, used to compute actual period length. */
    private long startMeasurement;

    /** cpu usage percentage as computed at end of last period */
    private int cpuUsagePercentage;

    /** the counters... **/
    private int threadsBlocked;
    private int threadsRunning;
    private int threadsTimedWaiting;
    private int threadsWaiting;

    public ThreadDataCollector(ThreadMXBean threadMXBean, boolean debug)
    {
        this.threadMXBean = threadMXBean;
        this.debug = debug;
    }

    public Data getThreadData(int maxThreadsDisplayed, boolean includeCpuUsageHistory, boolean fixOrder, Comparator<InfoStats> comparator, Long tracedThread, int maxStackTraceDepth) {
        Data data = new Data();

        try {
            List<? extends InfoStats> list = getThreadInfo(includeCpuUsageHistory);
            // Sort the list
            Collections.sort(list, comparator);
            if (!fixOrder) {
                // Re-number them, so we've something to sort on when order becomes fixed
                int index = 1;
                for (InfoStats info : list) {
                    info.setIndex(index++);
                }
            }

            data.threadList = list.subList(0, Math.min(list.size(), maxThreadsDisplayed + 1));
            data.threadStats = getStats();
            data.threadCount = list.size() - (includeCpuUsageHistory? 1: 0);
            data.cpuUsagePercentage = getCpuUsagePercentage();

            if (tracedThread != null)
                data.stackTrace = retrieveStackTrace(tracedThread, maxStackTraceDepth);
            else {
                data.stackTrace = new StackTraceElement[0];
            }

        } catch (Exception error) {
            data.error = error;
        }
        return data;
    }

    // Sync'd to guard against changing threadMXBean (setting it to null) and to ensure
    // that this method is never called concurrently (bad for performance and pretty useless)
    private synchronized List<? extends InfoStats> getThreadInfo(boolean showCpuUsage) throws SecurityException
    {
        if (threadMXBean == null) {
            return Collections.emptyList();
        }

        updateCount++;
        long[] threadIds = null;
        ThreadInfo[] threadInfos = null;
        try {
            threadIds = threadMXBean.getAllThreadIds();
            threadInfos = threadMXBean.getThreadInfo(threadIds);
        }
        catch (UndeclaredThrowableException ute) {
            if (debug)
                System.err.println("error while getting thread info: " + ute + " caused by " + ute.getCause());
            return Collections.emptyList();
        }
        catch (SecurityException se) {
            throw se;
        }
        catch (Exception e) {
            if (debug) {
                System.err.println("error while getting thread info: " + e);
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        long start = System.currentTimeMillis();
        long totalCpuTime = 0;
        for (int i = 0; i < threadIds.length; i++) {
            assert threadInfos[i].getThreadId() == threadIds[i];
            long id = threadIds[i];

            if (threadInfos[i] != null) {
                long cpuTime = threadMXBean.getThreadCpuTime(id);

                if (cpuTime != -1) {
                    ThreadInfoStats stats = threadData.get(id);
                    if (stats == null) {
                        stats = new ThreadInfoStats(id, threadInfos[i], cpuTime);
                        threadData.put(id, stats);
                    }
                    else {
                        totalCpuTime += stats.update(threadInfos[i], cpuTime);
                    }
                }
            }
        }
        long end = System.currentTimeMillis();

        // Compute cpu usage, based on total cpu time over all threads per second
        long previousStart = startMeasurement;
        startMeasurement = start + ((end - start) / 2);
        if (previousStart != 0) {
            long measurementPeriod = (startMeasurement - previousStart); // ms

            long lastTotalCpuTime = totalCpuTime / 1000; // from nano seconds to micro seconds
            lastTotalCpuTime = 1000 * lastTotalCpuTime / measurementPeriod; // per second
            cpuUsagePercentage = (int) ((100 * lastTotalCpuTime) / 1000000);
            if (debug)
                System.out.println(String.format("Obtaining cputimes took %3d ms, measurement period was %d ms, current total (/s) = %d",
                        end - start, measurementPeriod, lastTotalCpuTime));
        }

        // We're going to count all thread states, so reset counters first.
        resetStateCounts();
        // Get all thread info as a list.
        List<ThreadInfoStats> list = new ArrayList(threadData.values());
        // Fix stats for threads that have no update
        for (ThreadInfoStats info: list) {
            if (! info.checkUpdate(updateCount)) {
                // Thread wasn't included in all thread info, check if it's still there
                if (threadMXBean.getThreadInfo(info.getId()) == null) {
                    // No, assume thread dead.
                    threadData.remove(info.getId());
                    // To be strict, we should have to remove it from the list now also,
                    // but what the heck, we'll get rid of it in the next iteration...
                }
            }
            info.computePercentage(totalCpuTime);
            updateStateCounts(info.getState());
        }

        List untyped = list;
        if (showCpuUsage) {
            processInfo.update(totalCpuTime);
            processInfo.setPercentage(cpuUsagePercentage);
            untyped.add(processInfo);
        }
        return untyped;
    }

    public StackTraceElement[] retrieveStackTrace(long threadId, int maxStackTraceDepth) {
        ThreadInfo extendedInfo = threadMXBean.getThreadInfo(threadId, maxStackTraceDepth);
        if (extendedInfo != null) {
            return extendedInfo.getStackTrace();
        }
        else
            return new StackTraceElement[0];
    }

    public Map<State, Integer> getStats()
    {
        Map<State,Integer> map = new HashMap<State, Integer>();
        map.put(State.BLOCKED, threadsBlocked);
        map.put(State.RUNNABLE, threadsRunning);
        map.put(State.TIMED_WAITING, threadsTimedWaiting);
        map.put(State.WAITING, threadsWaiting);
        return map;
    }

    public int getCpuUsagePercentage()
    {
        return cpuUsagePercentage;
    }

    private void resetStateCounts()
    {
        this.threadsBlocked = 0;
        this.threadsRunning = 0;
        this.threadsTimedWaiting = 0;
        this.threadsWaiting = 0;
    }

    private void updateStateCounts(State threadState)
    {
        switch (threadState) {
        case BLOCKED:
            this.threadsBlocked++;
            break;
        case RUNNABLE:
            this.threadsRunning++;
            break;
        case TIMED_WAITING:
            this.threadsTimedWaiting++;
            break;
        case WAITING:
            this.threadsWaiting++;
            break;
        }
    }

    public synchronized void dispose()
    {
        threadMXBean = null;
        threadData = null;
    }
}
