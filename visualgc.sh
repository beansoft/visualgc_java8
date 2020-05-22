cd `dirname "$0"`
JAVA_HOME=/Users/beansoft/Documents/Java/jdk1.7.0_65.jdk/Contents/Home
#/Library/Java/JavaVirtualMachines/jdk-14.0.1.jdk/Contents/Home/bin/java
$JAVA_HOME/bin/java -cp $JAVA_HOME/lib/tools.jar:./lib/visualgc.jar:./lib/visualgc_patch.jar com.sun.jvmstat.tools.visualgc.VisualGCPatch
