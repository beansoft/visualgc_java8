# visualgc_idea
Note: This project is only worked under IDEA, and this project should depds on the IDEA plugin SDK.

 推荐安装方式: File-> Settings-> Plugins 输入 VisualGC 即可下载.

IDEA插件仓库主页: https://plugins.jetbrains.com/plugin/14557-visualgc IDEA 2020+版本可打开页面后点击 Install 按钮直接安装.

An IDEA plugin which displays a Visual GC tool window inside your IDE.

Usage:

1. From main interface bottom tool window;
2. From menu Tools > New VisualGC Window.

在IDEA中显示 VisualGC 子窗口, 便于一边运行代码一边观察GC情况.

用法:

1. 停靠窗口下方的 VisualGC;
2. 打开新的独立运行窗口使用菜单 Tools > New VisualGC Window.

如果需要不依赖IDEA单独运行, 请移步: [VisualGC 3.0 独立运行增强版, 支持JDK 8](https://www.cnblogs.com/beansoft/p/visualgc_jdk8_standalone.html)

安装方式:

![image](https://img2020.cnblogs.com/blog/2073018/202006/2073018-20200623071037706-310287575.png)

截图:

![vgc_idea_mac_en](screenshot/vgc_idea.png)

[![vgc_idea_cn](https://img2020.cnblogs.com/blog/2073018/202006/2073018-20200620133346407-155505802.png)](https://img2020.cnblogs.com/blog/2073018/202006/2073018-20200620133345259-810121084.png)

![vgc_idea_cn_ps](https://img2020.cnblogs.com/blog/2073018/202006/2073018-20200623072624739-2118859173.png)

2020-9-20
IDEA 2022的JBR 中缺少 rmi 相关的 package, 导致出现 jps 192.168.3.15 直接报错
`C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2022.2\jbr-\bin> .\jps 192.168.3.15
Exception in thread "main" java.lang.IllegalArgumentException: Could not find MonitoredHost for scheme: rmi
at jdk.internal.jvmstat/sun.jvmstat.monitor.MonitoredHost.getMonitoredHost(MonitoredHost.java:177)
at jdk.jcmd/sun.tools.jps.Jps.main(Jps.java:59)`

如果改 jdk.jstatd 中的包名, 又会无法通过RMI校验:
class com.sun.proxy.jdk.proxy1.$Proxy0 cannot be cast to class beansoft.jvmstat.monitor.remote.RemoteHost (com.sun.proxy.jdk.proxy1.$Proxy0 is in module jdk.proxy1 of loader 'app'; beansoft.jvmstat.monitor.remote.RemoteHost is in unnamed module of loader 'app')

太难了... 自己定制一个 jstatd for JBR??

如果改用 JBR SDK, 那么JCEF又没有, Markdown无法工作... 怎么办!!??

修复问题:
`access denied ("java.util.PropertyPermission" "sun.jvmstat.monitor.local" "read")`

```text
grant {
  permission java.security.AllPermission;
};
```