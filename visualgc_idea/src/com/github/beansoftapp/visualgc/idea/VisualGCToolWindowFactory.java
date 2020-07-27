package com.github.beansoftapp.visualgc.idea;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.sun.jvmstat.tools.visualgc.VisualGCPane;
import org.graalvm.visualvm.core.ui.components.DataViewComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Open the tool window.
 * @author beansoft@126.com
 */
public class VisualGCToolWindowFactory implements ToolWindowFactory, DumbAware {

  // Create the tool window content.
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

    VisualGCPaneIdea gcPane = new VisualGCPaneIdea();
    JPanel rootPane = new JPanel();
    rootPane.setLayout(new BorderLayout());
    rootPane.add(gcPane.createComponent(rootPane), BorderLayout.CENTER);

    Content content = contentFactory.createContent(rootPane, "", false);
    content.setIcon(new ImageIcon(VisualGCPane.class.getResource("/visualgc.png")));
    content.setDisposer(new Disposable() {
      @Override
      public void dispose() {
        System.out.println("VisualGC disposed");
        gcPane.dispse();
      }
    });
    toolWindow.getContentManager().addContent(content);
  }

  // Always could be opened, avoid index waiting
  public boolean isApplicable(@NotNull Project project) {
    return true;
  }
}
