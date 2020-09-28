package com.github.beansoft.devkit.provider;

import com.github.beansoft.devkit.util.ActionLocatorJava;
import com.github.beansoft.devkit.util.ActionLocatorKt;
import com.github.beansoft.devkit.util.JavaUtils;
import com.github.beansoft.devkit.util.PluginXmlUtil;
import com.google.common.collect.ImmutableSet;
import com.intellij.codeInsight.daemon.impl.GutterTooltipHelper;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.pom.Navigatable;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.devkit.util.ExtensionCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Xml Ref 跳转到 Action 引用定义.
 * from <reference ref="View PSI element at caret - devkit"/>
 * to
 * <Action id="View PSI element at caret - devkit" />
 * TODO 性能优化
 * 2020.09.20
 * @author beansoft@126.com
 */
public class PluginXmlRefLineMarkerProvider extends SimpleLineMarkerProvider<XmlAttribute, PsiElement> {

  private static final Logger logger = LoggerFactory.getLogger(SimpleLineMarkerProvider.class);

  private static final ImmutableSet<String> ATTRIBUTE_NAMES = ImmutableSet.of(
      "ref"
  );

  @Override
  public boolean isTheElement(@NotNull PsiElement element) {
    return element instanceof XmlAttribute
        && isTargetType(((XmlAttribute) element))
        && PluginXmlUtil.isIdeaPluginTagContainingFile(element);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Optional<? extends PsiElement> apply(@NotNull XmlAttribute from) {
    String attributeName = findLegalTargetName(from);
    if (attributeName == null) {
      return Optional.empty();
    }
    String implementation = from.getValue();
    if (implementation == null) {
      return Optional.empty();
    }

//    List<ExtensionCandidate> targets = ActionLocatorKt.locateActionsByXmlRefrence(from);
    List<ExtensionCandidate> targets = ActionLocatorJava.locateActionsByXmlRefrence(from);
    if (targets.isEmpty()) {
      return Optional.empty();
    }
    XmlTag tag = targets.get(0).pointer.dereference();

    if (tag != null) {
      if (tag.isValid()) {
        logger.info(" 查找 xml action ref结束, {}", tag);
        return Optional.of(tag);
      }
    }

    return Optional.empty();
  }

  private boolean isTargetType(@NotNull XmlAttribute xmlTag) {
    return findLegalTargetName(xmlTag) != null;
  }

  private String findLegalTargetName(@NotNull XmlAttribute xmlTag) {
    String name = xmlTag.getName().toLowerCase();
    for (String legalName :
        ATTRIBUTE_NAMES) {
      if (name.equals(legalName)) {
        return name;
      }
    }
//        return TARGET_TYPES.contains(name);
    return null;
  }

  @NotNull
  @Override
  public Navigatable getNavigatable(@NotNull XmlAttribute from, @NotNull PsiElement target) {
    return (Navigatable) target.getNavigationElement();
  }

  @NotNull
  @Override
  public String getTooltip(@NotNull XmlAttribute from, @NotNull PsiElement target) {
    return GutterTooltipHelper.getTooltipText(ImmutableSet.of(target), "Open", false,
        IdeActions.ACTION_GOTO_IMPLEMENTATION);
  }

  @NotNull
  @Override
  public Icon getIcon() {
    return AllIcons.Gutter.WriteAccess;
  }

}
