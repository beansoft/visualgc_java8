package com.github.beansoft.devkit.helper;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageUtil;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.xhtml.XHTMLLanguage;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.source.html.HtmlDocumentImpl;
import com.intellij.psi.impl.source.html.HtmlScriptInjectionBlockerExtension;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.util.HtmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;


/**
 * A simple injector to let you view HTML syntax highlight when edit HTML in plugin.xml files.
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

  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement host) {
    if (!host.isValid() || !(host instanceof XmlText) || !isIdeaPluginTagContainingFile(host)) {
      return;
    }
    XmlTag xmlTag = ((XmlText)host).getParentTag();

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
      registrar.startInjecting(language);
      for (PsiElement child : elements) {
        registrar.addPlace(null, null, (PsiLanguageInjectionHost)host, child.getTextRangeInParent());
      }
      registrar.doneInjecting();
    }
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Collections.singletonList(XmlText.class);
  }
}
