package com.github.beansoft.jvm.runner;

import com.intellij.diagnostic.logging.AdditionalTabComponent;
import com.intellij.execution.configurations.AdditionalTabComponentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MyAdditionalTabComponentManager extends JPanel implements AdditionalTabComponentManager {

    public MyAdditionalTabComponentManager() {
        add(new JButton("test"));
    }

    @Override
    public void addAdditionalTabComponent(@NotNull AdditionalTabComponent component, @NotNull String id) {

    }

    @Override
    public void removeAdditionalTabComponent(@NotNull AdditionalTabComponent component) {

    }
}
