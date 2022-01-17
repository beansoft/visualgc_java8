package com.github.beansoft.jvm.action;

import com.github.beansoft.jvm.ApplicationSettingsService;
import com.github.beansoft.jvm.LogHelper;
import com.github.beansoft.jvm.integration.VisualGCContext;
import com.github.beansoft.jvm.runner.RunnerUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import icons.PluginIcons;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 在控制台中启动VisualGC检测当前进程.
 */
public class StartVisualGCConsoleAction extends AnAction {
	private VisualGCContext visualGCContext;
	private boolean postConstructContextSet;
	private long created;

	public static volatile List<StartVisualGCConsoleAction> currentlyExecuted = new LinkedList<>();

	public StartVisualGCConsoleAction() {
	}

	public StartVisualGCConsoleAction(VisualGCContext visualGCContext) {
		super("VisualGC Monitor This Process", null, PluginIcons.RunVisualGC);
		this.visualGCContext = visualGCContext;
		created = System.currentTimeMillis();
		currentlyExecuted.add(this);
		LogHelper.print("created with " + visualGCContext, this);
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		final Presentation presentation = e.getPresentation();
		if (!VisualGCContext.isValid(visualGCContext)) {
			presentation.setEnabledAndVisible(false);
		} else {
			presentation.setEnabledAndVisible(false);
			presentation.setDescription("Open VisualGC with process id=" + visualGCContext.getProcessId());
		}
	}

	@Override
	public void actionPerformed(final AnActionEvent e) {
//		System.out.println(e.getProject());
//		if (!MyConfigurable.openSettingsIfNotConfigured(e.getProject())) {
//			return;
//		}
		RunnerUtil.startVisualGCFrameInNewThread(visualGCContext.getProcessId(), visualGCContext.getName());
	}

	public void setVisualGCContext(VisualGCContext visualGCContext) {
		if (postConstructContextSet) {
			LogHelper.print("setVisualGCContext false with " + visualGCContext, this);
		} else {
			postConstructContextSet = true;
			LogHelper.print("setVisualGCContext " + visualGCContext, this);
			this.visualGCContext = visualGCContext;
		}
	}

	public long getCreated() {
		return created;
	}

	public static void setVisualGCContextToRecentlyCreated(VisualGCContext visualGCContext) {
		LogHelper.print("#setVisualGCContextToRecentlyCreated" + visualGCContext, null);
		Iterator<StartVisualGCConsoleAction> iterator = currentlyExecuted.iterator();
		while (iterator.hasNext()) {
			StartVisualGCConsoleAction next = iterator.next();
			if (isRecentlyCreated(next)) {
				next.setVisualGCContext(visualGCContext);
			} else {
				LogHelper.print("#setVisualGCContextToRecentlyCreated remove", null);
				iterator.remove();
			}
		}
	}

	private static boolean isRecentlyCreated(StartVisualGCConsoleAction next) {
		long l = System.currentTimeMillis() - next.getCreated();
		LogHelper.print("#isRecentlyCreated " + l + " " + next, null);
		return l < ApplicationSettingsService.getInstance().getState().getDurationToSetContextToButtonAsLong();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("StartVisualGCConsoleAction");
		sb.append("{visualGCContext=").append(visualGCContext);
		sb.append(", created=").append(created);
		sb.append('}');
		return sb.toString();
	}
}
