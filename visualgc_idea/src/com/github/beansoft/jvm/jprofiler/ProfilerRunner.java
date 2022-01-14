package com.github.beansoft.jvm.jprofiler;

import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.github.beansoft.jvm.runner.JProfilerTab;
import com.intellij.diagnostic.logging.LogConsoleManagerBase;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationInfoProvider;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.JavaCommandLine;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ModuleRunProfile;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProcessProxy;
import com.intellij.execution.runners.ProcessProxyFactory;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.target.TargetEnvironmentAwareRunProfile;
import com.intellij.execution.target.TargetEnvironmentAwareRunProfileState;
import com.intellij.execution.target.local.LocalTargetEnvironmentRequest;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemJdkUtil;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunnableState;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.jprofiler.integrations.commands.AbstractProfilingCommand;
import com.jprofiler.integrations.idea.C0109e;
import com.jprofiler.integrations.idea.SessionManager;
import com.jprofiler.integrations.idea.runner.RunnerSession;
import com.jprofiler.integrations.p008b.C0069c;
import com.jprofiler.p006b.AbstractC0050a;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import javax.swing.JComponent;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import kotlin.jvm.internal.Ref;
import kotlin.ranges.RangesKt;
import org.jetbrains.concurrency.Promise;

@Metadata(mv = {1, 6, 0}, k = 1, xi = 48, d1 = {"��\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010\u000b\n��\n\u0002\u0010\u000e\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n��\b\u0007\u0018��2\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002J\u0018\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0016J\u001c\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u0016J\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018H\u0016J\u0018\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u0011\u001a\u00020\u0012H\u0002J\u0010\u0010\u001d\u001a\u00020\u00102\u0006\u0010\u001b\u001a\u00020\u001cH\u0002J\u001a\u0010\u001e\u001a\u0004\u0018\u00010\u001f2\u0006\u0010 \u001a\u00020!2\u0006\u0010\u001b\u001a\u00020\u001cH\u0014J \u0010\"\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001f0#2\u0006\u0010 \u001a\u00020$2\u0006\u0010\u001b\u001a\u00020\u001cH\u0014J\u001a\u0010%\u001a\u0004\u0018\u00010\u001f2\u0006\u0010 \u001a\u00020!2\u0006\u0010\u001b\u001a\u00020\u001cH\u0002J(\u0010&\u001a\u0012\u0012\u0006\u0012\u0004\u0018\u00010(\u0012\u0006\u0012\u0004\u0018\u00010\u001a0'2\u0006\u0010 \u001a\u00020!2\u0006\u0010\u001b\u001a\u00020\u001cH\u0002J\u0018\u0010)\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00160*2\u0006\u0010\r\u001a\u00020\u000eH\u0002J\u0018\u0010)\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00160*2\u0006\u0010\u0011\u001a\u00020\u0012H\u0002J\b\u0010+\u001a\u00020\fH\u0016J$\u0010,\u001a\n\u0012\u0004\u0012\u00020\u0012\u0018\u00010-2\b\u0010.\u001a\u0004\u0018\u00010/2\b\u0010\u0013\u001a\u0004\u0018\u000100H\u0016J\u0010\u00101\u001a\u00020\n2\u0006\u00102\u001a\u00020\u001cH\u0002J*\u00103\u001a\u00020\u00102\u0006\u00104\u001a\u00020\u001a2\b\u0010\u0011\u001a\u0004\u0018\u00010\u00122\u0006\u00105\u001a\u00020\u000e2\u0006\u00106\u001a\u00020\nH\u0016J\u0010\u00107\u001a\u00020\u00102\u0006\u00108\u001a\u000209H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0004¢\u0006\u0002\n��R\u000e\u0010\u0005\u001a\u00020\u0006X\u0004¢\u0006\u0002\n��R\u000e\u0010\u0007\u001a\u00020\bX\u0004¢\u0006\u0002\n��¨\u0006:"}, d2 = {"Lcom/jprofiler/integrations/idea/runner/ProfilerRunner;", "Lcom/intellij/execution/impl/DefaultJavaProgramRunner;", "()V", "clientHandler", "Lcom/jprofiler/integrations/idea/runner/ClientHandler;", "localHandler", "Lcom/jprofiler/integrations/idea/runner/LocalHandler;", "remoteHandler", "Lcom/jprofiler/integrations/idea/runner/RemoteHandler;", "canRun", "", "id", "", "profile", "Lcom/intellij/execution/configurations/RunProfile;", "checkConfiguration", "", "settings", "Lcom/intellij/execution/configurations/RunnerSettings;", "config", "Lcom/intellij/execution/configurations/ConfigurationPerRunnerSettings;", "createConfigurationData", "Lcom/jprofiler/integrations/idea/runner/BaseSettings;", "provider", "Lcom/intellij/execution/configurations/ConfigurationInfoProvider;", "createPatchedJavaParameters", "Lcom/intellij/execution/configurations/JavaParameters;", "environment", "Lcom/intellij/execution/runners/ExecutionEnvironment;", "disposeReusedSession", "doExecute", "Lcom/intellij/execution/ui/RunContentDescriptor;", "state", "Lcom/intellij/execution/configurations/RunProfileState;", "doExecuteAsync", "Lorg/jetbrains/concurrency/Promise;", "Lcom/intellij/execution/target/TargetEnvironmentAwareRunProfileState;", "executeImpl", "getExecutionResult", "Lkotlin/Pair;", "Lcom/intellij/execution/ExecutionResult;", "getHandler", "Lcom/jprofiler/integrations/idea/runner/ProfilerHandler;", "getRunnerId", "getSettingsEditor", "Lcom/intellij/openapi/options/SettingsEditor;", "executor", "Lcom/intellij/execution/Executor;", "Lcom/intellij/execution/configurations/RunConfiguration;", "isExecutorSupportedOnTarget", "env", "patch", "javaParameters", "runProfile", "beforeExecution", "requireExecutable", "project", "Lcom/intellij/openapi/project/Project;", "idea"})
public final class ProfilerRunner extends DefaultJavaProgramRunner {

    private final ClientHandler f458a = new ClientHandler();

    private final LocalHandler f459b = new LocalHandler();

    private final RemoteHandler f460c = new RemoteHandler();

    public String getRunnerId() {
        return "JProfiler";
    }

    public boolean canRun(String str, RunProfile runProfile) {
        boolean z;
        Intrinsics.checkNotNullParameter(str, "id");
        Intrinsics.checkNotNullParameter(runProfile, "profile");
        if (Intrinsics.areEqual(str, "JProfiler")) {
            if (runProfile instanceof ModuleRunProfile) {
                z = !(runProfile instanceof RemoteConfiguration);
            } else if (runProfile instanceof ExternalSystemRunConfiguration) {
                z = ApplicationInfo.getInstance().getBuild().getBaselineVersion() >= 172;
            } else {
                z = C0149f.m217a(runProfile);
            }
            if (z) {
                return true;
            }
        }
        return false;
    }

    public BaseSettings createConfigurationData(ConfigurationInfoProvider configurationInfoProvider) {
        Intrinsics.checkNotNullParameter(configurationInfoProvider, "provider");
        return m236a((RunProfile) configurationInfoProvider.getConfiguration()).mo153b();
    }

    public void patch(JavaParameters javaParameters, RunnerSettings runnerSettings, RunProfile runProfile, boolean z) {
        Intrinsics.checkNotNullParameter(javaParameters, "javaParameters");
        Intrinsics.checkNotNullParameter(runProfile, "runProfile");
        patch(javaParameters, runnerSettings, runProfile, z);
        Project project = ((RunConfiguration) runProfile).getProject();
        if (runnerSettings == null) {
            throw new IllegalArgumentException("Required value was null.".toString());
        }
        Intrinsics.checkNotNullExpressionValue(project, "project");
        m231a(project);
        m236a(runProfile).m183a(project, javaParameters, runnerSettings, !C0151g.m209d(runProfile));
    }

    public void checkConfiguration(RunnerSettings runnerSettings, ConfigurationPerRunnerSettings configurationPerRunnerSettings) {
        if (runnerSettings != null) {
            m234a(runnerSettings).m177b(runnerSettings);
        }
    }

    public SettingsEditor<RunnerSettings> getSettingsEditor(Executor executor, RunConfiguration runConfiguration) {
        if (runConfiguration == null) {
            return null;
        }
        ProfilerHandler<? extends BaseSettings> a = m236a((RunProfile) runConfiguration);
        Project project = runConfiguration.getProject();
        Intrinsics.checkNotNullExpressionValue(project, "config.project");
        return a.mo152b(project);
    }

    private final boolean m233a(ExecutionEnvironment executionEnvironment) {
        boolean z;
        Object invoke;
        Object invoke2;
        try {
            Executor executor = executionEnvironment.getExecutor();
            Intrinsics.checkNotNullExpressionValue(executor, "env.executor");
            invoke = executionEnvironment.getClass().getMethod("getTargetEnvironmentRequest", new Class[0]).invoke(executionEnvironment, new Object[0]);
            invoke2 = executor.getClass().getMethod("isSupportedOnTarget", new Class[0]).invoke(executor, new Object[0]);
        } catch (Throwable th) {
            z = true;
        }
        if (!(invoke instanceof LocalTargetEnvironmentRequest)) {
            if (!Intrinsics.areEqual(true, invoke2)) {
                z = false;
                return z;
            }
        }
        z = true;
        return z;
    }

    protected Promise<RunContentDescriptor> doExecuteAsync(TargetEnvironmentAwareRunProfileState targetEnvironmentAwareRunProfileState, ExecutionEnvironment executionEnvironment) {
        boolean z;
        Intrinsics.checkNotNullParameter(targetEnvironmentAwareRunProfileState, "state");
        Intrinsics.checkNotNullParameter(executionEnvironment, "environment");
        Project project = executionEnvironment.getProject();
        Intrinsics.checkNotNullExpressionValue(project, "environment.project");
        m231a(project);
        FileDocumentManager.getInstance().saveAllDocuments();
        try {
            TargetEnvironmentAwareRunProfile runProfile = executionEnvironment.getRunProfile();
            z = Intrinsics.areEqual(false, runProfile.getClass().getMethod("needPrepareTarget", new Class[0]).invoke(runProfile, new Object[0]));
        } catch (Throwable th) {
            z = true;
        }
        if (z || m233a(executionEnvironment)) {
            Promise<RunContentDescriptor> prepareTargetToCommandExecution = targetEnvironmentAwareRunProfileState.prepareTargetToCommandExecution(executionEnvironment, Logger.getInstance(DefaultJavaProgramRunner.class), "Failed to execute java run configuration async", () -> {
                return m228a(r4, r5, r6);
            });
            Intrinsics.checkNotNullExpressionValue(prepareTargetToCommandExecution, "state.prepareTargetToCom…e, environment)\n        }");
            return prepareTargetToCommandExecution;
        }
        throw new ExecutionException(ExecutionBundle.message("run.configuration.action.is.supported.for.local.machine.only", new Object[]{executionEnvironment.getExecutor().getActionName()}));
    }

    private static final RunContentDescriptor m228a(ProfilerRunner profilerRunner, TargetEnvironmentAwareRunProfileState targetEnvironmentAwareRunProfileState, ExecutionEnvironment executionEnvironment) {
        Intrinsics.checkNotNullParameter(profilerRunner, "this$0");
        Intrinsics.checkNotNullParameter(targetEnvironmentAwareRunProfileState, "$state");
        Intrinsics.checkNotNullParameter(executionEnvironment, "$environment");
        return profilerRunner.m235a((RunProfileState) targetEnvironmentAwareRunProfileState, executionEnvironment);
    }

    protected RunContentDescriptor doExecute(RunProfileState runProfileState, ExecutionEnvironment executionEnvironment) {
        Intrinsics.checkNotNullParameter(runProfileState, "state");
        Intrinsics.checkNotNullParameter(executionEnvironment, "environment");
        Project project = executionEnvironment.getProject();
        Intrinsics.checkNotNullExpressionValue(project, "environment.project");
        m231a(project);
        FileDocumentManager.getInstance().saveAllDocuments();
        return m235a(runProfileState, executionEnvironment);
    }

    private final RunContentDescriptor m235a(RunProfileState runProfileState, ExecutionEnvironment executionEnvironment) {
        Pair<ExecutionResult, JavaParameters> parametersPair = null;
        try {
            parametersPair = makePair(runProfileState, executionEnvironment);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        ExecutionResult executionResult = (ExecutionResult) parametersPair.component1();
        JavaParameters javaParameters = (JavaParameters) parametersPair.component2();
        Ref.ObjectRef objectRef = new Ref.ObjectRef();
        Function0 aVar = new DoExcuteFunction(executionEnvironment, executionResult, this, javaParameters, objectRef);
        if (EventQueue.isDispatchThread()) {
            aVar.invoke();
        } else {
            EventQueue.invokeAndWait(() -> {
                m227a(r0);
            });
        }
        return (RunContentDescriptor) objectRef.element;
    }


    public static final class DoExcuteFunction extends Lambda implements Function0<Unit> {

        final ExecutionEnvironment executionEnvironment;

        final ExecutionResult executionResult;

        final ProfilerRunner profilerRunner;

        final JavaParameters javaParameters;

        final Ref.ObjectRef<RunContentDescriptor> descriptorObjectRef;

        DoExcuteFunction(ExecutionEnvironment executionEnvironment, ExecutionResult executionResult, ProfilerRunner profilerRunner, JavaParameters javaParameters, Ref.ObjectRef<RunContentDescriptor> objectRef) {
            super(0);
            this.executionEnvironment = executionEnvironment;
            this.executionResult = executionResult;
            this.profilerRunner = profilerRunner;
            this.javaParameters = javaParameters;
            this.descriptorObjectRef = objectRef;
        }

        public Unit invoke() {
            try {
                doInvoke();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return Unit.INSTANCE;
        }

        public final void doInvoke() throws Exception {
            RunProfile runProfile = this.executionEnvironment.getRunProfile();
            Intrinsics.checkNotNullExpressionValue(runProfile, "environment.runProfile");
            RunnerSettings runnerSettings = this.executionEnvironment.getRunnerSettings();
            BaseSettings aVar = runnerSettings instanceof BaseSettings ? runnerSettings : null;
            if (this.executionResult != null && aVar != null) {
                C0069c.m619d().invoke(C01471.f466a);
                ProfilerHandler a = this.profilerRunner.m236a(runProfile);
                Project project = this.executionEnvironment.getProject();
                Intrinsics.checkNotNullExpressionValue(project, "environment.project");
                RunProfileState state = this.executionEnvironment.getState();
                AbstractProfilingCommand a2 = a.m181a(project, runProfile, aVar, state instanceof JavaCommandLine ? (JavaCommandLine) state : null, this.javaParameters);
                RunContentBuilder runContentBuilder = new RunContentBuilder(this.executionResult, this.executionEnvironment);
                this.profilerRunner.updateUI(this.executionEnvironment);
                RunContentDescriptor showRunContent = runContentBuilder.showRunContent(this.executionEnvironment.getContentToReuse());
                Project project2 = this.executionEnvironment.getProject();
                Intrinsics.checkNotNullExpressionValue(project2, "environment.project");
                ProcessHandler processHandler = this.executionResult.getProcessHandler();
                Intrinsics.checkNotNullExpressionValue(processHandler, "result.processHandler");
                LogConsoleManagerBase logConsoleManager = runContentBuilder.getLogConsoleManager();
                Intrinsics.checkNotNullExpressionValue(logConsoleManager, "runContentBuilder.logConsoleManager");
                RunnerLayoutUi runnerLayoutUi = showRunContent.getRunnerLayoutUi();
                Intrinsics.checkNotNull(runnerLayoutUi);
                Intrinsics.checkNotNullExpressionValue(runnerLayoutUi, "runContentDescriptor.runnerLayoutUi!!");
                if (SessionManager.f287a.m554a().m568a(a2, new RunnerSession(project2, processHandler, logConsoleManager, runnerLayoutUi))) {
                    this.descriptorObjectRef.element = showRunContent;
                }
            }
        }
    }

    private static final void m227a(Function0 function0) {
        Intrinsics.checkNotNullParameter(function0, "$tmp0");
        function0.invoke();
    }

    public final void updateUI(ExecutionEnvironment executionEnvironment) {
        ContentManager contentManager;
        RunContentDescriptor contentToReuse = executionEnvironment.getContentToReuse();
        if (contentToReuse != null) {
            RunnerLayoutUi runnerLayoutUi = contentToReuse.getRunnerLayoutUi();
            if (!(runnerLayoutUi == null || (contentManager = runnerLayoutUi.getContentManager()) == null)) {
                Content[] contents = contentManager.getContents();
                Intrinsics.checkNotNullExpressionValue(contents, "this.contents");
                Content[] contentArr = contents;
                ArrayList arrayList = new ArrayList();
                for (Content content : contentArr) {
                    if (RunnerSession.JProfilerTab.class.isAssignableFrom(content.getComponent().getClass())) {
                        arrayList.add(content);
                    }
                }
                ArrayList arrayList2 = arrayList;
                LinkedHashMap linkedHashMap = new LinkedHashMap(RangesKt.coerceAtLeast(MapsKt.mapCapacity(CollectionsKt.collectionSizeOrDefault(arrayList2, 10)), 16));
                for (Object obj : arrayList2) {
                    JComponent component = ((Content) obj).getComponent();
                    if (component == null) {
                        throw new NullPointerException("null cannot be cast to non-null type com.jprofiler.integrations.idea.runner.RunnerSession.JProfilerTab");
                    }
                    linkedHashMap.put((JProfilerTab) component, obj);
                }
                Set keySet = linkedHashMap.keySet();
                if (keySet != null) {
                    JProfilerTab bVar = (JProfilerTab) CollectionsKt.firstOrNull(keySet);
                    if (bVar != null) {
                        bVar.dispose();
                    }
                }
            }
        }
    }

    private final Pair<ExecutionResult, JavaParameters> makePair(RunProfileState runProfileState, ExecutionEnvironment executionEnvironment) throws ExecutionException {
        ExecutionResult executionResult;
        RunnerSettings runnerSettings = executionEnvironment.getRunnerSettings();
        if (runnerSettings == null) {
            return TuplesKt.to(runProfileState.execute(executionEnvironment.getExecutor(), (ProgramRunner) this), (JavaParameters) null);
        }
        if (runProfileState instanceof JavaCommandLine) {
            if (C0149f.m215a(runProfileState)) {
                JavaParameters a = m232a(executionEnvironment, runnerSettings);
                RunProfile runProfile = executionEnvironment.getRunProfile();
                Intrinsics.checkNotNullExpressionValue(runProfile, "environment.runProfile");
                ParametersList vMParametersList = a.getVMParametersList();
                Intrinsics.checkNotNullExpressionValue(vMParametersList, "javaParameters.vmParametersList");
                C0149f.m216a(runProfile, (JavaCommandLine) runProfileState, vMParametersList);
                return TuplesKt.to(runProfileState.execute(executionEnvironment.getExecutor(), (ProgramRunner) this), a);
            }
            JavaParameters javaParameters = ((JavaCommandLine) runProfileState).getJavaParameters();
            Intrinsics.checkNotNullExpressionValue(javaParameters, "state.javaParameters");
            RunProfile runProfile2 = executionEnvironment.getRunProfile();
            Intrinsics.checkNotNullExpressionValue(runProfile2, "environment.runProfile");
            patch(javaParameters, runnerSettings, runProfile2, true);
            ExecutionResult execute = runProfileState.execute(executionEnvironment.getExecutor(), (ProgramRunner) this);
            if (execute == null) {
                executionResult = null;
            } else {
                ProcessProxy createCommandLineProxy = ProcessProxyFactory.getInstance().createCommandLineProxy((JavaCommandLine) runProfileState);
                if (createCommandLineProxy != null) {
                    createCommandLineProxy.attach(execute.getProcessHandler());
                }
                executionResult = execute;
            }
            return TuplesKt.to(executionResult, ((JavaCommandLine) runProfileState).getJavaParameters());
        } else if (!(runProfileState instanceof ExternalSystemRunnableState)) {
            return TuplesKt.to(runProfileState.execute(executionEnvironment.getExecutor(), (ProgramRunner) this), (Object) null);
        } else {
            JavaParameters a2 = m232a(executionEnvironment, runnerSettings);
            executionEnvironment.putUserData(ExternalSystemTaskExecutionSettings.JVM_AGENT_SETUP_KEY, a2.getVMParametersList());
            return TuplesKt.to(runProfileState.execute(executionEnvironment.getExecutor(), (ProgramRunner) this), a2);
        }
    }

    private final JavaParameters m232a(ExecutionEnvironment executionEnvironment, RunnerSettings runnerSettings) {
        JavaParameters javaParameters = new JavaParameters();
        javaParameters.setJdk(ExternalSystemJdkUtil.getJdk(executionEnvironment.getProject(), "#USE_PROJECT_JDK"));
        RunProfile runProfile = executionEnvironment.getRunProfile();
        Intrinsics.checkNotNullExpressionValue(runProfile, "environment.runProfile");
        patch(javaParameters, runnerSettings, runProfile, true);
        return javaParameters;
    }

    public final ProfilerHandler<? extends BaseSettings> m236a(RunProfile runProfile) {
        return C0151g.m211b(runProfile) ? this.f460c : C0151g.m212a(runProfile) ? this.f459b : this.f458a;
    }

    private final ProfilerHandler<? extends BaseSettings> m234a(RunnerSettings runnerSettings) {
        return this.f460c.mo163a(runnerSettings) ? this.f460c : this.f459b.mo163a(runnerSettings) ? this.f459b : this.f458a;
    }

    private final void m231a(Project project) {
        if (!C0109e.m465a(project)) {
            throw new ExecutionException("JProfiler plugin is not configured properly");
        }
    }
}