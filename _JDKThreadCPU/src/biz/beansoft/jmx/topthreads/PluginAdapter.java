package biz.beansoft.jmx.topthreads;

import com.sun.tools.jconsole.JConsoleContext;
import com.sun.tools.jconsole.JConsoleContext.ConnectionState;
import com.sun.tools.jconsole.JConsolePlugin;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Map;
import javax.management.MBeanServerConnection;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/**
 * 此类似乎不能正确被VisualVM所识别.
 * @author BeanSoft
 *
 */
public class PluginAdapter extends JConsolePlugin
{
  public static final String TAB_NAME = "线程CPU性能诊断";

  public Map<String, JPanel> getTabs()
  {
		TopThreadsPanel topthreadspanel = new TopThreadsPanel();
		ConnectionListener connectionlistener = new ConnectionListener(topthreadspanel);
		addContextPropertyChangeListener(connectionlistener);
		connectionlistener.checkConnection();
		JPanel pane = topthreadspanel;
		return Collections.singletonMap(TAB_NAME, pane);
  }

  public SwingWorker<?, ?> newSwingWorker()
  {
    return null;
  }

  private class ConnectionListener
    implements PropertyChangeListener
  {
    private TopThreadsPanel jtopp;

	public ConnectionListener(TopThreadsPanel topthreadspanel)
	{
		jtopp = topthreadspanel;
	}

    public void checkConnection()
    {
      MBeanServerConnection localMBeanServerConnection = PluginAdapter.this.getContext().getMBeanServerConnection();
      if (localMBeanServerConnection != null)
        this.jtopp.setMBeanServerConnection(localMBeanServerConnection);
    }

    public void propertyChange(PropertyChangeEvent paramPropertyChangeEvent)
    {
      if (paramPropertyChangeEvent.getPropertyName() == "connectionState")
      {
        JConsoleContext localJConsoleContext = PluginAdapter.this.getContext();
        if (paramPropertyChangeEvent.getNewValue().equals(JConsoleContext.ConnectionState.CONNECTED))
        {
          if (localJConsoleContext != null)
            this.jtopp.setMBeanServerConnection(localJConsoleContext.getMBeanServerConnection());
        }
        else
          this.jtopp.setMBeanServerConnection(null);
      }
    }
  }
}

/* Location:           C:\Users\BeanSoft\Downloads\topthreads.jar
 * Qualified Name:     biz.beansoft.jmx.topthreads.PluginAdapter
 * JD-Core Version:    0.6.2
 */