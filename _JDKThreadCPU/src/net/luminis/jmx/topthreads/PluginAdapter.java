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

import com.sun.tools.jconsole.JConsoleContext;
import com.sun.tools.jconsole.JConsolePlugin;

import javax.management.MBeanServerConnection;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Map;

public class PluginAdapter extends JConsolePlugin
{
    public static final String TAB_NAME = "Top threads";

    private TopThreadsPanel topthreadsPanel;

    @Override
	public Map<String, JPanel> getTabs()
    {
        topthreadsPanel = new TopThreadsPanel();
        addContextPropertyChangeListener(new ConnectionListener());

        // See if we've got a connection right now
        JConsoleContext ctx = getContext();
        if (ctx != null && JConsoleContext.ConnectionState.CONNECTED.equals(ctx.getConnectionState())) {
            MBeanServerConnection connection = getContext().getMBeanServerConnection();
            if (connection != null)
                topthreadsPanel.connect(connection);
        }
        return Collections.singletonMap(TAB_NAME, (JPanel) topthreadsPanel);
	}

	@Override
	public SwingWorker<?, ?> newSwingWorker() {
		// We'll do our own polling, because of performance / scalability issues.
		return null;
	}

    @Override
    public void dispose() {
        if (topthreadsPanel != null)
            topthreadsPanel.dispose();
        super.dispose();
    }

    private class ConnectionListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent event)
        {
            if (JConsoleContext.CONNECTION_STATE_PROPERTY.equals(event.getPropertyName())) {
                if (event.getNewValue().equals(JConsoleContext.ConnectionState.CONNECTED)) {
                    JConsoleContext ctx = getContext();
                    if (ctx != null)
                        topthreadsPanel.connect(ctx.getMBeanServerConnection());
                }
                else if (event.getNewValue().equals(JConsoleContext.ConnectionState.DISCONNECTED)) {
                        topthreadsPanel.disconnect();
                }
            }
        }
    }
}
