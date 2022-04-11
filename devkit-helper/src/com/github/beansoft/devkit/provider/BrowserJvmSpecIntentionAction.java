package com.github.beansoft.devkit.provider;

import com.intellij.codeInsight.intention.AbstractIntentionAction;
import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.codeInsight.intention.PriorityAction;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiFile;
import javax.swing.Icon;

public final class BrowserJvmSpecIntentionAction
extends AbstractIntentionAction
        implements HighPriorityAction, Iconable {
    private final String instructionName;

    public String getText() {
        return "Open oracle doc";
    }

    public boolean isAvailable(Project project, Editor editor, PsiFile psiFile) {
        return true;
    }

    public Icon getIcon(int n10) {
        return AllIcons.Actions.OpenNewTab;
    }

    public void invoke(Project project, Editor editor, PsiFile psiFile) {
        BrowserUtil.browse("https://docs.oracle.com/javase/specs/jvms/se16/html/jvms-6.html#jvms-6.5.aload_n");
    }

    public BrowserJvmSpecIntentionAction(String l10) {
        this.instructionName = l10;
    }
}
