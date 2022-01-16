package com.github.beansoft.jvm.integration;

import com.intellij.openapi.diagnostic.Logger;

/*dirty, but works*/
public class VisualVMContext {
	private static final Logger log = Logger.getInstance(VisualVMContext.class.getName());
	private static volatile VisualVMContext currentlyExecuted;

	protected Long processId;

	public VisualVMContext(Long processId) {
		this.processId = processId;
	}

	public Long getProcessId() {
		return processId;
	}

	public void save() {
		if (log.isDebugEnabled()) {
			log.debug("saving context: " + this.toString());
		}
		VisualVMContext.currentlyExecuted = this;
	}

	public static VisualVMContext load() {
		return currentlyExecuted;
	}

	public static boolean isValid(VisualVMContext visualVMContext) {
		return visualVMContext != null && visualVMContext.getProcessId() != null;
	}

}
