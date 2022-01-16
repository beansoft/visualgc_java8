package com.github.beansoft.jvm.action;

import com.github.beansoft.jvm.LogHelper;
import com.github.beansoft.jvm.Resources;
import com.github.beansoft.jvm.integration.VisualVMContext;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;


import java.util.LinkedList;
import java.util.List;

public class StartVisualGCConsoleAction extends AnAction {
	private VisualVMContext visualVMContext;
	private boolean postConstructContextSet;
	private long created;

	public static volatile List<StartVisualGCConsoleAction> currentlyExecuted = new LinkedList<>();

	public StartVisualGCConsoleAction() {
	}

	public StartVisualGCConsoleAction(VisualVMContext visualVMContext) {
		super("Start VisualVM", null, Resources.CONSOLE_RUN);
		this.visualVMContext = visualVMContext;
		created = System.currentTimeMillis();
		currentlyExecuted.add(this);
		LogHelper.print("created with " + visualVMContext, this);
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		final Presentation presentation = e.getPresentation();
//		if (!VisualVMContext.isValid(visualVMContext)) {
////			presentation.setVisible(false);
//			presentation.setEnabled(false);
//		} else {
//			presentation.setDescription("Open VisualVM with id=" + visualVMContext.getProcessId());
//		}
	}

	@Override
	public void actionPerformed(final AnActionEvent e) {
		System.out.println(e.getProject());
//		if (!MyConfigurable.openSettingsIfNotConfigured(e.getProject())) {
//			return;
//		}
//		VisualVMHelper.startVisualVM(visualVMContext, e.getProject(), this);
	}

	public void setVisualVMContext(VisualVMContext visualVMContext) {
		if (postConstructContextSet) {
			LogHelper.print("setVisualVMContext false with " + visualVMContext, this);
		} else {
			postConstructContextSet = true;
			LogHelper.print("setVisualVMContext " + visualVMContext, this);
			this.visualVMContext = visualVMContext;
		}
	}

	public long getCreated() {
		return created;
	}

	public static void setVisualVMContextToRecentlyCreated(VisualVMContext visualVMContext) {
		LogHelper.print("#setVisualVMContextToRecentlyCreated" + visualVMContext, null);
//		Iterator<StartVisualVMConsoleAction> iterator = currentlyExecuted.iterator();
//		while (iterator.hasNext()) {
//			StartVisualVMConsoleAction next = iterator.next();
//			if (isRecentlyCreated(next)) {
//				next.setVisualVMContext(visualVMContext);
//			} else {
//				LogHelper.print("#setVisualVMContextToRecentlyCreated remove", null);
//				iterator.remove();
//			}
//		}
	}

//	private static boolean isRecentlyCreated(StartVisualVMConsoleAction next) {
//		long l = System.currentTimeMillis() - next.getCreated();
//		LogHelper.print("#isRecentlyCreated " + l + " " + next, null);
//		return l < ApplicationSettingsService.getInstance().getState().getDurationToSetContextToButtonAsLong();
//	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("StartVisualVMConsoleAction");
		sb.append("{visualVMContext=").append(visualVMContext);
		sb.append(", created=").append(created);
		sb.append('}');
		return sb.toString();
	}
}
