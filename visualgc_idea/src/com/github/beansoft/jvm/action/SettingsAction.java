package com.github.beansoft.jvm.action;

import com.github.beansoft.idea.BaseTooltipAction;
import com.github.beansoft.jvm.MyConfigurable;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.RevealFileAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class SettingsAction extends BaseTooltipAction {
    public SettingsAction() {
        super("Open VisualGC Setting", "Modify VisualGC to monitor server integration options.", AllIcons.General.Settings);
    }

    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ShowSettingsUtil.getInstance().editConfigurable(e.getProject(), new MyConfigurable());
    }

}
