package com.github.beansoft.jvm.runner;

import com.github.beansoft.jvm.executor.RunVisualVMExecutor;
import com.intellij.diagnostic.logging.AdditionalTabComponent;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.configurations.*;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.jar.JarApplicationConfiguration;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.MessageUtil;
import org.jetbrains.annotations.NotNull;

public class RunVisualVMRunner extends DefaultJavaProgramRunner {
	private static final Logger log = Logger.getInstance(RunVisualVMRunner.class.getName());

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

		RunProfileState currentState = environment.getState();
//		if (currentState == null) {
//			return;
//		}

		ExecutionManager executionManager = ExecutionManager.getInstance(environment.getProject());
		RunProfile runConfiguration = environment.getRunProfile();
		if (runConfiguration instanceof RunConfigurationBase) {
			RunConfigurationBase configuration = (RunConfigurationBase) runConfiguration;
			AdditionalTabComponentManager additionalTabComponentManager = new MyAdditionalTabComponentManager();
			configuration.createAdditionalTabComponents(additionalTabComponentManager, null);
		}

//		RunnerUtils.runVisualVM(this, env, state);
		System.out.println("Open visualGC");// TODO
		return runContentDescriptor;
	}

}
