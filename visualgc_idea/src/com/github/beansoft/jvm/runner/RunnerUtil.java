package com.github.beansoft.jvm.runner;

import com.beansoft.lic.CheckLicense;
import com.github.beansoft.jvm.LogHelper;
import com.github.beansoft.visualgc.idea.VisualGCPaneIdea;
import com.intellij.execution.filters.BrowserHyperlinkInfo;
import com.intellij.execution.filters.HyperlinkInfoBase;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.content.Content;
import com.intellij.util.ui.UIUtil;
import com.sun.jvmstat.tools.visualgc.VisualGCPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class RunnerUtil {
    private static final Logger log = Logger.getInstance(RunnerUtil.class.getName());

    public static void buildVgcRunnerUITab(@NotNull ExecutionEnvironment env, RunnerLayoutUi runnerLayoutUi) {
        if (runnerLayoutUi != null) {
            final Boolean isLicensed = CheckLicense.isLicensed();
            if (Boolean.FALSE.equals(isLicensed)) {
                ConsoleViewImpl consoleView = new ConsoleViewImpl(env.getProject(), true);

                DefaultActionGroup toolbarActions = new DefaultActionGroup();

                // 给控制台定制额外的按钮
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(consoleView.getComponent(), "Center");
                ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("unknown", toolbarActions, false);
                toolbar.setTargetComponent(consoleView.getComponent());
                panel.add(toolbar.getComponent(), "West");
                Content runnerContent = runnerLayoutUi.createContent("beansoft.vgc.help",
                        panel, "Reg VisualGC", null, null);
                runnerLayoutUi.addContent(runnerContent);
                DefaultActionGroup actionGroup = new DefaultActionGroup();
                actionGroup.add(ActionManager.getInstance().getAction("visualgc.MakeCoffeeAction"));

                consoleView.print(
                        "Unfortunately, you have not obtain the license yet or start a 30 days free trial." +
                                "\nHowever you can still use this plugin for free with other functions in the VisualGC tool window.\n",
                        ConsoleViewContentType.LOG_WARNING_OUTPUT);

                consoleView.print("For students, teachers and open source developers, it's free.\n", ConsoleViewContentType.LOG_INFO_OUTPUT);

//                consoleView.printHyperlink("https://github.com/beansoftapp/react-native-console/issues",
//                        new BrowserHyperlinkInfo("https://github.com/beansoftapp/react-native-console/issues"));

                consoleView.printHyperlink(
                        "Click to get a license or start a 30 days free trial for VisualGC to unlock this feature.",
                        new HyperlinkInfoBase() {
                            @Override
                            public void navigate(@NotNull Project project, @Nullable RelativePoint relativePoint) {
                                CheckLicense.requestLicense("Please consider register our plugin to make a donation!");
                            }
                        });
                // 添加左侧Action
//			runnerLayoutUi.getOptions().setTopLeftToolbar(actionGroup, ActionPlaces.UNKNOWN);
            }

        }
    }

    public static void startVisualGC(ProcessHandler processHandler, @NotNull ExecutionEnvironment env) {
        if (processHandler instanceof BaseProcessHandler) {
            int pid = OSProcessUtil.getProcessID(((BaseProcessHandler<?>) processHandler).getProcess());

            System.out.println("Open visualGC pid = " + pid);// TODO
            if (pid > 0) {
                // 延迟启动, 否则debug模式下面立即显示窗口会拿不到进程的JVM信息
                new Thread() {
                    @Override
                    public void run() {
                        LogHelper.print("#Thread run", this);
                        try {
                            UIUtil.invokeLaterIfNeeded(() -> {
//                                final Boolean isLicensed = CheckLicense.isLicensed();
                                        final Boolean isLicensed = true;
                                if (Boolean.TRUE.equals(isLicensed)) {
                                    RunnerUtil.startVisualGC(pid, env.getRunProfile().getName());
                                    processHandler.notifyTextAvailable(
                                            "Starting VisualGC in a new window to monitor this process ...", ProcessOutputType.STDOUT);
                                } else {
                                    processHandler.notifyTextAvailable("[VisualGC] Unfortunately, you have not obtain the license yet, so you can't directly view VisualGC with this process. However you can still use this plugin for free with other functions in the VisualGC tool window.", ProcessOutputType.STDERR);
                                }

                                    }

                            );
                        } catch (Throwable e) {
                            log.error(e);
                        }
                    }
                }.start();
            }
        }
    }

    public static void startVisualGC(int processId, String name) {
        JFrame frame = new JFrame();
        frame.setIconImage(new ImageIcon(VisualGCPane.class.getResource("/visualgc.png")).getImage());
        frame.setTitle("VisualGC IDEA - " + processId + " " + name);
        VisualGCPaneIdea gcPane = new VisualGCPaneIdea();
        frame.getContentPane().add(gcPane.createComponent(frame.getContentPane()), BorderLayout.CENTER);
        frame.setSize(1024, 768);
        frame.setVisible(true);
        gcPane.monitorProcessAndRefreshPane(processId, name);
    }
}
