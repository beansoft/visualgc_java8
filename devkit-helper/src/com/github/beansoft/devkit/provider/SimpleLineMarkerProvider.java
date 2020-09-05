package com.github.beansoft.devkit.provider;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author beansoft@126.com
 */
public abstract class SimpleLineMarkerProvider<F extends PsiElement, T>  implements LineMarkerProvider {

    private static final Logger logger = LoggerFactory.getLogger(SimpleLineMarkerProvider.class);

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public LineMarkerInfo<? extends PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!isTheElement(element)) return null;
        logger.info("getLineMarkerInfo start, element: {}", element);
        logger.info("xml加入跳转图标开始");
        Optional<? extends T> processResult = apply((F) element);
        Optional<LineMarkerInfo<? extends PsiElement>> optional = processResult.map(t -> new LineMarkerInfo<>(
            (F) element,
            element.getTextRange(),
            getIcon(),
            getTooltipProvider(t),
            getNavigationHandler(t),
            GutterIconRenderer.Alignment.CENTER
        ));
        logger.info("getLineMarkerInfo end");
        return optional.orElse(null);
    }

    private Function<F, String> getTooltipProvider(final T target) {
        return from -> getTooltip(from, target);
    }

    private GutterIconNavigationHandler<F> getNavigationHandler(final T target) {
        return (e, from) -> getNavigatable(from, target).navigate(true);
    }

    public abstract boolean isTheElement(@NotNull PsiElement element);

    public abstract Optional<? extends T> apply(@NotNull F from);

    @NotNull
    public abstract Navigatable getNavigatable(@NotNull F from, @NotNull T target);

    @NotNull
    public abstract String getTooltip(@NotNull F from, @NotNull T target);

    @NotNull
    public abstract Icon getIcon();
}
