/*
    IDEA PsiViewer Plugin
    Copyright (C) 2002 Andrew J. Armstrong

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

	Author:
	Andrew J. Armstrong <andrew_armstrong@bigpond.com>
*/
package idea.plugin.psiviewer.copyable.controller.project;

import com.intellij.icons.AllIcons;
import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.panels.HorizontalLayout;
import idea.plugin.psiviewer.copyable.PsiViewerConstants;
import idea.plugin.psiviewer.copyable.controller.actions.PropertyToggleAction;
import idea.plugin.psiviewer.copyable.util.Helpers;
import idea.plugin.psiviewer.copyable.view.PsiViewerPanel;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class PsiViewerProjectService implements JDOMExternalizable, PsiViewerConstants, Disposable {

    private static final Logger LOG = Logger.getInstance(PsiViewerProjectService.class);
    public boolean HIGHLIGHT = false;
    public boolean FILTER_WHITESPACE = false;
    public boolean SHOW_PROPERTIES = true;
    public int SPLIT_DIVIDER_POSITION = 300;
    public boolean AUTOSCROLL_TO_SOURCE = false;
    public boolean AUTOSCROLL_FROM_SOURCE = false;

    private ComboBox myLanguagesComboBox;
    private final Project myProject;
    private EditorListener myEditorListener;
    private PsiViewerPanel myViewerPanel;
    private final ItemListener myLanguagesComboBoxListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                myViewerPanel.refreshRootElement();
                myViewerPanel.selectElementAtCaret();
            }
        }
    };

    public PsiViewerProjectService(Project project) {
        myProject = project;
    }

    public void registerToolWindow() {
        ToolWindow toolWindow = getToolWindow();
        initToolWindow(toolWindow);
        toolWindow.setAvailable(true, null);
    }

    PsiViewerPanel initToolWindow(@NotNull ToolWindow toolWindow)
    {
        myViewerPanel = new PsiViewerPanel(this);

        myViewerPanel.addPropertyChangeListener("ancestor", it -> handleCurrentState());
        ActionManager actionManager = ActionManager.getInstance();

        DefaultActionGroup actionGroup = new DefaultActionGroup(ID_ACTION_GROUP, false);
        actionGroup.add(new PropertyToggleAction("Filter Whitespace",
                "Remove whitespace elements",
                Helpers.getIcon(ICON_FILTER_WHITESPACE),
                this::isFilterWhitespace,
                this::setFilterWhitespace));
        actionGroup.add(new PropertyToggleAction("Highlight",
                "Highlight selected PSI element",
                Helpers.getIcon(ICON_TOGGLE_HIGHLIGHT),
                this::isHighlighted,
                this::setHighlighted));
        actionGroup.add(new PropertyToggleAction("Properties",
                "Show PSI element properties",
                AllIcons.General.Settings,
                this::isShowProperties,
                this::setShowProperties));
        actionGroup.add(new PropertyToggleAction("Autoscroll to Source",
                "Autoscroll to Source",
                AllIcons.General.AutoscrollToSource,
                this::isAutoScrollToSource,
                this::setAutoScrollToSource));
        actionGroup.add(new PropertyToggleAction("Autoscroll from Source",
                "Autoscroll from Source111",
                AllIcons.General.AutoscrollFromSource,
                this::isAutoScrollFromSource,
                this::setAutoScrollFromSource));

        ActionToolbar toolBar = actionManager.createActionToolbar(ID_ACTION_TOOLBAR, actionGroup, true);

        JPanel panel = new JPanel(new HorizontalLayout(0));
        panel.add(toolBar.getComponent());

        myLanguagesComboBox = new ComboBox();
        panel.add(myLanguagesComboBox);
        updateLanguagesList(Collections.<Language>emptyList());

        myViewerPanel.add(panel, BorderLayout.NORTH);
        myViewerPanel.setToolWindow(toolWindow);

        myEditorListener = new EditorListener(myViewerPanel, myProject);

        return myViewerPanel;
    }

    private void handleCurrentState()
    {
        if (myViewerPanel == null)
            return;

        if (myViewerPanel.isDisplayable()) {
            myEditorListener.start();
            myViewerPanel.selectElementAtCaret();
        } else {
            myEditorListener.stop();
            myViewerPanel.removeHighlighting();
        }
    }

    public void unregisterToolWindow()
    {
        if (myViewerPanel != null) {
            myViewerPanel.removeHighlighting();
            myViewerPanel = null;
        }

        if (myEditorListener != null) {
            myEditorListener.stop();
            myEditorListener = null;
        }
        getToolWindow().setAvailable(false, null);
    }

    private ToolWindow getToolWindow()
    {
        return ToolWindowManager.getInstance(myProject).getToolWindow(ID_TOOL_WINDOW);
    }

    public void readExternal(Element element) throws InvalidDataException
    {
        DefaultJDOMExternalizer.readExternal(this, element);
    }

    public void writeExternal(Element element) throws WriteExternalException
    {
        DefaultJDOMExternalizer.writeExternal(this, element);
    }

    public PsiViewerPanel getViewerPanel()
    {
        return myViewerPanel;
    }

    public boolean isHighlighted()
    {
        return HIGHLIGHT;
    }

    public void setHighlighted(boolean highlight)
    {
        debug("set highlight to " + highlight);
        HIGHLIGHT = highlight;
        myViewerPanel.applyHighlighting();

    }

    public boolean isFilterWhitespace()
    {
        return FILTER_WHITESPACE;
    }

    public void setFilterWhitespace(boolean filterWhitespace)
    {
        FILTER_WHITESPACE = filterWhitespace;
        getViewerPanel().applyWhitespaceFilter();
    }

    public boolean isShowProperties()
    {
        return SHOW_PROPERTIES;
    }

    public void setShowProperties(boolean showProperties)
    {
        SHOW_PROPERTIES = showProperties;
        getViewerPanel().showProperties(showProperties);
    }

    public int getSplitDividerLocation()
    {
        return SPLIT_DIVIDER_POSITION;
    }

    public void setSplitDividerLocation(int location)
    {
        SPLIT_DIVIDER_POSITION = location;
    }

    public boolean isAutoScrollToSource()
    {
        return AUTOSCROLL_TO_SOURCE;
    }

    public void setAutoScrollToSource(boolean isAutoScrollToSource)
    {
        debug("autoscrolltosource=" + isAutoScrollToSource);
        AUTOSCROLL_TO_SOURCE = isAutoScrollToSource;
    }

    public boolean isAutoScrollFromSource()
    {
        return AUTOSCROLL_FROM_SOURCE;
    }

    public void setAutoScrollFromSource(boolean isAutoScrollFromSource)
    {
        debug("autoscrollfromsource=" + isAutoScrollFromSource);
        AUTOSCROLL_FROM_SOURCE = isAutoScrollFromSource;
    }

    public Project getProject()
    {
        return myProject;
    }

    public static PsiViewerProjectService getInstance(Project project) {
        return project.getService(PsiViewerProjectService.class);
    }

    private static void debug(String message)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug(message);
        }
    }

    @Nullable
    public Language getSelectedLanguage()
    {
        return (Language) myLanguagesComboBox.getSelectedItem();
    }

    public void updateLanguagesList(Collection<Language> languages)
    {
        Language selectedLanguage = getSelectedLanguage();

        myLanguagesComboBox.removeItemListener(myLanguagesComboBoxListener);

        //noinspection Since15
        myLanguagesComboBox.setModel(new ListComboBoxModel<>(new ArrayList<>(languages)));

        if (selectedLanguage != null && languages.contains(selectedLanguage))
        {
            myLanguagesComboBox.setSelectedItem(selectedLanguage);
        }

        if (languages.size() < 2) {
            myLanguagesComboBox.setVisible(false);
        } else {
            myLanguagesComboBox.setVisible(true);
        }

        myLanguagesComboBox.addItemListener(myLanguagesComboBoxListener);
    }

    public interface BooleanConsumer {
        void accept(boolean value);
    }

    @Override
    public void dispose() {

    }
}
