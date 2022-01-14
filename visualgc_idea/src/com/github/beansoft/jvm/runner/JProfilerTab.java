package com.github.beansoft.jvm.runner;

import com.github.beansoft.jvm.runner.AdditionalTabBase;
import kotlin.jvm.internal.Intrinsics;

import javax.swing.*;
import java.awt.*;

public final class JProfilerTab extends AdditionalTabBase {

    /* renamed from: a */
    private final Component content;

    public JProfilerTab(Component content) {
        this.content = content;
        setLayout((LayoutManager) new BorderLayout());
        add((Component) this.content, "Center");
    }

    public String getTabTitle() {
        return "VisualGC";
    }


//    /* renamed from: a */
//    public final FrontendPanel m139a() {
//        return this.f501a;
//    }

    @Override // com.jprofiler.integrations.idea.runner.RunnerSession.AdditionalTabBase
    public void dispose() {
//        this.f501a.dispose();
    }
}
