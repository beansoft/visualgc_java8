package beansoft.jvm.hotspot.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * http://rednaxelafx.iteye.com/blog/716918
 * Note: this class only works at Sun JDK and JRockit.
 * see:jrcmd.exe jrockit.tools.jrcmd.JrCmd in tools.jar
 * see:jps.exe
 * @author beansoft
 * 2012-01-31
 */
public class GetProcessID {

    public static int getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName(); // format: "pid@hostname"
//        System.out.println("name:" + name);
        try {
            return Integer.parseInt(name.substring(0, name.indexOf('@')));
        } catch (Exception e) {
            return -1;
        }
    }

}
