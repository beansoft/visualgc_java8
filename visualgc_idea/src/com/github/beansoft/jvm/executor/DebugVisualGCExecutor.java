package com.github.beansoft.jvm.executor;

import com.intellij.execution.executors.DefaultRunExecutor;
import icons.PluginIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DebugVisualGCExecutor extends DefaultRunExecutor {
	@NonNls
	public static final String EXECUTOR_ID = "Debug VisualGC with";
	public static final String DEBUG_WITH_VISUAL_VM = "Debug VisualGC";

	@NotNull
	public String getToolWindowId() {
		return getId();
	}

	public Icon getToolWindowIcon() {
		return PluginIcons.DebugVisualGC_13;
	}

	@NotNull
	public Icon getIcon() {
		return PluginIcons.DebugVisualGC;
	}

	public Icon getDisabledIcon() {
		return null;
	}

	public String getDescription() {
		return EXECUTOR_ID;
	}

	@NotNull
	public String getActionName() {
		return DEBUG_WITH_VISUAL_VM;
	}

	@NotNull
	public String getId() {
		return EXECUTOR_ID;
	}

	@NotNull
	public String getStartActionText() {
		return EXECUTOR_ID;
	}

	public String getContextActionId() {
		// HACK: ExecutorRegistryImpl expects this to be non-null, but we don't want any context actions for every file
		return getId() + " context-action-does-not-exist";
	}

	public String getHelpId() {
		return null;
	}

}
