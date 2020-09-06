package com.github.beansoft.devkit.helper;

import com.github.beansoft.devkit.util.PluginXmlUtil;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageUtil;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.html.HtmlScriptInjectionBlockerExtension;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.xml.*;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;


/**
 * A simple injector to let you view HTML syntax highlight when edit HTML in plugin.xml files.
 * CDATA operations see: com.intellij.codeInsight.daemon.impl.analysis.CDataToTextIntention
 *
 * @see com.intellij.psi.impl.source.html.HtmlScriptLanguageInjector
 */
public class PluginHtmlInjector implements MultiHostInjector {


    /**
     * Finds language to be injected into html tag
     *
     * @param xmlTag &lt;html&gt; tag
     * @return language to inject or null if no language found or not a html tag at all
     */
    @Nullable
    public static Language getHtmlLanguageToInject(@NotNull XmlTag xmlTag) {
        if (!isDescTag(xmlTag)) {
            return null;
        }
        return HTMLLanguage.INSTANCE;
    }

    public static boolean isDescTag(@Nullable XmlTag tag) {
        return tag != null && (tag.getLocalName().equalsIgnoreCase("description")
                || tag.getLocalName().equalsIgnoreCase("change-notes"));
    }

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement host) {
        if (!host.isValid() || !(host instanceof XmlText) || !PluginXmlUtil.isIdeaPluginTagContainingFile(host)) {
            return;
        }
        XmlTag xmlTag = ((XmlText) host).getParentTag();

        if (xmlTag == null) {
            return;
        }
        final Language language = getHtmlLanguageToInject(xmlTag);

        if (language == null || HtmlScriptInjectionBlockerExtension.isInjectionBlocked(xmlTag, language)) {
            return;
        }

        if (LanguageUtil.isInjectableLanguage(language)) {
            List<PsiElement> elements = ContainerUtil.filter(host.getChildren(), (child) -> !(child instanceof OuterLanguageElement));
            if (elements.isEmpty()) return;

            for (PsiElement child : elements) {
//                System.out.println(".getNode().getElementType()=" + child.getNode().getElementType());
                if (child.getNode().getElementType() == XmlElementType.XML_CDATA) {
                    PsiElement[] cdataChildren = child.getChildren();
                  for (PsiElement element : cdataChildren) {
                      registrar.startInjecting(language);
                      // Must only applys to the content only, not the <![CDATA[ or ]]> tag.
                      if (element.getNode().getElementType() == XmlElementType.XML_DATA_CHARACTERS) {
                          registrar.addPlace(null, null, (PsiLanguageInjectionHost) host, element.getTextRangeInParent());
                          registrar.doneInjecting();
                      }
                  }
                }

            }

        }
    }

    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Collections.singletonList(XmlText.class);
    }
}
