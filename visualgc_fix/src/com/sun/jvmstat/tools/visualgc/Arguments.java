package com.sun.jvmstat.tools.visualgc;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import sun.jvmstat.monitor.VmIdentifier;

public class Arguments
{

	private static boolean debug = Boolean.getBoolean("Arguments.debug");
	private static final int DEFAULT_INTERVAL = 500;
	private static final String VERSION_FILE = "version";
	private boolean help;
	private boolean version;
	private int interval;
	private String vmIdString;
	private VmIdentifier vmId;

	public static void printUsage(PrintStream printstream)
	{
		printVersion(printstream);
		printstream.println("usage: visualgc -help");
		printstream.println("       visualgc <vmid> [<interval>]");
		printstream.println();
		printstream.println("Definitions:");
		printstream.println("  <vmid>        Virtual Machine Identifier. A vmid takes the following form:");
		printstream.println("                     <lvmid>[@<hostname>[:<port>]]");
		printstream.println("                Where <lvmid> is the local vm identifier for the target");
		printstream.println("                Java virtual machine, typically a process id; <hostname> is");
		printstream.println("                the name of the host running the target Java virtual machine;");
		printstream.println("                and <port> is the port number for the rmiregistry on the");
		printstream.println("                target host. See the visualgc documentation for a more complete");
		printstream.println("                description of a <vmid>.");
		printstream.println("  <interval>    Sampling interval. The following forms are allowed:");
		printstream.println("                    <n>[\"ms\"|\"s\"]");
		printstream.println("                Where <n> is an integer and the suffix specifies the units as ");
		printstream.println("                milliseconds(\"ms\") or seconds(\"s\"). The default units are \"ms\".");
		printstream.println("                The default interval is 500ms");
	}

	public static void printVersion(PrintStream printstream)
	{
		URL url = (Arguments.class).getClassLoader().getResource("version");
		try
		{
			BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(url.openStream()));
			for (String s = null; (s = bufferedreader.readLine()) != null;)
				printstream.println(s);

		}
		catch (Exception exception)
		{
			System.err.println("Unexpected exception: " + exception.getMessage());
			exception.printStackTrace();
			System.exit(1);
		}
	}

	private static int toMillis(String s)
		throws IllegalArgumentException
	{
		String as[] = {
			"ms", "s"
		};
		String s1 = null;
		String s2 = null;
		int i = 0;
		do
		{
			if (i >= as.length)
				break;
			int j = s.indexOf(as[i]);
			if (j > 0)
			{
				s1 = s.substring(j);
				s2 = s.substring(0, j);
				break;
			}
			i++;
		} while (true);
		if (s1 == null)
			s2 = s;
		try
		{
			i = Integer.parseInt(s2);
			if (s1 == null || s1.compareTo("ms") == 0)
				return i;
		}
		catch (NumberFormatException numberformatexception)
		{
			throw new IllegalArgumentException("Could not convert interval: " + s);
		}
		if (s1.compareTo("s") == 0)
			return i * 1000;
		throw new IllegalArgumentException("Unsupported interval time unit: " + s1);
	}

	public Arguments(String as[])
		throws IllegalArgumentException
	{
		interval = -1;
		int i = 0;
		if (as.length < 1 || as.length > 2)
			throw new IllegalArgumentException("invalid argument count");
		if (as[0].compareTo("-?") == 0 || as[0].compareTo("-help") == 0)
			if (as.length != 1)
			{
				throw new IllegalArgumentException("invalid argument count");
			} else
			{
				help = true;
				return;
			}
		if (as[0].compareTo("-v") == 0 || as[0].compareTo("-version") == 0)
		{
			version = true;
			return;
		}
		i = 0;
		if (i < as.length && as[i].startsWith("-"))
		{
			String s = as[i];
			String s1 = null;
			int j = as[i].indexOf('@');
			if (j < 0)
				s1 = as[i];
			else
				s1 = as[i].substring(0, j);
			int k;
			try
			{
				k = Integer.parseInt(s1);
			}
			catch (NumberFormatException numberformatexception)
			{
				throw new IllegalArgumentException("illegal argument: " + as[i]);
			}
		}
		switch (as.length - i)
		{
		case 2: // '\002'
			interval = toMillis(as[as.length - 1]);
			vmIdString = as[as.length - 2];
			break;

		case 1: // '\001'
			vmIdString = as[as.length - 1];
			break;
		}
		if (interval == -1)
			interval = 500;
		try
		{
			vmId = new VmIdentifier(vmIdString);
		}
		catch (URISyntaxException urisyntaxexception)
		{
			IllegalArgumentException illegalargumentexception = new IllegalArgumentException("Malformed VM Identifier: " + vmIdString);
			illegalargumentexception.initCause(urisyntaxexception);
			throw illegalargumentexception;
		}
	}

	public boolean isHelp()
	{
		return help;
	}

	public boolean isVersion()
	{
		return version;
	}

	public String vmIdString()
	{
		return vmIdString;
	}

	public VmIdentifier vmId()
	{
		return vmId;
	}

	public int samplingInterval()
	{
		return interval;
	}

}
