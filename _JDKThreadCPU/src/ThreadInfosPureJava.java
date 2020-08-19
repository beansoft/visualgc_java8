import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServerConnection;

import biz.beansoft.jmx.topthreads.ThreadInfoStats;



public class ThreadInfosPureJava {

	  private MBeanServerConnection server;
	  private ThreadMXBean threadMXBean;
	  private Map<Long, ThreadInfoStats> threadData = new HashMap();
	  private int updateCount;
	  private boolean debug;
	  private int threadCount;
	  private boolean includeThreadPriorities;
	  private int threadsBlocked = 0;
	  private int threadsRunning = 0;
	  private int threadsTimedWaiting = 0;
	  private int threadsWaiting = 0;
	  private String latestStackTrace = "";
	  private int maxStackTraceDepth = 100;
	  private int maxThreadsDisplayed = 100;
	  
	public static void main(String[] args) throws Exception {
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		ThreadInfosPureJava monitor = new ThreadInfosPureJava();
		monitor.threadMXBean = threadMXBean;

		for (int i = 0; i < 100; i++) {
			List<ThreadInfoStats> stats = monitor.getThreadList();
			
			System.out.println("=========================================");
			for (ThreadInfoStats tinfo : stats) {
				System.out.println(tinfo);
			}
			System.out.println("=========================================");
			System.out.println();
			System.out.println();
			
			for(int j =0; j < 1000000; j++) {
				double k = Math.random() * j * 2048.111;
			}
			Thread.sleep(1000);
		}
	}
	  
	  private synchronized List<ThreadInfoStats> getThreadList()
	  {
	    if (this.threadMXBean == null)
	      return Collections.emptyList();
	    this.updateCount += 1;
	    long[] allThreadIds = null;
	    ThreadInfo[] allThreadInfos = null;
	    try
	    {
	      allThreadIds = this.threadMXBean.getAllThreadIds();
	      allThreadInfos = this.threadMXBean.getThreadInfo(allThreadIds);
	    }
	    catch (UndeclaredThrowableException localUndeclaredThrowableException)
	    {
	      if (this.debug)
	        System.err.println("error while getting thread info: " + localUndeclaredThrowableException + " caused by " + localUndeclaredThrowableException.getCause());
	      return Collections.emptyList();
	    }
	    catch (Exception localException)
	    {
	      if (this.debug)
	        System.err.println("error while getting thread info: " + localException);
	      return Collections.emptyList();
	    }
	    Map threadPrioritiesMap = null;
	    if (this.includeThreadPriorities)
	      threadPrioritiesMap = determineThreadPriorities();
	    long totalProcessCpuUsage = 0L;
	    for (int i = 0; i < allThreadIds.length; i++)
	    {
	      assert (allThreadInfos[i].getThreadId() == allThreadIds[i]);
	      long threadID = allThreadIds[i];
	      if (allThreadInfos[i] != null)
	      {
	        long threadCpuTime = this.threadMXBean.getThreadCpuTime(threadID);
	        if (threadCpuTime != -1L)
	        {
	          ThreadInfoStats _threadInfoStats = (ThreadInfoStats)this.threadData.get(Long.valueOf(threadID));
	          if (_threadInfoStats == null)
	          {
	            _threadInfoStats = new ThreadInfoStats(threadID, allThreadInfos[i], threadCpuTime);
	            this.threadData.put(Long.valueOf(threadID), _threadInfoStats);
	          }
	          else
	          {
	            totalProcessCpuUsage += _threadInfoStats.update(allThreadInfos[i], threadCpuTime);
	          }
	          if (this.includeThreadPriorities)
	          {
	            Integer threadPriorit = (Integer)threadPrioritiesMap.get(Long.valueOf(threadID));
	            if (threadPriorit != null)
	              _threadInfoStats.setThreadPriority(threadPriorit);
	            else
	              System.err.println("no prio for thread " + threadID);
	          }
	        }
	      }
	    }
	    resetStateCounts();
	    this.latestStackTrace = "";
	    ArrayList localArrayList = new ArrayList(this.threadData.values());
	    Iterator localIterator = localArrayList.iterator();
	    Object threadInfoStats;
	    while (localIterator.hasNext())
	    {
	      threadInfoStats = (ThreadInfoStats)localIterator.next();
	      if ((!((ThreadInfoStats)threadInfoStats).checkUpdate(this.updateCount)) && (this.threadMXBean.getThreadInfo(((ThreadInfoStats)threadInfoStats).getThreadId()) == null))
	        this.threadData.remove(Long.valueOf(((ThreadInfoStats)threadInfoStats).getThreadId()));
	      ((ThreadInfoStats)threadInfoStats).computePercentage(totalProcessCpuUsage);
	      updateStateCounts(((ThreadInfoStats)threadInfoStats).getThreadState());
	      if (((ThreadInfoStats)threadInfoStats).mustShowTrace())
	        this.latestStackTrace = retrieveStackTrace((ThreadInfoStats)threadInfoStats);
	    }
//	    Collections.sort(localArrayList, this.m_comparator);
//	    if (!this.fixOrder)
//	    {
//	      int j = 1;
//	      threadInfoStats = localArrayList.iterator();
//	      while (((Iterator)threadInfoStats).hasNext())
//	      {
//	        ThreadInfoStats localThreadInfoStats1 = (ThreadInfoStats)((Iterator)threadInfoStats).next();
//	        localThreadInfoStats1.setIndex(j++);
//	      }
//	    }
	    
	    this.threadCount = localArrayList.size();
	    return localArrayList.subList(0, Math.min(this.threadCount, this.maxThreadsDisplayed));
	  }

	  private Map<Long, Integer> determineThreadPriorities()
	  {
	    return Collections.emptyMap();
	  }
	  
	  /**
	   * 重置状态计数器.
	   */
	  private void resetStateCounts()
	  {
	    this.threadsBlocked = 0;
	    this.threadsRunning = 0;
	    this.threadsTimedWaiting = 0;
	    this.threadsWaiting = 0;
	  }

	  private void updateStateCounts(Thread.State paramState)
	  {
	    switch (paramState.ordinal())
	    {
	    case 1:
	      this.threadsBlocked += 1;
	      break;
	    case 2:
	      this.threadsRunning += 1;
	      break;
	    case 3:
	      this.threadsTimedWaiting += 1;
	      break;
	    case 4:
	      this.threadsWaiting += 1;
	    }
	  }
	  
	  private String retrieveStackTrace(ThreadInfoStats paramThreadInfoStats)
	  {
	    ThreadInfo localThreadInfo = this.threadMXBean.getThreadInfo(paramThreadInfoStats.getThreadId(), this.maxStackTraceDepth);
	    if (localThreadInfo != null)
	    {
	      StackTraceElement[] stackTraces = localThreadInfo.getStackTrace();
	      StringBuffer localStringBuffer = new StringBuffer();
	      for (StackTraceElement stackTraceElement : stackTraces)
	      {
	        localStringBuffer.append(stackTraceElement.toString());
	        localStringBuffer.append("\n");
	      }
	      return localStringBuffer.toString();
	    }
	    return "";
	  }
}
