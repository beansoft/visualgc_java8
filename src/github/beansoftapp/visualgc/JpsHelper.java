package github.beansoftapp.visualgc;

import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.StringMonitor;
import sun.jvmstat.monitor.VmIdentifier;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class JpsHelper {
  private static final String JAR_SUFFIX = ".jar";  // NOI18N

  public static void main(String[] args) throws Exception {
    System.out.println(getJvmPSList());
  }

  /**
   * 获取本机上的所有进程列表.
   *
   * @return List<JvmProcess>
   */
  public static List<String> getJvmPSList() {
    List<String> vms = new ArrayList<String>();

    try {
      HostIdentifier hostId = new HostIdentifier((String) null);
      MonitoredHost monitoredHost = MonitoredHost.getMonitoredHost(hostId);

      // get the set active JVMs on the specified host.
      Set<Integer> jvms = monitoredHost.activeVms();
      int curVMId = GetProcessID.getPid();
      /* empty */
      for (Integer lvmid : jvms) {
        Throwable lastError = null;
        StringBuilder output;
//				System.out.println("lvmid=" + lvmid);
        MonitoredVm vm = null;
        String vmidString = "//" + lvmid + "?mode=r";
        output = new StringBuilder();

        output.append(lvmid);
        try {
          VmIdentifier id = new VmIdentifier(vmidString);
          vm = monitoredHost.getMonitoredVm(id, 0);
//               vms.add(vmidString);
        } catch (URISyntaxException e) {
          e.printStackTrace();
          // unexpected as vmidString is based on a validated hostid
          lastError = e;
        } catch (Exception e) {
          lastError = e;
          e.printStackTrace();
        } finally {
          if (vm == null) {
            /*
             * we ignore most exceptions, as there are race conditions
             * where a JVM in 'jvms' may terminate before we get a
             * chance to list its information. Other errors, such as
             * access and I/O exceptions should stop us from iterating
             * over the complete set.
             */
            System.out.println(" -- process information unavailable");
          }
        }

//            if (curVMId == lvmid) {
//               output.append(" VisualGC Self");
//            } else {
        output.append(" ");
        output.append(getMainClass(vm));
//            }

        vms.add(output.toString());

        monitoredHost.detach(vm);
      }
    } catch (URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MonitorException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return vms;
  }

  public static String getVmInfo(String lvmid) {
    MonitoredVm vm = null;
    MonitoredHost monitoredHost = null;
    try {
      HostIdentifier hostId = new HostIdentifier((String) null);
      monitoredHost = MonitoredHost.getMonitoredHost(hostId);
      StringBuilder output = new StringBuilder();

      String vmidString = "//" + lvmid + "?mode=r";

      output.append(lvmid);
      try {
        VmIdentifier id = new VmIdentifier(vmidString);
        vm = monitoredHost.getMonitoredVm(id, 0);
//               vms.add(vmidString);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (vm == null) {
          /*
           * we ignore most exceptions, as there are race conditions
           * where a JVM in 'jvms' may terminate before we get a
           * chance to list its information. Other errors, such as
           * access and I/O exceptions should stop us from iterating
           * over the complete set.
           */
          System.out.println(" -- process information unavailable");
        }
      }

      output.append(" ");
      output.append(getMainClass(vm));

      return output.toString();
    } catch (URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MonitorException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      try {
        assert monitoredHost != null;
        monitoredHost.detach(vm);
      } catch (MonitorException e) {
        e.printStackTrace();
      }
    }

    return lvmid;
  }


  /**
   * Return the main class for the target Java application.
   * Returns the main class, if the application started with the <em>-jar</em> option,
   * it tries to determine main class from the jar file. If
   * the jar file is not accessible, the main class is simple
   * name of the jar file.
   *
   * @return String - the main class of the target Java
   * application.
   */
  public static String getMainClass(MonitoredVm vm) {
    String mainClassName = getFirstArgument(vm);

    // if we are on localhost try read main class from jar file
//        if (curVM.get().getHost().equals(Host.LOCALHOST)) {
    File jarFile = new File(mainClassName);
    if (jarFile.exists()) {
      try {
        JarFile jf = new JarFile(jarFile);
        mainClassName = jf.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
        assert mainClassName != null;
      } catch (IOException ex) {
//            logger.error("getMainClass", ex);   // NOI18N
      }
    }
//        }

    if (mainClassName.endsWith(JAR_SUFFIX)) {
      mainClassName = mainClassName.replace('\\', '/');
      int index = mainClassName.lastIndexOf('/');
      if (index != -1) {
        mainClassName = mainClassName.substring(index + 1);
      }
    }
    mainClassName = mainClassName.replace('\\', '/').replace('/', '.');
    mainClassName = new IdeaParser().getName(vm, mainClassName);
    mainClassName = new NetbeansParser().getName(vm, mainClassName);
    return mainClassName;
  }

  /**
   * Returns the Java virtual machine command line.
   *
   * @return String - contains the command line of the target Java
   * application or <CODE>NULL</CODE> if the
   * command line cannot be determined.
   */
  public static String getCommandLine(MonitoredVm vm) {
    return getVmStringValue(vm, "sun.rt.javaCommand");   // NOI18N
  }

  /**
   * Returns the Java virtual machine implementation version.
   * This method is equivalent to {@link System#getProperty
   * System.getProperty("java.class.path")}.
   *
   * @return the Java virtual machine classpath.
   * @see System#getProperty
   */
  public static String getClassPath(MonitoredVm vm) {
    return getVmStringValue(vm, "java.property.java.class.path");   // NOI18N
  }

  /**
   * Returns the Java virtual machine command line arguments.
   *
   * @return String - contains the command line arguments of the target Java
   * application or <CODE>NULL</CODE> if the
   * command line arguments cannot be determined.
   */
  public static String getJvmArgs(MonitoredVm vm) {
    return getVmStringValue(vm, "java.rt.vmArgs");   // NOI18N
  }

  public static String getVmStringValue(MonitoredVm vm, String name) {
    try {
      StringMonitor monitor = (StringMonitor) vm.findByName(name);
      return monitor == null ? null : monitor.stringValue();
    } catch (MonitorException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Returns the Java virtual machine home directory.
   * This method is equivalent to {@link System#getProperty
   * System.getProperty("java.home")}.
   *
   * @return the Java virtual machine home directory.
   * @see System#getProperty
   */
  public static String getJavaHome(MonitoredVm vm) {
    return getVmStringValue(vm, "java.property.java.home");   // NOI18N
  }


  private static String getFirstArgument(MonitoredVm vm) {
    String commandLine = getCommandLine(vm);
    String mainClassName = null;

    // search for jar file
    int jarIndex = commandLine.indexOf(JAR_SUFFIX);
    if (jarIndex != -1) {
      String jarFile = commandLine.substring(0, jarIndex + JAR_SUFFIX.length());
      // if this is not end of commandLine check that jar file is separated by space from other arguments
      if (jarFile.length() == commandLine.length() || commandLine.charAt(jarFile.length()) == ' ') {
        // jarFile must be on classpath
        String classPath = getClassPath(vm);
        if (classPath != null && classPath.indexOf(jarFile) != -1) {
          mainClassName = jarFile;
        }
      }
    }
    // it looks like ordinary commandline with main class
    if (mainClassName == null) {
      int firstSpace = commandLine.indexOf(' ');
      if (firstSpace > 0) {
        mainClassName = commandLine.substring(0, firstSpace);
      } else {
        mainClassName = commandLine;
      }
    }
    return mainClassName;
  }

}
