package com.github.beansoft.jvm.runner;

import com.intellij.diagnostic.logging.AdditionalTabComponent;
import com.intellij.openapi.actionSystem.ActionGroup;

import javax.swing.*;

public class AdditionalTabBase extends AdditionalTabComponent {
    public void dispose() {
    }

    public String getTabTitle() {
        return "JProfiler";
    }

    public JComponent getSearchComponent() {
        return null;
    }

    public String getToolbarPlace() {
        return null;
    }

    public JComponent getToolbarContextComponent() {
        return null;
    }

    public boolean isContentBuiltIn() {
        return false;
    }

    public ActionGroup getToolbarActions() {
        return null;
    }

    public JComponent getPreferredFocusableComponent() {
        return null;
    }
}
