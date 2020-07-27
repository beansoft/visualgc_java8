package com.github.beansoftapp.visualgc.idea;

import com.sun.jvmstat.tools.visualgc.VisualGCPane;
import org.graalvm.visualvm.core.ui.components.DataViewComponent;

import java.awt.*;

public class VisualGCPaneIdea extends VisualGCPane {
    static {
      VisualGCPane.customizeColors();
    }
    public DataViewComponent createComponent(final Container container) {
      return super.createComponent(container);
    }
  }
