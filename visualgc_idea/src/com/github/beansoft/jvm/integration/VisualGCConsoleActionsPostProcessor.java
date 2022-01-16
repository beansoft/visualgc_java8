package com.github.beansoft.jvm.integration;

import com.github.beansoft.jvm.action.StartVisualGCConsoleAction;
import com.intellij.execution.actions.ConsoleActionsPostProcessor;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class VisualGCConsoleActionsPostProcessor extends ConsoleActionsPostProcessor {
	private static final Logger log = Logger.getInstance(VisualGCConsoleActionsPostProcessor.class.getName());

	@NotNull
	@Override
	public AnAction[] postProcess(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		VisualVMContext context = VisualVMContext.load();
		ArrayList<AnAction> anActions = new ArrayList<AnAction>();
		anActions.add(new StartVisualGCConsoleAction(context));
		anActions.addAll(Arrays.asList(actions));
		return anActions.toArray(new AnAction[anActions.size()]);
	}
}
