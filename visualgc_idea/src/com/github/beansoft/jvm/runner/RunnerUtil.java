package com.github.beansoft.jvm.runner;

import com.github.beansoft.jvm.LogHelper;
import com.github.beansoft.visualgc.idea.VisualGCPaneIdea;
import com.intellij.execution.process.BaseProcessHandler;
import com.intellij.execution.process.OSProcessUtil;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ui.UIUtil;
import com.sun.jvmstat.tools.visualgc.VisualGCPane;

import javax.swing.*;
import java.awt.*;

public class RunnerUtil {
    private static final Logger log = Logger.getInstance(RunnerUtil.class.getName());

    public static void startVisualGC(ProcessHandler processHandler) {
        if(processHandler instanceof BaseProcessHandler) {
            int pid = OSProcessUtil.getProcessID(((BaseProcessHandler<?>)processHandler).getProcess());
            System.out.println("Open visualGC pid = " + pid);// TODO
            if(pid > 0) {
                new Thread() {
                    @Override
                    public void run() {
                        LogHelper.print("#Thread run", this);
                        try {
                            UIUtil.invokeLaterIfNeeded( ( ) ->
                                    RunnerUtil.startVisualGC(pid)
                            );
                        } catch (Throwable e) {
                            log.error(e);
                        }
                    }
                }.start();

            }
        }
    }

    public static void startVisualGC(int processId) {
        JFrame frame = new JFrame();
        frame.setIconImage(new ImageIcon(VisualGCPane.class.getResource("/visualgc.png")).getImage());
        frame.setTitle("VisualGC 3.0");
        VisualGCPaneIdea gcPane = new VisualGCPaneIdea();
        frame.getContentPane().add(gcPane.createComponent(frame.getContentPane()), BorderLayout.CENTER);
        frame.setSize(1024, 768);
        frame.setVisible(true);
        gcPane.monitorProcessAndRefreshPane(processId);
    }
}
