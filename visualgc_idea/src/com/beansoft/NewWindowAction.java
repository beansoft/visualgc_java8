package com.beansoft;

import com.intellij.execution.services.ServiceViewContributor;
import com.intellij.execution.services.ServiceViewToolWindowFactory;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.AutoScrollToSourceHandler;
import com.intellij.ui.content.impl.ContentImpl;
import com.intellij.util.containers.SmartHashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Set;

public class NewWindowAction extends AnAction {
  @NonNls
  private static final String HELP_ID = "beansoft.tool.window";
  private boolean myActivationActionsRegistered;
  private Project myProject;
  private final Set<String> myActiveToolWindowIds = new SmartHashSet<>();
  private boolean myRegisteringToolWindowAvailable;

  @Override
  public void actionPerformed(AnActionEvent e) {
    myProject = e.getProject();
    registerToolWindow(HELP_ID, true);
  }


  private void registerToolWindow(@NotNull String toolWindowId, boolean active) {
    if (myProject.isDefault()) {
      return;
    }

    ToolWindow toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow(toolWindowId);
    if (toolWindow != null) {
      return;
    }

    ApplicationManager.getApplication().invokeLater(() -> {
      if (!myActivationActionsRegistered) {
        myActivationActionsRegistered = true;
      }

      myRegisteringToolWindowAvailable = active;
      try {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
        ToolWindow toolWindowFound = toolWindowManager.registerToolWindow(RegisterToolWindowTask.lazyAndClosable(toolWindowId, new ServiceViewToolWindowFactory(), AllIcons.Toolwindows.ToolWindowServices));
        if (active) {
          myActiveToolWindowIds.add(toolWindowId);
          toolWindowFound.setShowStripeButton(true);
          toolWindowFound.setTitle("BeanSoft");
          toolWindowFound.getContentManager().addContent(new ContentImpl(new JButton("Bingo"), "test", true));
          toolWindowFound.show();
        }
        else {
          toolWindowFound.setShowStripeButton(false);
        }
      }
      finally {
        myRegisteringToolWindowAvailable = false;
      }
    }, ModalityState.NON_MODAL, myProject.getDisposed());
  }

  private void updateToolWindow(@NotNull String toolWindowId, boolean active, boolean show) {
    if (myProject.isDisposed() || myProject.isDefault()) {
      return;
    }

    ApplicationManager.getApplication().invokeLater(() -> {
      ToolWindow toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow(toolWindowId);
      if (toolWindow == null) {
        return;
      }

      if (active) {
        boolean doShow = show && !myActiveToolWindowIds.contains(toolWindowId) && !toolWindow.isShowStripeButton();
        myActiveToolWindowIds.add(toolWindowId);
        if (doShow) {
          toolWindow.show();
        }
      }
      else if (myActiveToolWindowIds.remove(toolWindowId)) {
        // Hide tool window only if model roots became empty and there were some services shown before update.
        toolWindow.hide();
        toolWindow.setShowStripeButton(false);
      }
    }, ModalityState.NON_MODAL, myProject.getDisposed());
  }
}
