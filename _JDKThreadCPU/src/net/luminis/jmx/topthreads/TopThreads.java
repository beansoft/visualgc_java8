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

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopThreads
{
    public static final String PROGRAM_NAME = "TopThreads";
    public static final String VERSION = "2.0-beta";
    public static final String JARNAME = "topthreads-" + VERSION + ".jar";
    public static final String DEBUG_FLAG = "net.luminis.jmx.topthreads.debug";


    static boolean debug;

    static void usageAndExit() {
        System.out.println("");
        System.out.println("TopThreads version " + VERSION);
        System.out.println("");
        System.out.println("Use as JConsole plugin: jconsole -pluginpath " + JARNAME);
        System.out.println("");
        System.out.println("Stand alone usage: java -jar " + JARNAME + " <hostname>:<port>");
        System.out.println("               or: java -jar " + JARNAME + " [--textui|-t] [--user|-u <username>] [--password|-p <password>] <hostname>:<port>");
        System.out.println("               or: java -jar " + JARNAME + " [--textui|-t] <pid>");
        System.out.println("               or: java -jar " + JARNAME + " [--textui|-t]");
        System.out.println("");
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {

        if (System.getProperty(DEBUG_FLAG) != null && System.getProperty(DEBUG_FLAG).equals("true"))
            debug = true;

        String hostname = null;
        Integer port = null;
        Integer pid = null;
        String role = null;
        String password = null;

        List<String> optionsWithArgs = Arrays.asList(new String[] { "-u", "--user", "-p", "--password"});
        Map<String, String> options = new HashMap<String, String>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-") && optionsWithArgs.contains(arg)) {
                if (i+1 < args.length) {
                    options.put(arg, args[++i]);
                }
                else {
                    options.put(arg, null);
                }
            }
            else if (arg.startsWith("-")) {
                options.put(arg, null);
            }
            else {
                if (hostname == null) {
                    // Check if syntax is hostname:port or port is provided as separate argument
                    if (arg.contains(":")) {
                        String[] addressParts = arg.split(":");
                        if (addressParts.length == 2) {
                            hostname = addressParts[0];
                            try {
                                port = Integer.parseInt(addressParts[1]);
                            } catch (NumberFormatException x) {
                                usageAndExit();
                            }
                        }
                        else {
                            usageAndExit();
                        }
                    }
                    else {
                        // If not hostname:port, the first non-option args should be the pid
                        try {
                            pid = Integer.parseInt(arg);
                        } catch (NumberFormatException x) {
                            usageAndExit();
                        }
                    }
                }
            }
        }

        if (options.containsKey("-v") || options.containsKey("--version")) {
            System.out.println(VERSION);
            return;
        }

        if (options.containsKey("-h") || options.containsKey("--help")) {
            usageAndExit();
        }

        if (options.containsKey("-u"))
            role = options.get("-u");
        if (options.containsKey("--user"))
            role = options.get("--user");

        if (options.containsKey("-p"))
            password = options.get("-p");
        if (options.containsKey("--password"))
            password = options.get("--password");

        if (options.containsKey("--clearPrefs")) {
            TopThreadsPanel.removePrefs();
            System.out.println("cleared saved preferences");
            return;
        }

        if (options.containsKey("--debug")) {
            debug = true;
        }

        if (options.containsKey("-t") || options.containsKey("--textui")) {
            if (hostname != null && port != null) {
                new TextUI(hostname, port, role, password, debug);
            }
            else if (hostname == null && port == null) {
                if (pid != null) {
                    new TextUI(pid, debug);
                }
                else {
                    new TextUI(debug);
                }
            }
            else {
                usageAndExit();
            }
        }
        else {
            // By now, hostname and port should be set (SwingUI does not (yet) support local connect)
            if (hostname == null || port == null) {
                usageAndExit();
            }
            new SwingUI(hostname, port, role, password);
        }
    }

    static MBeanServerConnection connect(String hostname, int port, String role, String password) throws IOException {
        String connectSting = "/jndi/rmi://" + hostname + ":" + port + "/jmxrmi";
        try {
            JMXServiceURL url = new JMXServiceURL("rmi", "", 0, connectSting);
            Map env = new HashMap();
            env.put(JMXConnector.CREDENTIALS, new String[] { role, password });
            JMXConnector connector = JMXConnectorFactory.connect(url, env);
            return connector.getMBeanServerConnection();
        } catch (MalformedURLException e) {
            // impossible
            return null;  // just to satisfy compiler
        }
    }
}
