/*
 * @(#)OptionPane.java 1.01 2008-10-5
 *
 * Copyright 2005-2008 BeanSoft@126.com. All rights reserved.
 * PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package beansoft.swing;

import javax.swing.*;
import java.awt.*;

/**
 * OptionPane, some useful method from javax.swing.JOptionPane with customized icon.
 * @author beansoft
 * @version 1.01 2008-10-5
 */
public class OptionPane {
    // Enable sounds
    static {
        UIManager.put("AuditoryCues.playList",
            UIManager.get("AuditoryCues.allAuditoryCues"));
    }

    /**
     * Display a warning message dialog.
     * @param parentComponent - 父组件
     * @param message - 消息内容
     * @param title - 消息标题
     */
    public static void showWarningMessageDialog(Component parentComponent, Object message, String title) {
        javax.swing.JOptionPane.showMessageDialog(parentComponent, message, title,
            JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Display a error message dialog.
     * @param parentComponent - 父组件
     * @param message - 消息内容
     * @param title - 消息标题
     */
    public static void showErrorMessageDialog(Component parentComponent, Object message, String title) {
        javax.swing.JOptionPane.showMessageDialog(parentComponent, message, title,
            JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display a info message dialog.
     * @param parentComponent - 父组件
     * @param message - 消息内容
     * @param title - 消息标题
     */
    public static void showInfoMessageDialog(Component parentComponent, Object message, String title) {
        javax.swing.JOptionPane.showMessageDialog(parentComponent, message, title,
            JOptionPane.INFORMATION_MESSAGE);
    }
}
