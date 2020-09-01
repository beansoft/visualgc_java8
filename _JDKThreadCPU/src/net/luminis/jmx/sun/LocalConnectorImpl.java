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
package net.luminis.jmx.sun;

import net.luminis.jmx.topthreads.LocalConnector;
import sun.tools.jconsole.LocalVirtualMachine;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class LocalConnectorImpl implements LocalConnector {

    public LocalConnectorImpl() {
        // Force NoClassDefError a.s.a.p. if a used class cannot be found.
        LocalVirtualMachine.getAllVirtualMachines();
    }

    /**
     * @return map: pid -> process name (shortened)
     */
    public Map<Integer, String> getLocalVMs() {
        Map<Integer, String> localVMs = new HashMap<Integer, String>();
        for (LocalVirtualMachine vm: LocalVirtualMachine.getAllVirtualMachines().values()) {
            String name = vm.displayName();
            int maxNameLength = 40;
            if (name.length() > maxNameLength) {
                name = name.split(" ")[0];
                if (name.length() > maxNameLength) {
                    name = name.substring(0, maxNameLength - 3) + "...";
                }
            }
            localVMs.put(vm.vmid(), name);
        }
        return localVMs;
    }

    public MBeanServerConnection connect(Integer pid) throws IOException {
        LocalVirtualMachine virtualMachine = LocalVirtualMachine.getAllVirtualMachines().get(pid);
        if (virtualMachine != null) {
            try {
                if (virtualMachine.isAttachable()) {
                    if (virtualMachine.connectorAddress() == null) {
                        virtualMachine.startManagementAgent();
                    }
                    if (virtualMachine.connectorAddress() == null) {
                        throw new IOException("Cannot connect");
                    }
                    JMXServiceURL url = new JMXServiceURL(virtualMachine.connectorAddress());
                    JMXConnector connector = JMXConnectorFactory.connect(url, null);
                    return connector.getMBeanServerConnection();
                }
                else {
                    throw new IOException("Process not attachable");
                }
            } catch (MalformedURLException e) {
                // Impossible
                throw new RuntimeException(e);
            }
        }
        else {
            throw new ConnectException("no such process");
        }
    }
}
