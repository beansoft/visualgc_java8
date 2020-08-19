package biz.beansoft.jmx.topthreads;

import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.Comparator;

public class ThreadInfoStats
{
  private static int historyLength = 10;
  private ThreadInfo threadInfo;
  private long threadId;
  private long previousCpuTime;
  private long lastCpuUsage;
  private int updateCount;
  private int percentage;
  private boolean showTrace;
  private int[] usageHistory;
  private int historyIndex;
  private int nrOfValidValues;
  private int index;
  private Integer threadPriority;

  public ThreadInfoStats(long threadId, ThreadInfo threadInfo, long previousCpuTime)
  {
    this.threadId = threadId;
    this.threadInfo = threadInfo;
    this.previousCpuTime = previousCpuTime;
    this.usageHistory = new int[historyLength];
  }

  public long getThreadId()
  {
    return this.threadId;
  }

  public boolean getShowTrace()
  {
    return this.showTrace;
  }

  public void showTrace(boolean paramBoolean)
  {
    this.showTrace = paramBoolean;
  }

  public boolean mustShowTrace()
  {
    return this.showTrace;
  }

  public int getPercentage()
  {
    return this.percentage;
  }

  public Thread.State getThreadState()
  {
    return this.threadInfo.getThreadState();
  }

  public long getCpuUsage()
  {
    return this.lastCpuUsage;
  }

  public int getAverageUsage()
  {
    if (this.nrOfValidValues > 0)
    {
      int i = this.percentage;
      for (int m : this.usageHistory)
        i += m;
      return i / this.nrOfValidValues;
    }
    return 0;
  }

  public long getCpuTime()
  {
    return this.previousCpuTime;
  }

  public String getThreadName()
  {
    return this.threadInfo.getThreadName();
  }

  public long update(ThreadInfo threadInfo, long cpuTime)
  {
    this.usageHistory[this.historyIndex] = this.percentage;
    this.historyIndex = (++this.historyIndex % historyLength);
    if (this.nrOfValidValues <= historyLength)
      this.nrOfValidValues += 1;
    this.threadInfo = threadInfo;
    this.lastCpuUsage = (cpuTime - this.previousCpuTime);
    if (this.lastCpuUsage < 0L)
      this.lastCpuUsage = 0L;
    this.previousCpuTime = cpuTime;
    this.updateCount += 1;
    return this.lastCpuUsage;
  }

  public boolean checkUpdate(int updateCount)
  {
    if (this.updateCount != updateCount)
    {
      assert ((this.updateCount == updateCount - 1) || (this.updateCount == 0));
      this.lastCpuUsage = 0L;
      this.updateCount = updateCount;
      return false;
    }
    return true;
  }

  public void computePercentage(long cpuUsage)
  {
		if (cpuUsage != 0L) {
			this.percentage = ((int) (100L * this.lastCpuUsage / cpuUsage));

//			if(this.percentage > 0) {
//				System.out.println(getThreadName());
//				
//				System.out.println(" lastCpuUsage= " + lastCpuUsage);
//				System.out.println(" cpuUsage= " + cpuUsage);
//				System.out.println(" this.percentage= " + this.percentage);
//			}

		}
  }

  public int[] getHistory()
  {
    int[] arrayOfInt = new int[historyLength];
    for (int i = 0; i < historyLength; i++)
    {
      int j = (this.historyIndex + i) % historyLength;
      arrayOfInt[i] = this.usageHistory[j];
    }
    return arrayOfInt;
  }

  public String getHistoryAsString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i = 0;
    for (int j = 0; j < historyLength; j++)
    {
      int k = (this.historyIndex + j) % historyLength;
      int m = this.usageHistory[k];
      if (m > 0)
        i = 1;
      localStringBuffer.append(m);
      localStringBuffer.append(" ");
    }
    if (i != 0)
      return localStringBuffer.toString();
    return "";
  }

  static Comparator<ThreadInfoStats> lastUsageComparator()
  {
    return new Comparator<ThreadInfoStats>()
    {
      public int compare(ThreadInfoStats paramAnonymousThreadInfoStats1, ThreadInfoStats paramAnonymousThreadInfoStats2)
      {
        if (paramAnonymousThreadInfoStats1.lastCpuUsage < paramAnonymousThreadInfoStats2.lastCpuUsage)
          return 1;
        if (paramAnonymousThreadInfoStats1.lastCpuUsage > paramAnonymousThreadInfoStats2.lastCpuUsage)
          return -1;
        if (paramAnonymousThreadInfoStats1.previousCpuTime < paramAnonymousThreadInfoStats2.previousCpuTime)
          return 1;
        if (paramAnonymousThreadInfoStats1.previousCpuTime > paramAnonymousThreadInfoStats2.previousCpuTime)
          return -1;
        return 0;
      }
    };
  }

  static Comparator<ThreadInfoStats> nameComparator()
  {
    return new Comparator<ThreadInfoStats>()
    {
      public int compare(ThreadInfoStats paramAnonymousThreadInfoStats1, ThreadInfoStats paramAnonymousThreadInfoStats2)
      {
        return paramAnonymousThreadInfoStats1.getThreadName().compareTo(paramAnonymousThreadInfoStats2.getThreadName());
      }
    };
  }

  public static Comparator<ThreadInfoStats> fixOrderComparator()
  {
    return new Comparator<ThreadInfoStats>()
    {
      public int compare(ThreadInfoStats paramAnonymousThreadInfoStats1, ThreadInfoStats paramAnonymousThreadInfoStats2)
      {
        if ((paramAnonymousThreadInfoStats1.index == 0) && (paramAnonymousThreadInfoStats2.index == 0))
          return new Long(paramAnonymousThreadInfoStats1.lastCpuUsage).compareTo(Long.valueOf(paramAnonymousThreadInfoStats2.lastCpuUsage));
        if (paramAnonymousThreadInfoStats1.index == 0)
          return 1;
        if (paramAnonymousThreadInfoStats2.index == 0)
          return -1;
        return new Integer(paramAnonymousThreadInfoStats1.index).compareTo(Integer.valueOf(paramAnonymousThreadInfoStats2.index));
      }
    };
  }

  public void setIndex(int paramInt)
  {
    this.index = paramInt;
  }

  public int getIndex()
  {
    return this.index;
  }

  public Integer getThreadPriority()
  {
    return this.threadPriority;
  }

  public void setThreadPriority(Integer paramInteger)
  {
    this.threadPriority = paramInteger;
  }

@Override
public String toString() {
	return "ThreadInfoStats [threadName=" + getThreadName() + ", threadId="
			+ threadId + ", previousCpuTime=" + previousCpuTime
			+ ", lastCpuUsage=" + lastCpuUsage + ", updateCount=" + updateCount
			+ ", percentage=" + percentage + ", showTrace=" + showTrace
			+ ", usageHistory=" + Arrays.toString(usageHistory)
			+ ", historyIndex=" + historyIndex + ", nrOfValidValues="
			+ nrOfValidValues + ", index=" + index + ", threadPriority="
			+ threadPriority + "]";
}
}
