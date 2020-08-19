# Visual Garbage Collection Monitoring Tool
a graphical tool for monitoring the HotSpot Garbage Collector, Compiler, and class loader. It can monitor both local and remote JVMs.

This repo contains three modules:
1. visualgc_fix
    minimal fix with original visualgc code to run under Java 8
    
2. visualgc_java8
    Fix visualgc to run and add more gc details
    
3. visualgc_idea
    A IDEA plugin with visualgc embed.

4. _JDKThreadCPU
    a JConsole plugin that displays the most active threads of the (Java) application being monitored by JConsole
