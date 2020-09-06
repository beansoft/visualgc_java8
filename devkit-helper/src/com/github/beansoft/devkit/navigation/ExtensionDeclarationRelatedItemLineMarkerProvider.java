// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.beansoft.devkit.navigation;

import com.github.beansoft.devkit.DevkitHelperBundle;
import com.github.beansoft.devkit.util.ActionLocatorKt;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import icons.DevkitIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.devkit.util.ExtensionCandidate;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
// UAST, for Java and Kotlin is all OK
public final class ExtensionDeclarationRelatedItemLineMarkerProvider extends DevkitRelatedClassLineMarkerProviderBase {

  @Override
  public String getName() {
    return DevkitHelperBundle.message("gutter.related.extension.declaration");
  }

  @NotNull
  @Override
  public Icon getIcon() {
    return DevkitIcons.Gutter.Plugin;
  }

  @Override
  protected void process(@NotNull PsiElement identifier,
                         @NotNull PsiClass psiClass,
                         @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
//    List<ExtensionCandidate> targets = ExtensionLocatorKt.locateExtensionsByPsiClass(psiClass);
    List<ExtensionCandidate> targets = ActionLocatorKt.locateActionsByPsiClass(psiClass);
    if (targets.isEmpty()) {
      return;
    }

    System.out.println("ExtensionDeclarationRelatedItemLineMarkerProvider targets.size() =" + targets.size());

    result.add(LineMarkerInfoHelper.createExtensionLineMarkerInfo(targets, identifier));
  }
}
