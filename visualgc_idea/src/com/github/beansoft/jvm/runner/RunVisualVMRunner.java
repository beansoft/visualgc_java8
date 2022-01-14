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
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProcessProxy;
import com.intellij.execution.runners.ProcessProxyFactory;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
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

	protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env) throws ExecutionException {
		FileDocumentManager.getInstance().saveAllDocuments();
		ProcessProxy proxy = null;
		if (state instanceof JavaCommandLine) {
			patchJavaCommandLineParams((JavaCommandLine)state, env);
			proxy = ProcessProxyFactory.getInstance().createCommandLineProxy((JavaCommandLine)state);
		}
		return executeJavaState(state, env, proxy);
	}

//	@Override
//	protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment)
//			throws ExecutionException {
//		RunContentDescriptor runContentDescriptor = super.doExecute(state, environment);
//
//		RunProfileState currentState = environment.getState();
////		if (currentState == null) {
////			return;
////		}
//
//		ExecutionManager executionManager = ExecutionManager.getInstance(environment.getProject());
//		RunProfile runConfiguration = environment.getRunProfile();
//		if (runConfiguration instanceof RunConfigurationBase) {
//			RunConfigurationBase configuration = (RunConfigurationBase) runConfiguration;
//			AdditionalTabComponentManager additionalTabComponentManager = new MyAdditionalTabComponentManager();
//			configuration.createAdditionalTabComponents(additionalTabComponentManager, null);
//		}
//
//		ContentManager contentManager;
//		RunContentDescriptor contentToReuse = environment.getContentToReuse();
//		if (contentToReuse != null) {
//			RunnerLayoutUi runnerLayoutUi = contentToReuse.getRunnerLayoutUi();
//			Content runnerContent = runnerLayoutUi.createContent("beansoft.vgc.1",
//					new JButton("test"), "test", null, null);
//			runnerLayoutUi.addContent(runnerContent);
//		}
//
////		RunnerUtils.runVisualVM(this, env, state);
//		System.out.println("Open visualGC");// TODO
//		return runContentDescriptor;
//	}

	private void patchJavaCommandLineParams(@NotNull JavaCommandLine state, @NotNull ExecutionEnvironment env)
			throws ExecutionException {
		final JavaParameters parameters = state.getJavaParameters();
		patch(parameters, env.getRunnerSettings(), env.getRunProfile(), true);

		if (Registry.is("execution.java.always.debug") && DebuggerSettings.getInstance().ALWAYS_DEBUG) {
			ParametersList parametersList = parameters.getVMParametersList();
			if (parametersList.getList().stream().noneMatch(s -> s.startsWith("-agentlib:jdwp"))) {
				parametersList.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,quiet=y");
			}
		}
	}

	private @Nullable
	RunContentDescriptor executeJavaState(@NotNull RunProfileState state,
										  @NotNull ExecutionEnvironment env,
										  @Nullable ProcessProxy proxy) throws ExecutionException {
		ExecutionResult executionResult = state.execute(env.getExecutor(), this);
		if (proxy != null) {
			ProcessHandler handler = executionResult != null ? executionResult.getProcessHandler() : null;
			if (handler != null) {
				proxy.attach(handler);
				handler.addProcessListener(new ProcessAdapter() {
					@Override
					public void processTerminated(@NotNull ProcessEvent event) {
						proxy.destroy();
						handler.removeProcessListener(this);
					}
				});
			}
			else {
				proxy.destroy();
			}
		}

		if (executionResult == null) {
			return null;
		}

		RunContentBuilder contentBuilder = new RunContentBuilder(executionResult, env);
		if (!(state instanceof JavaCommandLineState) || ((JavaCommandLineState)state).shouldAddJavaProgramRunnerActions()) {
			addDefaultActions(contentBuilder, executionResult, state instanceof JavaCommandLine);
		}

		testAddTab(contentBuilder.getLogConsoleManager());

		return contentBuilder.showRunContent(env.getContentToReuse());
	}

	private static void addDefaultActions(@NotNull RunContentBuilder contentBuilder,
										  @NotNull ExecutionResult executionResult,
										  boolean isJavaCommandLine) {
		final ExecutionConsole executionConsole = executionResult.getExecutionConsole();
		final JComponent consoleComponent = executionConsole != null ? executionConsole.getComponent() : null;
		ProcessHandler processHandler = executionResult.getProcessHandler();
		assert processHandler != null : executionResult;
		final ControlBreakAction controlBreakAction = new ControlBreakAction(processHandler, contentBuilder.getSearchScope());
		if (consoleComponent != null) {
			controlBreakAction.registerCustomShortcutSet(controlBreakAction.getShortcutSet(), consoleComponent);
			processHandler.addProcessListener(new ProcessAdapter() {
				@Override
				public void processTerminated(@NotNull final ProcessEvent event) {
					processHandler.removeProcessListener(this);
					controlBreakAction.unregisterCustomShortcutSet(consoleComponent);
				}
			});
		}
		contentBuilder.addAction(controlBreakAction);
		if (isJavaCommandLine) {
			AttachDebuggerAction.add(contentBuilder, processHandler);
		}
		contentBuilder.addAction(new SoftExitAction(processHandler));
	}

	public void testAddTab(LogConsoleManagerBase logConsoleManagerBase) {
		if(logConsoleManagerBase == null) {
			return;
		}
//		LogConsoleManagerBase logConsoleManagerBase = null;
		logConsoleManagerBase.addAdditionalTabComponent(this.jProfilerTab, "JProfiler", null).setCloseable(false);
		AdditionalTabComponent additionalTabBase = new AdditionalTabBase();
//		logConsoleManagerBase.addAdditionalTabComponent(additionalTabBase, "Dummy");
//		logConsoleManagerBase.removeAdditionalTabComponent(aVar);
	}


}
