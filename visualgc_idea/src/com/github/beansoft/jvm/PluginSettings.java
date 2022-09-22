package com.github.beansoft.jvm;


import com.github.beansoft.jvm.integration.VisualGCHelper;

public class PluginSettings {

	private String visualVmExecutable;
	private String durationToSetContextToButton = "10000";
	private String delayForVisualVMStart = "10000";
	/** Remote JVM host address */
	private String remoteJvmURL;


	public String getVisualVmExecutable() {
		return visualVmExecutable;
	}

	public void setVisualVmExecutable(final String visualVmExecutable) {
		this.visualVmExecutable = visualVmExecutable;
	}


	public static boolean isValid(PluginSettings state) {
		return state != null && VisualGCHelper.isValidPath(state.getVisualVmExecutable());
	}

	public String getDurationToSetContextToButton() {
		return durationToSetContextToButton;
	}

	public void setDurationToSetContextToButton(final String durationToSetContextToButton) {
		this.durationToSetContextToButton = durationToSetContextToButton;
	}

	public String getDelayForVisualVMStart() {
		return delayForVisualVMStart;
	}

	public void setDelayForVisualVMStart(String delayForVisualVMStart) {
		this.delayForVisualVMStart = delayForVisualVMStart;
	}

	public long getDurationToSetContextToButtonAsLong() {
		return Long.parseLong(durationToSetContextToButton);
	}

	public long getDelayForVisualVMStartAsLong() {
		return Long.parseLong(delayForVisualVMStart);
	}


	public String getRemoteJvmURL() {
		return remoteJvmURL;
	}

	public void setRemoteJvmURL(String remoteJvmURL) {
		this.remoteJvmURL = remoteJvmURL;
	}
}
