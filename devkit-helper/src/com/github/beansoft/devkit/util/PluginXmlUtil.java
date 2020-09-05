package com.github.beansoft.devkit.util;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.html.HtmlDocumentImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;

public class PluginXmlUtil {
    public static boolean isIdeaPluginTagContainingFile(PsiElement element) {
        if (element == null) {
            return false;
        }
        final PsiFile file = element.getContainingFile();
        if (file != null) {
            if (file instanceof XmlFile && !InjectedLanguageManager.getInstance(element.getProject()).isInjectedFragment(file)) {
                final XmlDocument document = PsiTreeUtil.getParentOfType(element, XmlDocument.class, false);
                if (document instanceof HtmlDocumentImpl) {
                    return false;
                }
                if (document == null || document.getRootTag() == null) {
                    return false;
                }

                if ("idea-plugin".equals(document.getRootTag().getLocalName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
