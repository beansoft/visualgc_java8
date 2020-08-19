import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.Test;

import beansoft.profiler.global.CommonConstants;

public class ThreadStatsTest {
	
	@Test
	public void testProcessCPUUsage() throws Exception {
		com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
	    int availableProcessors = operatingSystemMXBean.getAvailableProcessors();
	    long prevUpTime = runtimeMXBean.getUptime();
	    long prevProcessCpuTime = operatingSystemMXBean.getProcessCpuTime();
	    double cpuUsage;
	    try 
	    {
	        Thread.sleep(500);
	    } 
	    catch (Exception ignored) { }

	    operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	    long upTime = runtimeMXBean.getUptime();
	    long processCpuTime = operatingSystemMXBean.getProcessCpuTime();
	    long elapsedCpu = processCpuTime - prevProcessCpuTime;
	    long elapsedTime = upTime - prevUpTime;

	    cpuUsage = Math.min(99F, elapsedCpu / (elapsedTime * 10000F * availableProcessors));
	    System.out.println("Java CPU: " + cpuUsage);
	}

	// See Thread.State.BLOCKED etc
	 byte getState(ThreadInfo threadInfo) {
         Thread.State state = threadInfo.getThreadState();
         switch (state) {
             case BLOCKED:
                 return CommonConstants.THREAD_STATUS_MONITOR;
             case RUNNABLE:
                 return CommonConstants.THREAD_STATUS_RUNNING;
             case TIMED_WAITING:
             case WAITING:
                 return isSleeping(threadInfo.getStackTrace()[0]) ?
                     CommonConstants.THREAD_STATUS_SLEEPING :
                     CommonConstants.THREAD_STATUS_WAIT;
             case TERMINATED:
             case NEW:
                 return CommonConstants.THREAD_STATUS_ZOMBIE;
         }
         return CommonConstants.THREAD_STATUS_UNKNOWN;
     }

     boolean isSleeping(StackTraceElement element) {
         return Thread.class.getName().equals(element.getClassName()) &&
                 "sleep".equals(element.getMethodName());    // NOI18N
     }
     
//     private String retrieveStackTrace(ThreadInfo tinfo)
//     {
//       ThreadInfo localThreadInfo = this.threadMXBean.getThreadInfo(paramThreadInfoStats.getThreadId(), this.maxStackTraceDepth);
//       if (localThreadInfo != null)
//       {
//         StackTraceElement[] stackTraces = localThreadInfo.getStackTrace();
//         StringBuffer localStringBuffer = new StringBuffer();
//         for (StackTraceElement stackTraceElement : stackTraces)
//         {
//           localStringBuffer.append(stackTraceElement.toString());
//           localStringBuffer.append("\n");
//         }
//         return localStringBuffer.toString();
//       }
//       return "";
//     }
     
     private static final String PROCESS_CPU_TIME_ATTR = "ProcessCpuTime"; // NOI18N
     private static final String PROCESSING_CAPACITY_ATTR = "ProcessingCapacity"; // NOI18N
     
     long getProcessCPUTime(MBeanServerConnection conn, long processCPUTimeMultiplier) {       
           if (conn != null) {
                 try {
                     Long cputime = (Long)conn.getAttribute(getOSName(),PROCESS_CPU_TIME_ATTR);
                     
                     return cputime.longValue()*processCPUTimeMultiplier;
                 } catch (Exception ex) {
                    ex.printStackTrace();
                 }
             }
         return -1;
     }

     long getProcessCPUTimeMultiplier(MBeanServerConnection conn) {       
         if (conn != null) {
               try {
            	   Number mul = (Number) conn.getAttribute(getOSName(),PROCESSING_CAPACITY_ATTR);
                  return mul.longValue();
               } catch (Exception ex) {
//                  ex.printStackTrace();
            	   System.out.println(ex);
               }
           }
       return 1;
   }
     
	private static ObjectName getOSName() {
	    try {
	        return new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
	    } catch (MalformedObjectNameException ex) {
	        throw new RuntimeException(ex);
	    }
	}
     
    private final int processorsCount = 0;

    private long prevUpTime = -1;
    private long prevProcessCpuTime = -1;
    private long prevProcessGcTime = -1;
    
	@Test
	public void testThreadStats() throws Exception {
		Thread.sleep(2000);
		
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		
		long processCPUTimeMultiplier = getProcessCPUTimeMultiplier(mBeanServer);
		long processCPUTime = getProcessCPUTime(mBeanServer, processCPUTimeMultiplier);
		
		long upTime = runtimeMXBean.getUptime();
		long startTime = runtimeMXBean.getStartTime();
		
		String runTimemsg = String.format(
				"Start at %s , run %d seconds, process cpu time %d seconds",
				java.text.DateFormat.getDateTimeInstance().format(new java.util.Date(startTime)), upTime / 1000, processCPUTime / 1000);
		System.out.println(runTimemsg);
		
		assertTrue(threadMXBean.isThreadCpuTimeSupported());
		assertTrue(threadMXBean.isCurrentThreadCpuTimeSupported());

		threadMXBean.setThreadContentionMonitoringEnabled(true);
		threadMXBean.setThreadCpuTimeEnabled(true);
		assertTrue(threadMXBean.isThreadCpuTimeEnabled());

		ThreadInfo[] threadInfo = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), 2);
		for (ThreadInfo threadInfo2 : threadInfo) {
			long blockedTime = threadInfo2.getBlockedTime();
			long waitedTime = threadInfo2.getWaitedTime();
			long cpuTime = threadMXBean.getThreadCpuTime(threadInfo2.getThreadId());
			long userTime = threadMXBean.getThreadUserTime(threadInfo2.getThreadId());
			int state = getState(threadInfo2);

			String msg = String.format(
					"%s: %d ns cpu time, %d ns user time, blocked for %d ms, waited %d ms, state %d",
					threadInfo2.getThreadName(), cpuTime, userTime, blockedTime, waitedTime, state);
			System.out.println(msg);
		}
	}
}