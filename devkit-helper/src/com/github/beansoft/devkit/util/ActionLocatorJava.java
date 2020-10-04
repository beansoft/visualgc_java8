package com.github.beansoft.devkit.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.util.ClassUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.SmartList;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.idea.devkit.dom.Action;
import org.jetbrains.idea.devkit.util.ExtensionCandidate;
import org.jetbrains.idea.devkit.util.PluginRelatedLocatorsUtils;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * 查找符合条件的元素并处理. 功能和ActionLocator.kt 里面的相同.
 * @author beansoft
 * Date: 2020-09-28
 */
public class ActionLocatorJava {

  public static List<ExtensionCandidate> locateActionsByXmlRefrence(XmlAttribute xmlAttribute) {
    if (StringUtils.isEmpty(xmlAttribute.getValue())) {
      return emptyList();
    }
    return findActionsByClassName(xmlAttribute.getProject(), xmlAttribute.getValue());
  }

  public static List<ExtensionCandidate> locateActionsByPsiClass(PsiClass psiClass) {
    String className = ClassUtil.getJVMClassName(psiClass);
    if (StringUtils.isEmpty(className)) {
      return emptyList();
    }
    return findActionsByClassName(psiClass.getProject(), className);
  }

  public static boolean processActionDeclarations(String name, Project project, boolean strictMatch,
                                                  PairProcessor<Action, XmlTag> callback) {
    GlobalSearchScope scope = PluginRelatedLocatorsUtils.getCandidatesScope(project);
    return PsiSearchHelper.getInstance(project).processElementsWithWord((element, offsetInElement) -> {
      if (!(element instanceof XmlTag)) {
        return true;
      }

      XmlTag xmlTag = (XmlTag) element;
      PsiElement elementAtOffset = xmlTag.findElementAt(offsetInElement);
      if (strictMatch) {
        if (!elementAtOffset.textMatches(name)) {
          return true;
        }
      } else if (!StringUtil.contains(elementAtOffset.getText(), name)) {
        return true;
      }

      DomElement extension = DomManager.getDomManager(project).getDomElement(xmlTag);
      if (!(extension instanceof Action)) {
        return true;
      }
      return callback.process((Action) extension, xmlTag);
    }, scope, name, UsageSearchContext.ANY, true);
  }

  private static List<ExtensionCandidate> findActionsByClassName(Project project ,  String className) {
    List<ExtensionCandidate> result = Collections.synchronizedList(new SmartList<ExtensionCandidate>());
    SmartPointerManager smartPointerManager =  SmartPointerManager.getInstance(project);
    processActionsByClassName(project, className, (action, tag) -> {
      result.add(new ExtensionCandidate(smartPointerManager.createSmartPsiElementPointer(tag)));
      return true;
    });
    return result;
  }

  private static boolean processActionsByClassName(Project project ,  String className, PairProcessor<Action, XmlTag> callback) {
    return processActionDeclarations(className, project, true, (action, tag) -> {
      if(action != null) {
        return callback.process(action, tag);
      }
      return true;
    });
  }
}
