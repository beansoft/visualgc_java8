package com.github.beansoft.idea;

import com.intellij.ide.HelpTooltip;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Base action provide a popup tooltip.
 */
public abstract class BaseTooltipAction extends AnAction implements CustomComponentAction {

    /**
     * 使用此属性, 解决当 tooltip 文案发生变化时可以更新显示内容的功能.
     */
    private String myLastLinkText = "";

    protected Project currentProject;

    public BaseTooltipAction() {}

    public BaseTooltipAction(Icon icon) {
        super(icon);
    }

    public BaseTooltipAction(@Nullable String text) {
        super(text);
    }

    public BaseTooltipAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    // Always available and enabled
    @Override
    public boolean isDumbAware() {
        return true;
    }

    /**
     * Tooltip 中的链接文字. 不能为空, 否则 2021.1 EAP 会报错 ActionLink 为空.
     * @return 链接文字
     */
    @NotNull public String linkText() {
        return "";
    }

    /**
     * Tooltip 中的链接文字点击后的动作.
     * @return Runnable
     */
    public Runnable linkAction() {
        return () -> {};
    }


    /**
     * 快捷键文字
     * @return
     */
    @Nullable
    private String getShortcut() {
//                if (myIsDoubleCtrlRegistered) {
//                    return IdeBundle.message("double.ctrl.or.shift.shortcut",
//                            SystemInfo.isMac ? FontUtil.thinSpace() + MacKeymapUtil.CONTROL : "Ctrl"); //NON-NLS
//                }
        //keymap shortcut is added automatically
        return KeymapUtil.getFirstKeyboardShortcutText(this);
    }

    @NotNull
    @Override
    public JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        return new ActionButton(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE) {
            @Override
            protected void updateToolTipText() {
                HelpTooltip.dispose(this);

                try {
                    HelpTooltip helpTooltip = new HelpTooltip()
                            .setTitle(presentation.getText())
                            .setShortcut(getShortcut())
                            .setDescription(presentation.getDescription());

                    myLastLinkText = linkText();
                    if(!StringUtil.isEmpty(myLastLinkText)) {
                        helpTooltip.setLink(myLastLinkText, linkAction());
                    }

                    helpTooltip.installOn(this);
                } catch (Exception e) {
                    /**
                     * try to fix the WebStorm 2011 startup sometimes NPE
                     java.lang.NullPointerException
                     at com.intellij.ide.HelpTooltip.deriveDescriptionFont(HelpTooltip.java:577)
                     at com.intellij.ide.HelpTooltip.createTipPanel(HelpTooltip.java:435)
                     at com.intellij.ide.HelpTooltip.initPopupBuilder(HelpTooltip.java:372)
                     at com.intellij.ide.HelpTooltip.installImpl(HelpTooltip.java:339)
                     at com.intellij.ide.HelpTooltip.installOn(HelpTooltip.java:327)
                     at com.github.beansoftapp.reatnative.idea.actions.BaseRNConsoleAction$1.updateToolTipText(BaseRNConsoleAction.java:100)
                     */
                    e.printStackTrace();
                }
            }


            @Override
            public void setToolTipText(String s) {
                String shortcutText = getShortcutText();
                super.setToolTipText(StringUtil.isNotEmpty(shortcutText) ? (s + " (" + shortcutText + ")") : s);
            }
        };
    }

    private static final String TOOLTIP_PROPERTY = "JComponent.helpTooltip";

    /**
     * 当 linkText() 发生变化时, 重新创建提示信息.
     * @param e
     */
    public void update(@NotNull final AnActionEvent e) {
        currentProject = e.getProject();

        super.update(e);
        JComponent button = e.getPresentation().getClientProperty(CustomComponentAction.COMPONENT_KEY);
        if (button != null) {
            Object property = button.getClientProperty(TOOLTIP_PROPERTY);
            if (property instanceof HelpTooltip) {
                HelpTooltip helpTooltip = (HelpTooltip)property;

                if(!StringUtil.isEmpty(linkText()) && !linkText().equals(myLastLinkText)) {
                    myLastLinkText = linkText();
                    helpTooltip.setLink(myLastLinkText, linkAction());

                    HelpTooltip.dispose(button);
                    try {
                        helpTooltip.installOn(button);
                    } catch (Exception exception) {
                        /**
                         * try to fix the WebStorm 2011 startup sometimes NPE
                         java.lang.NullPointerException
                         at com.intellij.ide.HelpTooltip.deriveDescriptionFont(HelpTooltip.java:577)
                         at com.intellij.ide.HelpTooltip.createTipPanel(HelpTooltip.java:435)
                         at com.intellij.ide.HelpTooltip.initPopupBuilder(HelpTooltip.java:372)
                         at com.intellij.ide.HelpTooltip.installImpl(HelpTooltip.java:339)
                         at com.intellij.ide.HelpTooltip.installOn(HelpTooltip.java:327)
                         at com.github.beansoftapp.reatnative.idea.actions.BaseRNConsoleAction$1.updateToolTipText(BaseRNConsoleAction.java:100)
                         */
                        exception.printStackTrace();
                    }
                }
            }
        }

    }

    @Nullable
    protected Editor getEditor(@NotNull AnActionEvent e) {
        return e.getData(CommonDataKeys.EDITOR);
    }
}
