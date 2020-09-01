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
import java.io.IOException;
import java.util.Map;

/**
 * Provides methods for connecting to local JVM's. Depends on "sun" internal JDK classes!
 */
public interface LocalConnector {

    /**
     * @return map: pid -> process name (shortened)
     */
    public Map<Integer, String> getLocalVMs();

    public MBeanServerConnection connect(Integer pid) throws IOException;
}
