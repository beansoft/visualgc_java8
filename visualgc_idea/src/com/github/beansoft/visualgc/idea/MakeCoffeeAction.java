package com.github.beansoft.visualgc.idea;

import com.beansoft.lic.CheckLicense;
import com.intellij.CommonBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MakeCoffeeAction extends AnAction {
    public static final String TITLE = "VisualGC";

    public MakeCoffeeAction() {
        super();
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        final Boolean isLicensed = CheckLicense.isLicensed();
        if (Boolean.TRUE.equals(isLicensed)) {
            final String message = "You have made a donation to VisualGC.\n" +
                    "Thank you and this will made the plugin better!";
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), message, TITLE, JOptionPane.INFORMATION_MESSAGE);
        } else {
            final String message = "Unfortunately, you have not obtain the license yet. However you can still use this plugin for free with most functions.\n" +
                    "Would you like to register the plugin to make a donation?";
//          JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), message, TITLE, JOptionPane.INFORMATION_MESSAGE);
            boolean sureReg = Messages.showYesNoDialog(e.getProject(), message, TITLE, CommonBundle.getContinueButtonText(), CommonBundle.getCancelButtonText(),
                    Messages.getQuestionIcon()) ==
                    Messages.YES;
            if (sureReg) {
                CheckLicense.requestLicense("Please consider register our plugin to make a donation!");
            } else {
                Messages.showInfoMessage("Unfortunately, you have not obtain the license yet. \n However you can still use this plugin for free with most functions.", TITLE);
            }
        }
    }

    // Always available and enabled
    @Override
    public boolean isDumbAware() {
        return true;
    }

}
