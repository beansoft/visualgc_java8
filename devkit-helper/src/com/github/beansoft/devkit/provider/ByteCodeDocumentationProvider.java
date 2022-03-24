package com.github.beansoft.devkit.provider;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.ExternalDocumentationProvider;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.ui.GuiUtils;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ByteCodeDocumentationProvider extends AbstractDocumentationProvider implements ExternalDocumentationProvider {
    public ByteCodeDocumentationProvider() {
        System.out.println("new ByteCodeDocumentationProvider()");
    }
    /**
     * Returns the text to show in the Ctrl-hover popup for the specified element.
     *
     * @param element         the element for which the documentation is requested (for example, if the mouse is over
     *                        a method reference, this will be the method to which the reference is resolved).
     * @param originalElement the element under the mouse cursor
     * @return the documentation to show, or {@code null} if the provider can't provide any documentation for this element. Documentation can contain
     * HTML markup. If HTML special characters need to be shown in popup, they should be properly escaped.
     */
    @Override
    @Nullable
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        if (element instanceof PsiIdentifier) {
            PsiIdentifier identifier = (PsiIdentifier)element;
            String qualifiedName = identifier.getText();
            if("GETSTATIC".equals(qualifiedName)) {// JavaDocBundle.containsKey(qualifiedName)
                System.out.println("getQuickNavigateInfo 发现 PsiIdentifier " + qualifiedName);
                return StringUtil.escapeXmlEntities("Get Static");//JavaDocBundle.message(qualifiedName));
            }

//            return "\"" + renderPropertyValue((PsiIdentifier)element) + "\"" + getLocationString(element);
        }
        return null;
    }

    @Override
    public List<String> getUrlFor(final PsiElement element, final PsiElement originalElement) {
        if (element instanceof PsiIdentifier) {
            PsiIdentifier identifier = (PsiIdentifier)element;
            String qualifiedName = identifier.getText();
            if("GETSTATIC".equals(qualifiedName)) {// JavaDocBundle.containsKey(qualifiedName)
                return Arrays.asList("https://docs.oracle.com/javase/specs/jvms/se16/html/jvms-6.html#jvms-6.5.aload_n");
            }

//            return "\"" + renderPropertyValue((PsiIdentifier)element) + "\"" + getLocationString(element);
        }
        return null;
    }

    private static String getLocationString(PsiElement element) {
        PsiFile file = element.getContainingFile();
        return file != null ? " [" + file.getName() + "]" : "";
    }


    /**
     * <p>Callback for asking the doc provider for the complete documentation.
     * Underlying implementation may be time-consuming, that's why this method is expected not to be called from EDT.</p>
     *
     * <p>One can use {@link DocumentationMarkup} to get proper content layout. Typical sample will look like this:
     * <pre>
     * DEFINITION_START + definition + DEFINITION_END +
     * CONTENT_START + main description + CONTENT_END +
     * SECTIONS_START +
     *   SECTION_HEADER_START + section name +
     *     SECTION_SEPARATOR + "&lt;p&gt;" + section content + SECTION_END +
     *   ... +
     * SECTIONS_END
     * </pre>
     * </p>
     * To show different content on mouse hover in editor, {@link #generateHoverDoc(PsiElement, PsiElement)} should be implemented.
     *
     * @param element         the element for which the documentation is requested (for example, if the mouse is over
     *                        a method reference, this will be the method to which the reference is resolved).
     * @param originalElement the element under the mouse cursor
     * @return target element's documentation, or {@code null} if provider is unable to generate documentation
     * for the given element
     */
    @Override
    public String generateDoc(final PsiElement element, @Nullable final PsiElement originalElement) {
        return getQuickNavigateInfo(element, originalElement);
//        if (element instanceof IProperty) {
//            IProperty property = (IProperty)element;
//            String text = property.getDocCommentText();
//
//            @NonNls String info = "";
//            if (text != null) {
//                TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(PropertiesHighlighter.PROPERTY_COMMENT).clone();
//                Color background = attributes.getBackgroundColor();
//                if (background != null) {
//                    info +="<div bgcolor=#"+ GuiUtils.colorToHex(background)+">";
//                }
//                String doc = StringUtil.join(ContainerUtil.map(StringUtil.split(text, "\n"), s -> {
//                    final String trimHash = StringUtil.trimStart(s, PropertiesCommenter.HASH_COMMENT_PREFIX);
//                    final String trimExclamation = StringUtil.trimStart(trimHash, PropertiesCommenter.EXCLAMATION_COMMENT_PREFIX);
//                    return trimExclamation.trim();
//                }), "<br>");
//                final Color foreground = attributes.getForegroundColor();
//                info += foreground != null ? "<font color=#" + GuiUtils.colorToHex(foreground) + ">" + doc + "</font>" : doc;
//                info += "\n<br>";
//                if (background != null) {
//                    info += "</div>";
//                }
//            }
//            info += "\n<b>" + property.getName() + "</b>=\"" + renderPropertyValue(((IProperty)element)) + "\"";
//            info += getLocationString(element);
//            return info;
//        }
//        return null;
    }

    @Override
    public boolean hasDocumentationFor(PsiElement element, PsiElement originalElement) {
        return false;
    }

    @Override
    public boolean canPromptToConfigureDocumentation(PsiElement element) {
        return false;
    }

    @Override
    public void promptToConfigureDocumentation(PsiElement element) {}

    @Override
    public @Nullable String fetchExternalDocumentation(Project project, PsiElement element, List<String> docUrls, boolean onHover) {
        System.out.println("fetchExternalDocumentation " + element.getClass());
        return null;
    }
}
