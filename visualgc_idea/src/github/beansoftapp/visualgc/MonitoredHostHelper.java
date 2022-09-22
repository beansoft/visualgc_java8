package github.beansoftapp.visualgc;

import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.VmIdentifier;
import sun.jvmstat.perfdata.monitor.protocol.rmi.MonitoredHostProvider;

/**
 * Because the JBR 17 bundled in IDEA lacks the module jdk.jstatd, thus it can't run jstatd or connect to a remote JVM
 * server.
 * This class is trying to fix this problem with the bundled jstatd classes.
 * @author beansoft
 * @version 2022-09-20
 */
public class MonitoredHostHelper {
    /**
     * Fix issue in JBR(non SDK) that can't support rmi:
     * <code></code>java.lang.IllegalArgumentException: Could not find MonitoredHost for scheme: rmi</code>
     * Factory method to construct a MonitoredHost instance to manage the
     * connection to the Java Virtual Machine indicated by {@code vmid}.
     *
     * This method provide a convenient short cut for attaching to a specific
     * instrumented Java Virtual Machine. The information in the VmIdentifier
     * is used to construct a corresponding HostIdentifier, which in turn is
     * used to create the MonitoredHost instance.
     *
     * @param vmid The identifier for the target Java Virtual Machine.
     * @return MonitoredHost - The MonitoredHost object needed to attach to
     *                         the target Java Virtual Machine.
     *
     * @throws MonitorException Thrown if monitoring errors occur.
     * @version 2022-09-20
     */
    public static MonitoredHost getMonitoredHost(VmIdentifier vmid)
            throws MonitorException {
        // use the VmIdentifier to construct the corresponding HostIdentifier
        HostIdentifier hostId = new HostIdentifier(vmid);
        return getMonitoredHost(hostId);
    }

    /**
     * Fix issue in JBR(non SDK) that can't support rmi:
     * <code></code>java.lang.IllegalArgumentException: Could not find MonitoredHost for scheme: rmi</code>.
     * Replaces the MonitoredHost.getMonitoredHost() method.
     * Factory method to construct a MonitoredHost instance to manage the
     * connection to the host indicated by {@code hostId}.
     *
     * @see MonitoredHost#getMonitoredHost(HostIdentifier)
     * @param hostId the identifier for the target host.
     * @return MonitoredHost - The MonitoredHost object needed to attach to
     *                         the target host.
     *
     * @throws MonitorException Thrown if monitoring errors occur.
     * @version 2022-09-20
     */
    public static MonitoredHost getMonitoredHost(HostIdentifier hostId)
            throws MonitorException {
        if("localhost".equalsIgnoreCase(hostId.getHost())) {
            return MonitoredHost.getMonitoredHost(hostId);
        } else {
            // 远程连接, 使用插件提供的rmi远程类
            return new MonitoredHostProvider(hostId);
        }
    }
}
