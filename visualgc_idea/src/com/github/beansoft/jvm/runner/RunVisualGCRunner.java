package com.github.beansoft.jvm.runner;

import com.github.beansoft.jvm.executor.RunVisualGCExecutor;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.*;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.jar.JarApplicationConfiguration;
import com.intellij.execution.process.*;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.target.TargetEnvironmentAwareRunProfileState;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;

public class RunVisualGCRunner extends DefaultJavaProgramRunner {
	private static final Logger log = Logger.getInstance(RunVisualGCRunner.class.getName());
//	private final JProfilerTab jProfilerTab = new JProfilerTab(new JLabel("test"));

	@NotNull
	public String getRunnerId() {
		return RunVisualGCExecutor.RUN_WITH_VISUAL_VM;
	}

	public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
		return executorId.equals(RunVisualGCExecutor.RUN_WITH_VISUAL_VM) && (profile instanceof ModuleRunProfile || profile instanceof JarApplicationConfiguration) && !(profile instanceof RemoteConfiguration);
	}

	@Override
	public void execute(@NotNull final ExecutionEnvironment env) throws ExecutionException {
		super.execute(env);

		RunProfileState state = env.getState();
//		RunContentDescriptor runContentDescriptor = env.getContentToReuse();
//		System.out.println("run profile name=" + env.getRunProfile().getName());// 配置项的名字
//		ProcessHandler processHandler = runContentDescriptor.getProcessHandler();
//		RunnerUtil.startVisualGC(processHandler, env);
	}

	/**
	 * In IDEA 2021.3, this method is called when you running a Java program, but in 2020.3 the doExecute is called.
	 * @see #doExecute(RunProfileState, ExecutionEnvironment)
	 * @param state
	 * @param env
	 * @return
	 * @throws ExecutionException
	 */
	@NotNull
	protected Promise<RunContentDescriptor> doExecuteAsync(@NotNull TargetEnvironmentAwareRunProfileState state,
																	 @NotNull ExecutionEnvironment env)
			throws ExecutionException {
		Promise<RunContentDescriptor> result = super.doExecuteAsync(state,env);
		result.thenAsync( runContentDescriptor -> {
//			System.out.println("run profile name=" + env.getRunProfile().getName());// 配置项的名字
			if(runContentDescriptor != null) {
				ProcessHandler processHandler = runContentDescriptor.getProcessHandler();

				RunnerLayoutUi runnerLayoutUi = runContentDescriptor.getRunnerLayoutUi();
				if(runnerLayoutUi != null) {
					// Run in UI thread
					UIUtil.invokeLaterIfNeeded(() -> RunnerUtil.buildVgcRunnerUITab(env, runnerLayoutUi)
					);
				}

				RunnerUtil.startVisualGC(processHandler, env);
			}
			return result;
		});

		return result;
	}

	@Override
	protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env)
			throws ExecutionException {
		// 开始执行进程
		RunContentDescriptor runContentDescriptor = super.doExecute(state, env);
//		System.out.println("run profile name=" + env.getRunProfile().getName());// 配置项的名字
		// environment.getRunProfile() 的实际类型可能是 com.intellij.execution.application.ApplicationConfiguration , 可以
		// 获取到 main 类的名字

		RunnerLayoutUi runnerLayoutUi = runContentDescriptor.getRunnerLayoutUi();

		RunnerUtil.buildVgcRunnerUITab(env,runnerLayoutUi);
////		if (runnerLayoutUi != null) {
//			Content runnerContent = runnerLayoutUi.createContent("beansoft.vgc.help",
//					new JButton("test"), "VisualGC", null, null);
//			runnerLayoutUi.addContent(runnerContent);
//			DefaultActionGroup actionGroup = new DefaultActionGroup();
//			actionGroup.add(ActionManager.getInstance().getAction("visualgc.MakeCoffeeAction"));
//			// 添加左侧Action
////			runnerLayoutUi.getOptions().setTopLeftToolbar(actionGroup, ActionPlaces.UNKNOWN);
//		}

		ProcessHandler processHandler = runContentDescriptor.getProcessHandler();
		RunnerUtil.startVisualGC(processHandler, env);

//		if(myProcessHandler instanceof KillableColoredProcessHandler) {
//			String pid = String.valueOf(OSProcessUtil.getProcessID(((KillableColoredProcessHandler)myProcessHandler).getProcess()));
//			System.out.println("Open visualGC pid = " + pid);// TODO
//		}

//		RunnerUtils.runVisualVM(this, env, state);

		return runContentDescriptor;
	}
}