package com.github.beansoft.jvm.executor;

import javax.swing.*;


import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

import com.intellij.execution.executors.DefaultRunExecutor;

public class RunVisualGCExecutor extends DefaultRunExecutor {

	public static final String RUN_WITH_VISUAL_VM = "Run VisualGC with";
	public static final String RUN_WITH_VISUAL_VM1 = "Run VisualGC";

	@NotNull
	public String getToolWindowId() {
		return getId();
	}

	public Icon getToolWindowIcon() {
		return PluginIcons.RunVisualGC_13;
	}

	@NotNull
	public Icon getIcon() {
		return PluginIcons.RunVisualGC;
	}

	public Icon getDisabledIcon() {
		return null;
	}

	public String getDescription() {
		return RUN_WITH_VISUAL_VM;
	}

	@NotNull
	public String getActionName() {
		return RUN_WITH_VISUAL_VM1;
	}

	@NotNull
	public String getId() {
		return RUN_WITH_VISUAL_VM;
	}

	@NotNull
	public String getStartActionText() {
		return RUN_WITH_VISUAL_VM;
	}

	public String getContextActionId() {
		// HACK: ExecutorRegistryImpl expects this to be non-null, but we don't want any context actions for every file
		return getId() + " context-action-does-not-exist";
	}

	public String getHelpId() {
		return null;
	}
}
