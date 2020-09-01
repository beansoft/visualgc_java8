/*
 * Copyright 2007-2016 Peter Doornbosch
 *
 * This file is part of TopThreads, a JConsole plugin to analyse CPU-usage per thread.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * TopThreads is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package net.luminis.jmx.topthreads;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import javax.management.MBeanServerConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.lang.management.ManagementFactory.*;

public class TextUI
{
    private static int NON_THREAD_ROWS = 6;
    private static String lotsOfSpaces = "                                             ";
    private static final Comparator<InfoStats> lastUsageComparator = ThreadInfoStats.lastUsageComparator();
    private static final Comparator<InfoStats> fixOrderComparator = ThreadInfoStats.fixOrderComparator();

    private final PrintStream out;
    private String processName;
    private int interval = 3;
    private boolean clearOnNextDraw = false;
    private int previousVerticalSize;
    private int phase = 0;
    private ThreadDataCollector threadDataCollector;
    private volatile boolean showCpuUsageHistory;
    private volatile int maxThreadsDisplayed;
    private volatile boolean showStackTrace;
    private volatile int showStackTraceFor;
    private long secondaryScreenTimeout = 30 * 1000;
    private int maxColumns = 80;
    private volatile int maxRows = 24;
    private volatile Comparator<InfoStats> comparator;
    private String threadColumnHeader;
    private boolean showAbout;
    private boolean showHelp;
    private String message;
    private boolean debug;

    public TextUI(String hostname, Integer port, String role, String password, boolean debug) {
        this.debug = debug;

        AnsiConsole.systemInstall();
        out = AnsiConsole.out();

        try {
            MBeanServerConnection server = TopThreads.connect(hostname, port, role, password);
            start(server);
        } catch (SecurityException e) {
            if (e.getMessage() != null && e.getMessage().trim() != "")
                System.err.println("Could not connect to '" + hostname + ":" + port + "': " + e.getMessage() + ".");
            else
                System.err.println("Could not connect to '" + hostname + ":" + port + "' (" + e + ")");
        } catch (IOException e) {
            System.err.println("Could not connect to '" + hostname + ":" + port + "' (" + e + ")");
        }
    }

    public TextUI(Integer pid, boolean debug) {
        this.debug = debug;

        AnsiConsole.systemInstall();
        out = AnsiConsole.out();

        try {
            MBeanServerConnection server = createLocalConnector().connect(pid);
            start(server);
        } catch (ConnectException cannotConnect) {
            System.err.println("No Java process with pid '" + pid + "' (or it does not support local jmx connections).");
        } catch (IOException e) {
            System.err.println("Could not connect to process with pid '" + pid + "' (" + e + ")");
        } catch (ConfigurationException error) {
            System.err.println("TopThreads cannot connect local VM's, because " + error.getMessage() + ".");
            System.err.println("Alternatively, run TopThreads with 'localhost:<port>' argument to connect via an explicit jmx connection.");
        }
    }

    public TextUI(boolean debug) {
        this.debug = debug;

        AnsiConsole.systemInstall();
        out = AnsiConsole.out();

        try {
            MBeanServerConnection server = selectLocalVM();
            if (server != null)
                start(server);
            // else: nothing selected.
        } catch (IOException e) {
            System.err.println("Could not connect");
        } catch (ConfigurationException error) {
            System.err.println("TopThreads cannot connect local VM's, because " + error.getMessage() + ".");
            System.err.println("Alternatively, run TopThreads with 'localhost:<port>' argument to connect via an explicit jmx connection.");
        }
    }

    private void start(MBeanServerConnection mbeanServer) throws IOException {

        maxThreadsDisplayed = 20;
        showCpuUsageHistory = true;
        boolean debug = false;
        comparator = lastUsageComparator;

        ThreadMXBean threadMXBean = newPlatformMXBeanProxy(mbeanServer, THREAD_MXBEAN_NAME, ThreadMXBean.class);
        RuntimeMXBean runtimeMXBean = newPlatformMXBeanProxy(mbeanServer, RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
        processName = runtimeMXBean.getName();

        threadDataCollector = new ThreadDataCollector(threadMXBean, debug);

        final Thread processThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    Data data = threadDataCollector.getThreadData(maxThreadsDisplayed, showCpuUsageHistory, false, comparator, null, 20);

                    updateUI(data);
                    try {
                        Thread.sleep(interval * 1000);
                    } catch (InterruptedException e) {
                        // Nothing
                    }

                    if (showStackTrace) {
                        int index = showCpuUsageHistory? showStackTraceFor + 1: showStackTraceFor;
                        if (data.threadList.size() > index) {
                            int lines = showStackTrace((ThreadInfoStats) data.threadList.get(index));
                            if (lines == 0) {
                                showStackTrace = false;
                            } else {
                                previousVerticalSize = lines;
                            }
                        }
                        else {
                            System.err.println("no stacktrace to show");
                        }
                    }

                    if (showAbout || showHelp || showStackTrace) {
                        if (showAbout) showAbout();
                        if (showHelp)  showHelp();
                        try {
                            Thread.sleep(secondaryScreenTimeout);
                        } catch (InterruptedException e) {
                            // Nothing
                        }
                        showAbout = showHelp = showStackTrace = false;
                        out.print(Ansi.ansi().eraseScreen());
                    }
                }
            }
        });

        Thread cmdLoopThread = new Thread(new Runnable() {
            @Override
            public void run() {
                commandLoop(processThread);
            }
        });

        out.print(Ansi.ansi().eraseScreen());

        cmdLoopThread.start();
        processThread.start();
        try {
            cmdLoopThread.join();
        } catch (InterruptedException e) {
            // Exit
        }
    }

    private int showStackTrace(ThreadInfoStats threadInfo) {
        StackTraceElement[] stackTraceElements = threadDataCollector.retrieveStackTrace(threadInfo.getId(), maxRows);
        if (stackTraceElements.length > 0) {
            int lineNr = 1;
            out.print(Ansi.ansi().eraseScreen());
            out.print(Ansi.ansi().cursor(lineNr++, 0).eraseLine().bold().a("Stacktrace").boldOff().a(" for thread " + threadInfo.getName()));
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                out.print(Ansi.ansi().cursor(lineNr++, 0).eraseLine().a(trim("" + (lineNr - 1) + " " + stackTraceElement, maxColumns)));
                if (lineNr > maxRows)
                    break;
            }
            out.flush();
            return lineNr - 1;
        } else {
            return 0;
        }
    }

    void commandLoop(Thread processThread) {
        String input = "";
        try {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            while ((input = stdin.readLine()) != null) {
                try {
                    if (input.equals("q")) {
                        System.err.println("bye...");
                        System.exit(0);
                    } else if (input.equals("h")) {
                        showHelp = true;
                    } else if (input.equals("r")) {
                        clearOnNextDraw = true;
                    } else if (input.equals("f")) {
                        if (comparator == lastUsageComparator) {
                            comparator = fixOrderComparator;
                        }
                        else {
                            comparator = lastUsageComparator;
                        }
                    } else if (input.startsWith("s")) {
                        showStackTraceFor = 0;
                        if (! input.equals("s")) {
                            showStackTraceFor = Integer.parseInt(input.substring(1).trim())-1;
                        }
                        showStackTrace = true;
                    } else if (input.startsWith("l")) {
                        if (! input.equals("l")) {
                            int value = Integer.parseInt(input.substring(1).trim());
                            if (value >= 7) {
                                maxRows = value;
                                maxThreadsDisplayed = maxRows - NON_THREAD_ROWS;
                            }
                        }
                    } else if (input.startsWith("i")) {
                        int value = Integer.parseInt(input.substring(1));
                        if (value > 0) {
                            interval = value;
                            processThread.interrupt();
                        }
                    } else if (input.equals("a")) {
                        showAbout = true;
                    } else if (input.trim().length() > 0) {
                        message = "Unknown input: '" + input + "'";
                    } else if (showStackTrace || showAbout || showHelp) {
                        showHelp = showStackTrace = showAbout = false;
                    }
                    processThread.interrupt();
                } catch (NumberFormatException e) {
                    // Don't bother
                }
            }
        }
        catch(IOException e){
            System.err.println("Error in reading commands");
            return;
        }
    }

    void updateUI(Data data) {
        PrintStream out = AnsiConsole.out();

        if (clearOnNextDraw) {
            out.print(Ansi.ansi().eraseScreen());
            clearOnNextDraw = false;
        }

        int lineNr = 1;
        out.print(Ansi.ansi().cursor(lineNr++, 0).eraseLine().a("  ").bold().a("TopThreads " + pad("2.0", 5, false)).boldOff()
                .a(" connected to \"").a(pad(processName + "\"", 30, false))
                .a("CPU: ").bold().a(pad(data.cpuUsagePercentage, 3) + "%").boldOff()
        );

        if (data.threadList == null) {
            out.print(Ansi.ansi().cursor(lineNr++, 0).eraseLine().a("Error while retrieving thread info: " + data.error + "\n"));
            System.exit(1);
        }

        int countPrecision = data.threadCount > 999? 4: data.threadCount > 99? 3: data.threadCount > 9? 2: 1;
        out.print(Ansi.ansi().cursor(lineNr++, 0).eraseLine().a(""
                + data.threadCount + " threads  "
                + pad(data.threadStats.get(Thread.State.RUNNABLE),      countPrecision) + " runnable  "
                + pad(data.threadStats.get(Thread.State.BLOCKED),       countPrecision) + " blocked  "
                + pad(data.threadStats.get(Thread.State.WAITING),       countPrecision) + " waiting  "
                + pad(data.threadStats.get(Thread.State.TIMED_WAITING), countPrecision) + " timed-waiting"
        ));
        out.print(Ansi.ansi().cursor(lineNr++, 0).eraseLine());
        String arrow = pad(lotsOfSpaces.substring(0, phase + 1) + ">    >    >    >", 21, false);
        phase = (phase + 1) % 5;
        threadColumnHeader = "THREAD NAME " + (comparator == fixOrderComparator? " (fixed order)": "");
        out.print(Ansi.ansi().cursor(lineNr++, 0).eraseLine().a(pad(threadColumnHeader, 43, false) + "%CPU" + "  HISTORY" + arrow));

        int index = 1;
        for (InfoStats info: data.threadList) {
            out.print(Ansi.ansi().cursor(lineNr, 0).eraseLine());
            String line = "";
            if (info instanceof ThreadInfoStats)
                line = pad(index++, 2) + " ";
            else
                line = "   ";
            line += pad(info.getName(), 40, false) + " ";
            line += colorize(info.getPercentage(), pad(info.getPercentage(), 2) + " ");
            int[] history = info.getHistory();
            for (int i = history.length-1; i >= 0; i--) {
                line += " " + colorize(history[i], pad(history[i], 2));
            }
            out.println(line);
            lineNr++;
            if (lineNr == maxRows)
                break;
        }
        int cursorX;
        if (message == null) {
            out.print(Ansi.ansi().cursor(lineNr++, 0).eraseLine().a("Period: ").a(pad("" + interval + "s", 5, false))
                    .bold().a("Command").boldOff().a(" (+Enter) h=Help: "));
            cursorX = 39;
        } else {
            if (message.length() > 50)
                message = message.substring(0, 47) + "...";
            out.print(Ansi.ansi().cursor(lineNr++, 0).eraseLine().fgBright(Ansi.Color.RED).a(message + "  ").fg(Ansi.Color.DEFAULT)
                    .bold().a("Command").boldOff().a(" (+Enter) h=Help: "));
            cursorX = message.length() + 28;
        }
        int currentLines = lineNr - 1;
        while (lineNr <= previousVerticalSize) {
            out.print(Ansi.ansi().cursor(lineNr++, 0).eraseLine());
        }
        out.print(Ansi.ansi().cursor(currentLines, cursorX));
        out.flush();

        previousVerticalSize = currentLines;
        message = null;
    }

    private String colorize(int threshold, String value) {
        if (threshold == 0)
            return pad(" -", value.length(), false);
        else if (threshold < 33) {
            return Ansi.ansi().fgGreen().a(value).fgDefault().toString();
        }
        else if (threshold < 67) {
            return Ansi.ansi().fgBrightYellow().a(value).fgDefault().toString();
        }
        else {
            return Ansi.ansi().fgBrightRed().a(value).fgDefault().toString();
        }
    }

    private String pad(int number, int width) {
        return pad("" + number, width, true);
    }

    private String pad(String text, int width, boolean right) {
        if (text.length() == width)
            return text;
        if (text.length() >= width)
            return text.substring(0, width);
        int padSize = width - text.length();
        while (lotsOfSpaces.length() < padSize) {
            lotsOfSpaces = lotsOfSpaces + lotsOfSpaces;
        }
        String pad = lotsOfSpaces.substring(0, padSize);
        if (right)
            return pad + text;
        else
            return text + pad;
    }

    private String trim(String text, int length) {
        if (text.length() == length)
            return text;
        if (text.length() >= length)
            return text.substring(0, length);
        return text;
    }

    private void showHelp() {
        out.print(Ansi.ansi().eraseScreen().cursor(0, 0));
        out.println(Ansi.ansi().bold().a("  TopThreads Help").boldOff());
        out.println("");
        out.println(" f          fix thread order (toggle)");
        out.println(" i <number> set refresh interval (in seconds)");
        out.println(" s [number] show stacktrace for thread (default is top thread)");
        out.println(" l <number> number of lines to use for display (must be >= 7)");
        out.println(" r          refresh display");
        out.println(" h          help");
        out.println(" a          about");
        out.println(" q          quit");
        out.println("");
        out.println("(<Enter> to return)");
    }

    private void showAbout() {
        out.print(Ansi.ansi().eraseScreen().cursor(0, 0));
        out.println("");
        out.println("   _____         _____ _                 _        ___   ___ ");
        out.println("  |_   _|___ ___|_   _| |_ ___ ___ ___ _| |___   |_  | |   |");
        out.println("    | | | . | . | | | |   |  _| -_| .'| . |_ -|  |  _|_| | |");
        out.println("    |_| |___|  _| |_| |_|_|_| |___|__,|___|___|  |___|_|___|");
        out.println("            |_|");
        out.println("");
        out.println("");
        out.println("  created by Peter Doornbosch (peter.doornbosch@luminis.eu)");
        out.println("  http://arnhem.luminis.eu/new_version_topthreads_jconsole_plugin/");
        out.println("");
        out.println("");
        out.println("  (<Enter> to return)");
    }

    private MBeanServerConnection selectLocalVM() throws IOException, ConfigurationException {

        LocalConnector connector = createLocalConnector();

        // Create a list of all VMs
        Map<Integer,String> vmsByPid = connector.getLocalVMs();
        List<Map.Entry<Integer,String>> localVMs = new ArrayList(vmsByPid.entrySet());
        // Sort them on process id
        Collections.sort(localVMs, new Comparator<Map.Entry<Integer, String>>() {
            @Override
            public int compare(Map.Entry<Integer, String> o1, Map.Entry<Integer, String> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        out.println(Ansi.ansi().eraseScreen().cursor(1, 1).a("Select a local process from the list (or 'q' to quit):"));

        int index = 1;
        for (Map.Entry<Integer, String> entry: localVMs) {
            out.println("(" + index++ + ") " + entry.getKey() + "\t" + entry.getValue());
        }
        out.print("Select a local process from the list (or 'q' to quit): ");
        out.flush();

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = stdin.readLine().trim();
            try {
                if (line.equals("q") || line.equals("quit")) {
                    return null;
                }
                int selected = Integer.parseInt(line);
                if (selected > 0 && selected <= localVMs.size()) {
                    return connector.connect(localVMs.get(selected-1).getKey());
                }
                else if (vmsByPid.containsKey(selected)) {
                    return connector.connect(selected);
                }
            }
            catch (NumberFormatException parseError) {
            }
            out.print(Ansi.ansi().cursorUpLine().a("Invalid input, select an index or pid (or 'q' to quit): ").eraseLine());
            out.flush();
        }
    }

    private LocalConnector createLocalConnector() throws ConfigurationException {
        String message = "";
        try {
            String toolsJar = "tools.jar";
            String jconsoleJar = "jconsole.jar";

            String javaHome = System.getenv("JAVA_HOME");
            File javaLibDir = new File("/dummy");
            // Do some sanity checks, but do not throw an exception immediately, it might work if libs are on classpath!
            if (javaHome == null || javaHome.isEmpty()) {
                javaHome = guessJavaHome();
                if (javaHome == null) {
                    message = "environment variable JAVA_HOME is not set.";
                }
                else {
                    if (debug) {
                        System.err.println("Guessed JAVA_HOME is " + javaHome);
                    }
                    message = "jdk libs " + toolsJar + ", " + jconsoleJar + " cannot be loaded from " + javaHome + "; set JAVA_HOME environment variable";
                }
            }
            if (javaHome != null) {
                if (!new File(javaHome).isDirectory()) {
                    message = "environment variable JAVA_HOME does not point to a directory";
                }
                else if (!new File(javaHome, "lib").isDirectory()) {
                    message = "environment variable JAVA_HOME does not point to Java Home (no lib directory)";
                }
                else {
                    javaLibDir = new File(javaHome, "lib");
                    if (!new File(javaLibDir, toolsJar).exists() || !new File(javaLibDir, jconsoleJar).exists()) {
                        message = "cannot find \"" + toolsJar + "\" and/or \"" + jconsoleJar + "\" in <JAVA_HOME>/lib";
                    }
                }
            }

            URL toolsPath = new URL("file:" + javaLibDir.getAbsolutePath() + File.separator + toolsJar);
            URL jconsolePath = new URL("file:" + javaLibDir.getAbsolutePath() + File.separator + jconsoleJar);
            URLClassLoader classLoader = new CustomClassloader(new URL[]{ toolsPath, jconsolePath }, getClass().getClassLoader());

            Class<?> connectorClass = classLoader.loadClass("net.luminis.jmx.sun.LocalConnectorImpl");
            return (LocalConnector) connectorClass.newInstance();
        } catch (MalformedURLException e) {
            // Impossible
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            // Impossible
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            // Impossible
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(message);
        } catch (NoClassDefFoundError missingClass) {
            throw new ConfigurationException(message);
        }
    }

    private String guessJavaHome() {

        String os = System.getProperty("os.name").toLowerCase();
        String javaVersion = System.getProperty("java.version");

        if (os.contains("mac") || os.contains("osx") || os.contains("os x")) {
            File baseDir = new File("/Library/Java/JavaVirtualMachines/");
            if (baseDir.isDirectory()) {
                for (String subDir: baseDir.list()) {
                    if (subDir.contains(javaVersion)) {
                        return new File(baseDir, subDir + "/Contents/Home").getAbsolutePath();
                    }
                }
            }
        }
        else if (os.contains("windows")) {
            for (String envVar: new String[] { "PROGRAMFILES", "PROGRAMFILES(X86)", "ProgramFiles", "ProgramFiles(X86)" }) {
                String progLocation = System.getenv(envVar);
                if (progLocation != null && new File(progLocation, "Java").exists()) {
                    File baseDir = new File(progLocation, "Java");
                    for (String subDir: baseDir.list()) {
                        if (subDir.contains(javaVersion)) {
                            return new File(baseDir, subDir).getAbsolutePath();
                        }
                    }
                }
            }
        }
        else if (os.contains("linux") || os.contains("unix")) {
            for (String location: new String[] { "/usr/local/lib/" }) {
                File baseDir = new File(location);
                if (baseDir.exists()) {
                    for (String subDir : baseDir.list()) {
                        if (subDir.contains(javaVersion)) {
                            return new File(baseDir, subDir).getAbsolutePath();
                        }
                    }
                }
            }
        }
        return null;
    }
}
