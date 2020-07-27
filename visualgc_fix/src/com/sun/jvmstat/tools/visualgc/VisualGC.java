package com.sun.jvmstat.tools.visualgc;

import beansoft.jvm.hotspot.util.Exceptions;
import beansoft.jvm.hotspot.util.GetProcessID;
import com.sun.jvmstat.graph.GridDrawer;
import com.sun.jvmstat.graph.Level;
import com.sun.jvmstat.graph.Line;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.VmIdentifier;
import sun.jvmstat.monitor.event.HostEvent;
import sun.jvmstat.monitor.event.HostListener;
import sun.jvmstat.monitor.event.VmStatusChangeEvent;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class VisualGC
{

	private static volatile boolean active = true;
	private static volatile boolean terminated = false;
	private static Arguments arguments;

	// Begin >>>
	private static boolean hasMetaspace;

	private static final boolean ORIGINAL_UI = Boolean.getBoolean("org.graalvm.visualvm.modules.visualgc.originalUI");

	private static final Color NORMAL_GRAY = new Color(165, 165, 165);

	private static final Color LIGHTER_GRAY = new Color(220, 220, 220);

	private static final Color EVEN_LIGHTER_GRAY = new Color(242, 242, 242);

	static {
		customizeColors();
	}

	private static void customizeColors() {
		if (ORIGINAL_UI)
			return;
		System.setProperty("graphgc.gc.color", colorString(152, 178, 0));
		System.setProperty("graphgc.class.color", "" + colorString(81, 131, 160));
		System.setProperty("graphgc.compile.color", colorString(52, 87, 106));
		System.setProperty("eden.color", "" + colorString(200, 145, 1));
		System.setProperty("survivor.color", colorString(193, 101, 0));
		System.setProperty("old.color", "" + colorString(127, 122, 2));
		System.setProperty("perm.color", "" + colorString(235, 156, 8));
	}

	private static String colorString(int r, int g, int b) {
		return Integer.toString((new Color(r, g, b)).getRGB());
	}

	private static void fixMetaspace(GraphGC graphGC) {
		if (hasMetaspace)
			try {
				Field PERM_PANEL = GraphGC.class.getDeclaredField("permPanel");
				Field BORDER_STRING = GCSpacePanel.class.getDeclaredField("borderString");
				PERM_PANEL.setAccessible(true);
				BORDER_STRING.setAccessible(true);
				GCSpacePanel permPanel = (GCSpacePanel)PERM_PANEL.get(graphGC);
				String borderString = (String)BORDER_STRING.get(permPanel);
				BORDER_STRING.set(permPanel, borderString.replace("Perm Gen", "Metaspace"));
			} catch (NoSuchFieldException ex) {
				Exceptions.printStackTrace(ex);
			} catch (SecurityException ex) {
				Exceptions.printStackTrace(ex);
			} catch (IllegalArgumentException ex) {
				Exceptions.printStackTrace(ex);
			} catch (IllegalAccessException ex) {
				Exceptions.printStackTrace(ex);
			}
	}

	private static void customizeComponents(Component component, List<GridDrawer> gridDrawers) {
		if (ORIGINAL_UI)
			return;
		if (component == null)
			return;
		if (!(component instanceof JComponent))
			return;
		JComponent jComponent = (JComponent)component;
		if (jComponent.getBorder() instanceof TitledBorder) {
			TitledBorder titledBorder = (TitledBorder)jComponent.getBorder();
			TitledBorder newBorder = new TitledBorder(titledBorder.getTitle());
			newBorder.setBorder(BorderFactory.createLineBorder(NORMAL_GRAY, 1));
			newBorder.setTitleColor(titledBorder.getTitleColor());
			Font titleFont = newBorder.getTitleFont();
			if (titleFont == null)
				titleFont = UIManager.getFont("TitledBorder.font");
			if (titleFont == null)
				titleFont = UIManager.getFont("Label.font");
			newBorder.setTitleFont(titleFont.deriveFont(1));
			jComponent.setBorder(newBorder);
		} else if (jComponent instanceof Line) {
			Line line = (Line)jComponent;
			line.setGridPrimaryColor(LIGHTER_GRAY);
			line.setGridSecondaryColor(EVEN_LIGHTER_GRAY);
		} else if (jComponent instanceof Level) {
			Level level = (Level)jComponent;
			level.setPreferredSize(new Dimension(25, 15));
			level.setMinimumSize(level.getPreferredSize());
			try {
				Class<?> levelClass = level.getClass();
				Field gridDrawerField = levelClass.getDeclaredField("gridDrawer");
				gridDrawerField.setAccessible(true);
				GridDrawer gridDrawer = (GridDrawer)gridDrawerField.get(level);
				gridDrawer.setPrimaryColor(LIGHTER_GRAY);
				if (gridDrawers != null)
					gridDrawers.add(gridDrawer);
			} catch (Exception e) {
				Exceptions.printStackTrace(e);
			}
		} else if (jComponent instanceof JLabel) {
			JLabel label = (JLabel)jComponent;
			label.setFont(UIManager.getFont("Label.font"));
			if (label.getText().endsWith(": "))
				label.setFont(label.getFont().deriveFont(1));
			label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		}
		jComponent.setOpaque(false);
		for (Component child : jComponent.getComponents())
			customizeComponents(child, gridDrawers);
	}

	// <<< End

	public VisualGC()
	{
	}

	public static void main(String args[])
	{
//		args = new String[]{"412"};
//		args = new String[]{ GetProcessID.getPid() + ""};
		if (args == null || args.length == 0) {
			JList list = new JList(JpsHelper.getJvmPSList().toArray());
			String s = JOptionPane.showInputDialog((Component)null, list, "请选择需要分析的JVM进程或输入一个新值，点取消将分析VisualGC自身", 3);
			if (s != null && s.length() != 0) {
				args = new String[]{s};
			} else {
				Object val = list.getSelectedValue();
				if (val == null) {
					args = new String[]{String.valueOf(GetProcessID.getPid())};
				} else {
					s = val.toString().substring(0, val.toString().indexOf(32));
					args = new String[]{s};
				}
			}

			System.out.println(s);
		}

		try {
			arguments = new Arguments(args);
		} catch (IllegalArgumentException var29) {
			System.err.println(var29.getMessage());
			Arguments.printUsage(System.err);
			System.exit(1);
		}

		if (arguments.isHelp()) {
			Arguments.printUsage(System.out);
			System.exit(1);
		}

		if (arguments.isVersion())
		{
			Arguments.printVersion(System.out);
			System.exit(1);
		}

		String s = arguments.vmIdString();
		int i = arguments.samplingInterval();
		MonitoredVmModel monitoredvmmodel = null;
		MonitoredHost monitoredhost = null;
		MonitoredVm monitoredvm = null;
		try
		{
			VmIdentifier vmidentifier = arguments.vmId();
			monitoredhost = MonitoredHost.getMonitoredHost(vmidentifier);
			monitoredvm = monitoredhost.getMonitoredVm(vmidentifier, i);
			monitoredvmmodel = new MonitoredVmModel(monitoredvm);
			hasMetaspace = ModelFixer.fixMetaspace(monitoredvmmodel, monitoredvm);

			class TerminationHandler
				implements HostListener
			{

				Integer lvmid;
				MonitoredHost host;

				public void vmStatusChanged(VmStatusChangeEvent vmstatuschangeevent)
				{
					if (vmstatuschangeevent.getTerminated().contains(lvmid) || !vmstatuschangeevent.getActive().contains(lvmid))
						VisualGC.terminated = true;
				}

				public void disconnected(HostEvent hostevent)
				{
					if (host == hostevent.getMonitoredHost())
						VisualGC.terminated = true;
				}

			TerminationHandler(int i, MonitoredHost monitoredhost)
			{
				lvmid = new Integer(i);
				host = monitoredhost;
			}
			}

			if (vmidentifier.getLocalVmId() != 0)
				monitoredhost.addHostListener(new TerminationHandler(vmidentifier.getLocalVmId(), monitoredhost));
		}
		catch (MonitorException monitorexception)
		{
			if (monitorexception.getMessage() != null)
			{
				System.err.println(monitorexception.getMessage());
			} else
			{
				Throwable throwable = monitorexception.getCause();
				if (throwable != null && throwable.getMessage() != null)
					System.err.println(throwable.getMessage());
				else
					monitorexception.printStackTrace();
			}
			if (monitoredhost != null && monitoredvm != null)
				try
				{
					monitoredhost.detach(monitoredvm);
				}
				catch (Exception exception) { }
			System.exit(1);
		}
		GCSample gcsample = new GCSample(monitoredvmmodel);
		int j = Integer.getInteger("visualheap.x", 0).intValue();
		int k = Integer.getInteger("visualheap.y", 0).intValue();
		int l = Integer.getInteger("visualheap.width", 450).intValue();
		int i1 = Integer.getInteger("visualheap.height", 600).intValue();
		int j1 = Integer.getInteger("graphgc.x", j + l).intValue();
		int k1 = Integer.getInteger("graphgc.y", k).intValue();
		int l1 = Integer.getInteger("graphgc.width", 450).intValue();
		int i2 = Integer.getInteger("graphgc.height", 600).intValue();
		int j2 = Integer.getInteger("agetable.x", j).intValue();
		int k2 = Integer.getInteger("agetable.y", k + i1).intValue();
		int l2 = Integer.getInteger("agetable.width", l1 + l).intValue();
		int i3 = Integer.getInteger("agetable.height", 200).intValue();
		final GraphGC graphgc = new GraphGC(gcsample);
		graphgc.setBounds(j1, k1, l1, i2);
		fixMetaspace(graphgc);
		customizeComponents(graphgc.getContentPane(), null);

		VisualAgeHistogram visualagehistogram = null;
		if (gcsample.ageTableSizes != null)
		{
			visualagehistogram = new VisualAgeHistogram(gcsample);
			visualagehistogram.setBounds(j2, k2, l2, i3);
			customizeComponents(visualagehistogram.getContentPane(), null);
		}
		final VisualAgeHistogram visualagehistogram1 = visualagehistogram;
		List<GridDrawer> gridDrawers = new ArrayList<GridDrawer>();
		final VisualHeap visualheap = new VisualHeap(graphgc, visualagehistogram1, gcsample){
			public void updateLevel(GCSample currentSample) {
				super.updateLevel(currentSample);
				for (GridDrawer gridDrawer : gridDrawers)
					gridDrawer.setSecondaryColor(EVEN_LIGHTER_GRAY);
			}
		};
		visualheap.setBounds(j, k, l, i1);
		customizeComponents(visualheap.getContentPane(), gridDrawers);
//		visualheap.getContentPane().getComponent(0).setVisible(false);// Hide cmd info pane
		visualheap.show();
		graphgc.show();
		if (visualagehistogram1 != null)
			visualagehistogram1.show();
		boolean flag = false;
		GCSample gcsample1 = null;
		do
		{
			if (!active)
				break;
			try
			{
				Thread.sleep(i);
			}
			catch (InterruptedException interruptedexception) { }
			if (terminated)
			{
				if (!flag)
				{
					flag = true;
					try
					{
						SwingUtilities.invokeAndWait(new Runnable() {

							public void run()
							{
								String as[] = {
									"Monitored Java Virtual Machine Terminated", " ", "Exit visualgc?", " "
								};
								int j3 = JOptionPane.showConfirmDialog(visualheap, as, "Target Terminated", 0, 1);
								if (j3 == 0)
									System.exit(0);
							}



						}
);
					}
					catch (Exception exception1)
					{
						exception1.printStackTrace();
					}
					gcsample1 = new GCSample(monitoredvmmodel);
				}
			} else
			{
				final GCSample gcsample2 = gcsample1 == null ? new GCSample(monitoredvmmodel) : gcsample1;
				SwingUtilities.invokeLater(new Runnable() {

					public void run()
					{
						visualheap.update(gcsample2);
						graphgc.update(gcsample2);
						if (visualagehistogram1 != null)
							visualagehistogram1.update(gcsample2);
					}


				}
);
			}
		} while (true);
	}



}
