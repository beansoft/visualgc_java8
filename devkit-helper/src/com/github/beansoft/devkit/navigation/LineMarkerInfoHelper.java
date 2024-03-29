// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.beansoft.devkit.navigation;

import com.github.beansoft.devkit.DevkitHelperBundle;
import com.github.beansoft.devkit.util.text.HtmlBuilder;
import com.github.beansoft.devkit.util.text.HtmlChunk;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.ColorUtil;
import com.intellij.util.NotNullFunction;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import icons.DevkitIcons;
import icons.PluginIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;
import org.jetbrains.idea.devkit.dom.Action;
import org.jetbrains.idea.devkit.dom.Extension;
import org.jetbrains.idea.devkit.dom.ExtensionPoint;
import org.jetbrains.idea.devkit.util.PointableCandidate;

import java.util.Collection;
import java.util.List;

final class LineMarkerInfoHelper {
  private static final NotNullFunction<PointableCandidate, Collection<? extends PsiElement>> CONVERTER =
    candidate -> ContainerUtil.createMaybeSingletonList(candidate.pointer.getElement());
  private static final NotNullFunction<PointableCandidate, Collection<? extends GotoRelatedItem>> RELATED_ITEM_PROVIDER =
    candidate -> GotoRelatedItem.createItems(ContainerUtil.createMaybeSingletonList(candidate.pointer.getElement()), "DevKit");

  // 名称转换
  private static final NullableFunction<PointableCandidate, String> EXTENSION_NAMER =
    createNamer("line.marker.tooltip.extension.declaration", tag -> {
      final DomElement element = DomUtil.getDomElement(tag);

      if (element instanceof Action) {
        return "action id=\"" + ((Action)element).getId().toString() + "\"";
      }

      if (!(element instanceof Extension)) return "?";
      return getExtensionPointName(((Extension)element).getExtensionPoint());
    });

  private static final NullableFunction<PointableCandidate, String> EXTENSION_POINT_NAMER =
    createNamer("line.marker.tooltip.extension.point.declaration", tag -> {
      return getExtensionPointName(DomUtil.getDomElement(tag));
    });

  // 获取元素显示名称
  @NotNull
  private static String getExtensionPointName(DomElement element) {
    if (element instanceof Action) {
      return ((Action)element).getText().toString();
    }
    if (!(element instanceof ExtensionPoint)) return "?";
    return ((ExtensionPoint)element).getEffectiveQualifiedName();
  }

  private LineMarkerInfoHelper() {
  }

  @NotNull
  static RelatedItemLineMarkerInfo<PsiElement> createExtensionLineMarkerInfo(@NotNull List<? extends PointableCandidate> targets,
                                                                             @NotNull PsiElement element) {
    return createPluginLineMarkerInfo(targets, element,
            DevkitHelperBundle.message("gutter.related.navigation.choose.extension"),
                                      EXTENSION_NAMER);
  }

  @NotNull
  static RelatedItemLineMarkerInfo<PsiElement> createExtensionPointLineMarkerInfo(@NotNull List<? extends PointableCandidate> targets,
                                                                                  @NotNull PsiElement element) {
    return createPluginLineMarkerInfo(targets, element,
            DevkitHelperBundle.message("gutter.related.navigation.choose.extension.point"),
                                      EXTENSION_POINT_NAMER);
  }

  @NotNull
  private static RelatedItemLineMarkerInfo<PsiElement> createPluginLineMarkerInfo(@NotNull List<? extends PointableCandidate> targets,
                                                                                  @NotNull PsiElement element,
                                                                                  @Nls(capitalization = Nls.Capitalization.Title) String popup,
                                                                                  NullableFunction<PointableCandidate, String> namer) {
    return NavigationGutterIconBuilder
      .create(PluginIcons.Gutter_Plugin, CONVERTER, RELATED_ITEM_PROVIDER)//DevkitHelpIcons.XmlFile_12
      .setTargets(targets)
      .setPopupTitle(popup)
      .setNamer(namer)
      .setAlignment(GutterIconRenderer.Alignment.RIGHT)
      .createLineMarkerInfo(element);
  }

  @NotNull
  private static NullableFunction<PointableCandidate, String> createNamer(@PropertyKey(resourceBundle = DevkitHelperBundle.BUNDLE) String tooltipPatternPropertyName,
                                                                          NotNullFunction<? super XmlTag, String> nameProvider) {
    return target -> {
      XmlTag tag = target.pointer.getElement();
      if (tag == null) {
        // shouldn't happen
        throw new NullPointerException("Null element for pointable candidate: " + target);
      }

      PsiFile file = tag.getContainingFile();
      String path = file.getVirtualFile().getPath();

      String fileDisplayName = file.getName();
      Module module = ModuleUtilCore.findModuleForPsiElement(file);
      if (module != null) {
        fileDisplayName += new HtmlBuilder()
          .append(" ")
          .append(HtmlChunk.text("[" + module.getName() + "]")
                    .wrapWith(HtmlChunk.font(ColorUtil.toHex(UIUtil.getInactiveTextColor()))));
      }

      return DevkitHelperBundle.message(tooltipPatternPropertyName,
                                  path, String.valueOf(tag.getTextRange().getStartOffset()), nameProvider.fun(tag),
                                  fileDisplayName);
    };
  }
}
