package github.beansoftapp.visualgc;

import sun.jvmstat.monitor.MonitoredVm;

import java.util.Scanner;

/**
 * IDEA relatived main class parser, referred from org.graalvm.visualvm.application.type.IntellijApplicationTypeFactory
 * but with enhanced process name display.
 * Ref: https://github.com/oracle/visualvm/blob/master/visualvm/application/src/org/graalvm/visualvm/application/type/IntellijApplicationTypeFactory.java
 * @author beansoft
 * @date 2022-3-7
 */
public class IdeaParser {

  private static final String MAIN_CLASS = "com.intellij.idea.Main"; // NOI18N
  private static final String PLATFORM_ID = "-Didea.paths.selector="; // NOI18N
  private static final String PLATFORM_PREFIX = "-Didea.platform.prefix="; // NOI18N
  private static final String IDEA_ID = "Idea";  // NOI18N
  private static final String IDEA_CE = "IdeaIC"; // NOI18N
  private static final String IDEA_ENT = "IntelliJIdea"; // NOI18N
  private static final String PYCHARM_CE = "PyCharmCE"; // NOI18N
  private static final String IDEA_NAME = "IntelliJ IDEA"; // NOI18N
  private static final String IDEA_CE_NAME = "IntelliJ IDEA Community Edition "; // NOI18N
  private static final String IDEA_ENT_NAME = "IntelliJ IDEA Ultimate Edition "; // NOI18N
  private static final String PYCHARM_CE_NAME = "PyCharm CE "; // NOI18N
  private static final String PLATFORM_NAME = "IntelliJ Platform"; // NOI18N

  /**
   * Detects IntelliJ Plaform application. It returns
   * {@link IntellijApplicationType} for IntelliJ Platform application.
   *
   * @return {@link ApplicationType} subclass or <code>null</code> if
   * this application is not IntelliJ Platform application
   */
  public String getName(MonitoredVm vm, String mainClass) {
    if (MAIN_CLASS.equals(mainClass)) {
      return getName(vm);
    }
    if (mainClass == null || mainClass.length() == 0) {    // there is no main class - detect native Windows launcher
      String args = JpsHelper.getJvmArgs(vm);
      if (args != null && args.contains(PLATFORM_ID)) {
        return getName(vm);
      }
    }
    return mainClass;
  }

  private String getName(MonitoredVm vm) {
    String jvmArgs = JpsHelper.getJvmArgs(vm);
    String name = getValue(jvmArgs, PLATFORM_PREFIX);
    String selector = getValue(jvmArgs, PLATFORM_ID);
    if (selector != null) { // && selector.startsWith(IDEA_ID)) {
      if (selector.startsWith(IDEA_CE)) {
        return selector.replace(IDEA_CE, IDEA_CE_NAME);
      }
      if (selector.startsWith(IDEA_ENT)) {
        return selector.replace(IDEA_ENT, IDEA_ENT_NAME);
      }
      if (selector.startsWith(PYCHARM_CE)) {
        return selector.replace(PYCHARM_CE, PYCHARM_CE_NAME);
      }

      if(selector.startsWith(IDEA_ID)) {
        return selector.replace(IDEA_ID, IDEA_NAME);
      }
    }
    if (name != null) {
      return name;
    }

    return PLATFORM_NAME;
  }

  private String getValue(String args, String key) {
    if (args == null) return null;
    int index = args.indexOf(key);

    if (index >= 0) {
      Scanner sc = new Scanner(args.substring(index+key.length()));
      if (sc.hasNext()) {
        return sc.next();
      }
    }
    return null;
  }


}
