# Java Perf Tools and IDEA Plugin DevKit Helper


This repo contains mainly two modules:

## A. Visual Garbage Collection Monitoring Tool and JConsole performance tool

    a graphical tool for monitoring the HotSpot Garbage Collector, Compiler, and class loader. It can monitor both local and remote JVMs.
1. visualgc_fix
    minimal fix with original visualgc code to run under Java 8

2. visualgc_java8
    Fix visualgc to run and add more gc details

3. visualgc_idea
    A IDEA plugin with visualgc embed.

4. _JDKThreadCPU
    a JConsole plugin that displays the most active threads of the (Java) application being monitored by JConsole

## B. A IDEA Plugin DevKit Helper
 This plugin will provides some feature on IDEA Plugin DevKit. The 1.0 only provides a small function that when
    you editing plugin.xml, the HTML content in description and change-notes will be treated as HTML so syntax highlighting
    and tag completion will work.
