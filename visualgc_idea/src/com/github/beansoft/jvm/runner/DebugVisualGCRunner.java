package com.github.beansoft.jvm.runner;

import com.github.beansoft.jvm.LogHelper;
import com.github.beansoft.jvm.executor.DebugVisualGCExecutor;
import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.ModuleRunProfile;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.jar.JarApplicationConfiguration;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DebugVisualGCRunner extends GenericDebuggerRunner {
    private static final Logger log = Logger.getInstance(DebugVisualGCRunner.class.getName());

    @NotNull
    public String getRunnerId() {
        return DebugVisualGCExecutor.EXECUTOR_ID;
    }

    @Override
    public void execute(@NotNull final ExecutionEnvironment environment)
            throws ExecutionException {
        LogHelper.print("#execute", this);

//		boolean b = MyConfigurable.openSettingsIfNotConfigured(environment.getProject());
//		if (!b) {
//			return;
//		}
        super.execute(environment);
    }

    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return executorId.equals(DebugVisualGCExecutor.EXECUTOR_ID) && (profile instanceof ModuleRunProfile || profile instanceof JarApplicationConfiguration)
                && !(profile instanceof RemoteConfiguration);
    }

    @Nullable
    @Override
    protected RunContentDescriptor attachVirtualMachine(RunProfileState state, @NotNull ExecutionEnvironment env,
                                                        RemoteConnection connection, boolean pollConnection) throws ExecutionException {
        RunContentDescriptor runContentDescriptor = super.attachVirtualMachine(state, env, connection, pollConnection);
        LogHelper.print("#attachVirtualMachine", this);
//		RunnerUtils.runVisualVM(this, env, state);
        if (runContentDescriptor == null) {
            return null;
        }
        RunnerLayoutUi runnerLayoutUi = runContentDescriptor.getRunnerLayoutUi();

        // Run in UI thread
        UIUtil.invokeLaterIfNeeded(() -> RunnerUtil.buildVgcRunnerUITab(env, runnerLayoutUi)
        );


//		if (runnerLayoutUi != null) {
//			Content runnerContent = runnerLayoutUi.createContent("beansoft.vgc.1",
//					new JButton("test"), "test", null, null);
//			runnerLayoutUi.addContent(runnerContent);
//			DefaultActionGroup actionGroup = new DefaultActionGroup();
//			actionGroup.add(ActionManager.getInstance().getAction("visualgc.MakeCoffeeAction"));
//			// 添加左侧Action, 会覆盖默认的Debug工具栏
////			runnerLayoutUi.getOptions().setTopLeftToolbar(actionGroup, ActionPlaces.UNKNOWN);
//		}

        ProcessHandler processHandler = runContentDescriptor.getProcessHandler();
        RunnerUtil.startVisualGC(processHandler, env);

        return runContentDescriptor;
    }

}
