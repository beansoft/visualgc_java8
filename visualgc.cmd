cd "%cd%"
rem set JAVA_HOME=C:\Java\jdk1.8.0_252
"%JAVA_HOME%\bin\java" -cp "%JAVA_HOME%\lib\tools.jar";.\lib\visualgc.jar;.\lib\visualgc_patch.jar com.sun.jvmstat.tools.visualgc.VisualGCPatch
pause