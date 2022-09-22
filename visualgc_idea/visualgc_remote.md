<!-- TOC -->
  * [Visualizing operations of a remote JVM](#visualizing-operations-of-a-remote-jvm)
<!-- TOC -->
## Visualizing operations of a remote JVM
VisualGC IDEA now supports remote JVM's GC activity in realtime, however, this function must be used in Development servers only as it has a security risk that any hacker might get and kill your JVM process.

In the remote machine, create a file called `jstatd.all.policy`, create it using any text editor you like, eg:
```sh
vim /Users/beansoft/jstatd.all.policy

grant codebase "file:${java.home}/../lib/tools.jar" {
  permission java.security.AllPermission;
};

```
then start the jstatd tool(which is inside the $JAVA_HOME/bin):
```sh
jstatd -J-Djava.security.policy=/Users/beansoft/jstatd.all.policy -J-Djava.rmi.server.hostname=<YOUR REMOTE MACHINE IP>
```
the IP must be accessed outside that machine, eg value is: `192.168.3.15`

To check if it is accessed from your local machine which running the VisualGC IDEA plugin, try:
```sh
jps 192.168.3.15
```
then something which contails JVM process list will output like this:
```sh
625
1145 Jstatd
1509 Bootstrap
1422
```

Now you can view the gc log using terminal:
```sh
jstat -gc 1509@192.168.3.15 1000 20
```
If everything works fine, and you can see some outputs like this:
```text
 S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT    CGC    CGCT     GCT
4352.0 4352.0  0.0   4352.0 34944.0  21631.1   179596.0   114517.7  54940.0 53168.6 6700.0 6017.4     13    0.262   0      0.000   6      0.055    0.318
```
, now the VisualGC IDEA plugin is ready to connect the remote JVM machine.
Activate the VisualGC tool window in your IDE,
then click the button `Connect to Remote JVM...`,
input remote hostname (sample input 192.168.3.15 or 192.168.3.15:1099) or input empty value to connect local JVM,
finally click the `OK` button to save the hostname. Now you can see a remote 