You can run your IDEA with JBR/OpenJDK 17 together with VisualGC.

Edit file `idea.vmoptions` (Mac OS) or `idea64.exe.vmoptions` (Windows) under the IDEA installation home /bin folder, append following line:

```properties
--add-opens=java.desktop/java.awt.event=ALL-UNNAMED
--add-opens=java.desktop/sun.font=ALL-UNNAMED
--add-opens=java.desktop/java.awt=ALL-UNNAMED
--add-opens=java.desktop/sun.awt=ALL-UNNAMED
--add-opens=java.base/java.lang=ALL-UNNAMED
--add-opens=java.base/java.util=ALL-UNNAMED
--add-opens=java.desktop/javax.swing=ALL-UNNAMED
--add-opens=java.desktop/sun.swing=ALL-UNNAMED
--add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED
--add-opens=java.desktop/java.awt.peer=ALL-UNNAMED
--add-opens=java.desktop/javax.swing.text.html=ALL-UNNAMED
--add-exports=java.desktop/sun.font=ALL-UNNAMED
--add-exports=java.desktop/sun.java2d=ALL-UNNAMED
--add-exports=java.desktop/sun.awt.windows=ALL-UNNAMED
--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED
--add-opens=java.desktop/sun.awt.windows=ALL-UNNAMED
--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED
--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor.event=ALL-UNNAMED
```

The below configuration is required by VisualGC plugin:
```properties
--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED
--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor.event=ALL-UNNAMED
```

Under Mac, please add `-Dsun.java2d.metal=true`