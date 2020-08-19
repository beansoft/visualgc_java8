package biz.beansoft.jmx.topthreads;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class Main {
	private static final String PROGRAM_NAME = "TopThreads";
	public static final String JARNAME = "topthreads.jar";
	public static final String VERSION = "1.0.4";

	static void usage() {
		System.out.println("");
		System.out.println("TopThreads version 1.0.4");
		System.out.println("");
		System.out.println("Usage: java -jar topthreads.jar <hostname>:<port>");
		System.out.println("    or java -cp topthreads.jar "
				+ Main.class.getName() + " <hostname>:<port>");
		System.out
				.println("    or as a JConsole plugin: jconsole -pluginpath topthreads.jar");
		System.out
				.println("Requires Java 1.5, JConsole plugin requires Java 1.6");
		// System.exit(1);
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		if (args.length != 1)
			usage();
		String str = "localhost";
		int port = 1090;
		try {
			String[] arrayOfString = args[0].split(":");
			if (arrayOfString.length != 2)
				usage();
			str = arrayOfString[0];
			port = -1;
			try {
				port = Integer.parseInt(arrayOfString[1]);
			} catch (NumberFormatException localNumberFormatException) {
				usage();
			}
			if (port < 0)
				usage();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		final TopThreadsPanel topThreadsPanel = new TopThreadsPanel();
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					Main.createAndShowGUI(topThreadsPanel);
				}
			});
		} catch (InterruptedException localInterruptedException) {
		} catch (InvocationTargetException localInvocationTargetException) {
		}

		if (args.length != 1) {
			 MBeanServer mBeanServer =
						 ManagementFactory.getPlatformMBeanServer();
			 topThreadsPanel
				.setMBeanServerConnection(mBeanServer);
		} else {
			MBeanServerConnection localMBeanServerConnection = connect(
					topThreadsPanel, str, port);
			topThreadsPanel
					.setMBeanServerConnection(localMBeanServerConnection);
		}


	}

	public static void createAndShowGUI(JPanel paramJPanel) {
		JFrame localJFrame = new JFrame("线程诊断工具 by github.com/beansoft");
		JComponent localJComponent = (JComponent) localJFrame.getContentPane();
		localJComponent.add(paramJPanel, "Center");
		localJComponent.setOpaque(true);
		localJComponent.setBorder(new EmptyBorder(12, 12, 12, 12));
		localJFrame.setDefaultCloseOperation(3);
		localJFrame.setContentPane(localJComponent);
		localJFrame.pack();
		localJFrame.setVisible(true);
	}

	static MBeanServerConnection connect(JPanel paramJPanel, String host,
			int port) {
		String str = "/jndi/rmi://" + host + ":" + port + "/jmxrmi";
		MBeanServerConnection localMBeanServerConnection = null;
		try {
			JMXServiceURL localJMXServiceURL = new JMXServiceURL("rmi", "", 0,
					str);
			JMXConnector localJMXConnector = JMXConnectorFactory
					.connect(localJMXServiceURL);

			// MBeanServer mBeanServer =
			// ManagementFactory.getPlatformMBeanServer();
			localMBeanServerConnection = localJMXConnector
					.getMBeanServerConnection();
			// localMBeanServerConnection=mBeanServer;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
			JOptionPane
					.showMessageDialog(
							paramJPanel,
							"Could not connect to "
									+ host
									+ ":"
									+ port
									+ ".\n"
									+ "Check that the application you want to monitor has remote jmx monitoring enabled.",
							"TopThreads", 0);
			System.exit(1);
		}
		return localMBeanServerConnection;
	}
}

/*
 * Location: C:\Users\BeanSoft\Downloads\topthreads.jar Qualified Name:
 * biz.beansoft.jmx.topthreads.Main JD-Core Version: 0.6.2
 */
