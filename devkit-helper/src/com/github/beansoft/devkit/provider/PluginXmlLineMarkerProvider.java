package com.github.beansoft.devkit.provider;

import com.github.beansoft.devkit.util.JavaUtils;
import com.github.beansoft.devkit.util.PluginXmlUtil;
import com.google.common.collect.ImmutableSet;
import com.intellij.codeInsight.daemon.impl.GutterTooltipHelper;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Optional;

/**
 * Xml跳转到类文件定义.
 * TODO 性能优化
 * @author beansoft@126.com
 */
public class PluginXmlLineMarkerProvider extends SimpleLineMarkerProvider<XmlTag, PsiElement> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleLineMarkerProvider.class);

    private static final ImmutableSet<String> ATTRIBUTE_NAMES = ImmutableSet.of(
        "class", "implementation"
    );

    @Override
    public boolean isTheElement(@NotNull PsiElement element) {
        return element instanceof XmlTag
            && isTargetType(((XmlTag)element))
            && PluginXmlUtil.isIdeaPluginTagContainingFile(element);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<? extends PsiElement> apply(@NotNull XmlTag from) {
        String attributeName = findLegalTargetName(from);
        if(attributeName == null) {
            return Optional.empty();
        }
        String implementation = from.getAttributeValue(attributeName);
        if(implementation == null) {
            return Optional.empty();
        }
        Optional<PsiClass> clazz = JavaUtils.findClazz(from.getProject(), implementation);
        logger.info("clazz 查找类结束, {}",clazz.orElse(null));
        return clazz;
    }

    private boolean isTargetType(@NotNull XmlTag xmlTag) {
        return findLegalTargetName(xmlTag) != null;
    }

    private String findLegalTargetName(@NotNull XmlTag xmlTag) {
        XmlAttribute[] attributes = xmlTag.getAttributes();
        for (XmlAttribute attribute : attributes) {
            String name = attribute.getName().toLowerCase();
            for (String legalName :
                    ATTRIBUTE_NAMES) {
                if(name.contains(legalName)) {
                    return attribute.getName();
                }
            }
        }
//        return TARGET_TYPES.contains(name);
        return null;
    }

    @NotNull
    @Override
    public Navigatable getNavigatable(@NotNull XmlTag from, @NotNull PsiElement target) {
        return (Navigatable) target.getNavigationElement();
    }

    @NotNull
    @Override
    public String getTooltip(@NotNull XmlTag from, @NotNull PsiElement target) {
//        String text = null;
//        if (target instanceof PsiMethod) {
//            PsiMethod psiMethod = (PsiMethod) target;
//            PsiClass containingClass = psiMethod.getContainingClass();
//            if (containingClass != null) {
//                text = containingClass.getQualifiedName() + "#" +psiMethod.getName();
//            }
//        }
//        if (text == null && target instanceof PsiClass) {
//            PsiClass psiClass = (PsiClass) target;
//            text = psiClass.getQualifiedName();
//        }
//        if (text == null) {
//            text = target.getContainingFile().getText();
//        }
//        return "Go to - " + text;

        return GutterTooltipHelper.getTooltipText(ImmutableSet.of(target), "Open", false,
                IdeActions.ACTION_GOTO_IMPLEMENTATION);
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return AllIcons.Gutter.OverridenMethod;
    }

}
