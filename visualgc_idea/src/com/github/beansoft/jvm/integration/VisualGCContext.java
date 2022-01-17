package com.github.beansoft.jvm.integration;

import com.intellij.openapi.diagnostic.Logger;

/*dirty, but works*/
public class VisualGCContext {
	private static final Logger log = Logger.getInstance(VisualGCContext.class.getName());
	private static volatile VisualGCContext currentlyExecuted;

	/** Process Id */
	protected int processId;

	/** App id used to identify a remote Java program such as Tomcat */
	protected Long appId;

	/** The run item's name */
	protected String name;

	public VisualGCContext(Long appId) {
		this.appId = appId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}

	public Long getAppId() {
		return appId;
	}

	public void save() {
		if (log.isDebugEnabled()) {
			log.debug("saving context: " + this.toString());
		}
		VisualGCContext.currentlyExecuted = this;
	}

	public static VisualGCContext load() {
		return currentlyExecuted;
	}

	public static boolean isValidProcessId(VisualGCContext visualGCContext) {
		return visualGCContext != null && visualGCContext.getProcessId() > 0;
	}

	public static boolean isValid(VisualGCContext visualGCContext) {
		return visualGCContext != null && visualGCContext.getAppId() != null;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("VisualVMContext");
		sb.append("{appId=").append(appId);
		sb.append("{processId=").append(processId);
//		sb.append(", jdkPath='").append(jdkPath).append('\'');
		sb.append('}');
		return sb.toString();
	}

}
