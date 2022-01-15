package com.github.beansoft.jvm.runner;

import com.github.beansoft.visualgc.idea.VisualGCPaneIdea;
import com.sun.jvmstat.tools.visualgc.VisualGCPane;

import javax.swing.*;
import java.awt.*;

public class RunnerUtil {
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
