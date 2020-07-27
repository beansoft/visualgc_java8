package github.beansoftapp.visualgc;

import sun.jvmstat.monitor.MonitoredVm;

import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

public class NetbeansParser {

  private static final String NETBEANS_DIRS = "-Dnetbeans.dirs="; // NOI18N
  private static final String NB_PLATFORM_HOME = "-Dnetbeans.home="; // NOI18N
  private static final String BRANDING_ID = "--branding "; // NOI18N
  private static final String VISUALVM_ID = "visualvm"; // NOI18N
  private static final String MAIN_CLASS = "org.netbeans.Main"; // NOI18N
  private static final Pattern NBCLUSTER_PATTERN = Pattern.compile("nb[0-9]+\\.[0-9]+");    // NOI18N
  private static final String BUILD_CLUSTER = "cluster"; // NOI18N
  private static final String VISUALVM_BUILD_WIN_ID = "\\visualvm\\build\\cluster;"; // NOI18N
  static final String NB_CLUSTER = "nb";    // NOI18N
  static final String PRODUCT_VERSION_PROPERTY="netbeans.productversion";  // NOI18N

  private boolean isNetBeans(MonitoredVm vm, String mainClass) {
    if (MAIN_CLASS.equals(mainClass)) {
      return true;
    }
    if (mainClass == null || mainClass.length() == 0) {    // there is no main class - detect new NB 7.0 windows launcher
      String args = JpsHelper.getJvmArgs(vm);
      if (args != null && args.contains(NB_PLATFORM_HOME)) {
        return true;
      }
    }
    return false;
  }

  protected String getBranding(MonitoredVm vm) {
    String args = JpsHelper.getJvmArgs(vm);
    if (args != null) {
      int brandingOffset = args.indexOf(BRANDING_ID);

      if (brandingOffset > -1) {
        Scanner sc = new Scanner(args.substring(brandingOffset));
        sc.next(); // skip --branding
        if (sc.hasNext()) {
          return sc.next();
        }
      }
    }
    return null;
  }

  /**
   * Detects IntelliJ Plaform application. It returns
   * {@link IntellijApplicationType} for IntelliJ Platform application.
   *
   * @return {@link ApplicationType} subclass or <code>null</code> if
   * this application is not IntelliJ Platform application
   */
  public String getName(MonitoredVm jvm, String mainClass) {
    if (isNetBeans(jvm,mainClass)) {
      String branding = getBranding(jvm);
      if (VISUALVM_ID.equals(branding)) {
        return "VisualVM";
      }

      Set<String> clusters = computeClusters(jvm);

      for (String cluster : clusters) {
        if (NBCLUSTER_PATTERN.matcher(cluster).matches()) {
          return "NetBeans IDE";
        }
        if (NB_CLUSTER.equals(cluster)) {
          return "NetBeans IDE";
        }
        if (VISUALVM_ID.equals(cluster)) {
          return "VisualVM";
        }
        if (BUILD_CLUSTER.equals(cluster)) {
          // NetBeans platform application was executed
          // directly from IDE or from ant script.
          // Check if it is VisualVM on Windows - on other platforms
          // VisualVM is recognized via branding
          if (JpsHelper.getJvmArgs(jvm).contains(VISUALVM_BUILD_WIN_ID)) {
            return "VisualVM";
          }
        }
      }
    }

    return mainClass;
  }

  protected Set<String> computeClusters(MonitoredVm jvm) {
    String args = JpsHelper.getJvmArgs(jvm);
    int clusterIndex = args.indexOf(NETBEANS_DIRS);
    String pathSeparator = JpsHelper.getJavaHome(jvm).contains("\\")?";":":";    // NOI18N
    String separator = pathSeparator.equals(":")?"/":"\\";      // NOI18N
    Set<String> clusters = new HashSet();

    if (clusterIndex > -1) {
      String clustersString=args.substring(clusterIndex);
      int endIndex = clustersString.indexOf(" -");  // NOI18N
      Scanner clusterScanner;
      if (endIndex == -1) {
        endIndex = clustersString.indexOf(" exit");  // NOI18N
      }
      if (endIndex > -1) {
        clustersString = clustersString.substring(0,endIndex);
      }
      clusterScanner = new Scanner(clustersString).useDelimiter(pathSeparator);
      while (clusterScanner.hasNext()) {
        String clusterPath = clusterScanner.next();
        int pathIndex = clusterPath.lastIndexOf(separator);
        if (pathIndex > -1) {
          clusters.add(clusterPath.substring(pathIndex+1));
        }
      }
    }
    return Collections.unmodifiableSet(clusters);
  }
}
