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
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import java.io.IOException;

public class SwingUI
{
    public SwingUI(String hostname, int port, String role, String password)
    {
        JFrame frame = null;

        try {
            MBeanServerConnection serverConnection = TopThreads.connect(hostname, port, role, password);

            frame = new JFrame(TopThreads.PROGRAM_NAME);

            TopThreadsPanel topThreadsPanel = new TopThreadsPanel();
            JComponent contentPane = (JComponent) frame.getContentPane();
            contentPane.add(topThreadsPanel);
            int borderWidth = 10;
            contentPane.setBorder(new EmptyBorder(borderWidth, borderWidth, borderWidth, borderWidth));

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(contentPane);
            frame.setLocation(400, 0);
            frame.pack();
            frame.setVisible(true);

            topThreadsPanel.connect(serverConnection);
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(frame, "Could not connect to " + hostname + ":" + port + ".\n"
                            + (e.getMessage() != null && e.getMessage().trim() != ""? e.getMessage(): e),
                    TopThreads.PROGRAM_NAME, JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Could not connect to " + hostname + ":" + port
                    + (e.getCause() != null && e.getCause().toString().contains("ConnectException")? " (connection refused).\n": ".\n")
                    + "Check that the application you want to monitor has remote jmx monitoring enabled.",
                    TopThreads.PROGRAM_NAME, JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
