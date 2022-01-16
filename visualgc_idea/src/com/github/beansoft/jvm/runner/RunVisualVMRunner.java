package com.github.beansoft.jvm.runner;

import com.github.beansoft.jvm.executor.RunVisualVMExecutor;
import com.intellij.debugger.settings.DebuggerSettings;
import com.intellij.diagnostic.logging.AdditionalTabComponent;
import com.intellij.diagnostic.logging.LogConsoleManagerBase;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.*;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.jar.JarApplicationConfiguration;
import com.intellij.execution.process.*;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProcessProxy;
import com.intellij.execution.runners.ProcessProxyFactory;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.impl.ContentImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class RunVisualVMRunner extends DefaultJavaProgramRunner {
	private static final Logger log = Logger.getInstance(RunVisualVMRunner.class.getName());
	private final JProfilerTab jProfilerTab = new JProfilerTab(new JLabel("test"));


	@NotNull
	public String getRunnerId() {
		return RunVisualVMExecutor.RUN_WITH_VISUAL_VM;
	}

	public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
		return executorId.equals(RunVisualVMExecutor.RUN_WITH_VISUAL_VM) && (profile instanceof ModuleRunProfile || profile instanceof JarApplicationConfiguration) && !(profile instanceof RemoteConfiguration);
	}

	@Override
	public void execute(@NotNull final ExecutionEnvironment env) throws ExecutionException {
		super.execute(env);
	}

	@Override
	protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment)
			throws ExecutionException {
		RunContentDescriptor runContentDescriptor = super.doExecute(state, environment);

		RunnerLayoutUi runnerLayoutUi = runContentDescriptor.getRunnerLayoutUi();
		if (runnerLayoutUi != null) {
			Content runnerContent = runnerLayoutUi.createContent("beansoft.vgc.1",
					new JButton("test"), "test", null, null);
			runnerLayoutUi.addContent(runnerContent);
			DefaultActionGroup actionGroup = new DefaultActionGroup();
			actionGroup.add(ActionManager.getInstance().getAction("visualgc.MakeCoffeeAction"));
			// 添加左侧Action
			runnerLayoutUi.getOptions().setTopLeftToolbar(actionGroup, ActionPlaces.UNKNOWN);
		}

		ProcessHandler processHandler = runContentDescriptor.getProcessHandler();
		RunnerUtil.startVisualGC(processHandler);

//		if(myProcessHandler instanceof KillableColoredProcessHandler) {
//			String pid = String.valueOf(OSProcessUtil.getProcessID(((KillableColoredProcessHandler)myProcessHandler).getProcess()));
//			System.out.println("Open visualGC pid = " + pid);// TODO
//		}

//		RunnerUtils.runVisualVM(this, env, state);

		return runContentDescriptor;
	}
}