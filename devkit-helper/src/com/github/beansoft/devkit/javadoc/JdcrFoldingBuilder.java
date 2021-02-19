package com.github.beansoft.devkit.javadoc;

import com.github.beansoft.devkit.javadoc.utils.JdcrPsiTreeUtils;
import com.github.beansoft.devkit.javadoc.utils.JdcrStringUtils;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.JavaDocElementType;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.javadoc.PsiInlineDocTag;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.parser.Parser;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class JdcrFoldingBuilder implements FoldingBuilder {

    private static final int LENGTH_DOC_INLINE_TAG_END = 1; // }
    private static final FoldingDescriptor[] EMPTY_ARRAY = {};
    private Deque<FoldingDescriptor> foldingDescriptors;
    private FoldingGroup foldingGroup;

    public JdcrFoldingBuilder() {
    }

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
        PsiElement root = node.getPsi();// 这里总是 PsiJavaFile.... 没有单个节点
        foldingDescriptors = new ArrayDeque<>();

        long startTime = System.currentTimeMillis();
        PsiTreeUtil.findChildrenOfType(root, PsiAnnotation.class).stream()
                .forEach(this::replaceAnnotation);

        for (PsiDocComment psiDocComment : PsiTreeUtil.findChildrenOfType(root, PsiDocComment.class)) {
            foldingGroup = FoldingGroup.newGroup("JDCR fold: " + psiDocComment.getTextRange().toString());

            PsiTreeUtil.findChildrenOfType(psiDocComment, PsiDocToken.class).stream()
                    .forEach(this::removeLeadingAsterisks);

            PsiTreeUtil.findChildrenOfType(psiDocComment, PsiDocToken.class).stream()
                    .filter(JdcrPsiTreeUtils::isNotInsideCodeOrLiteralTag)
                    .forEach(this::checkHtmlTagsAndEscapedChars);

            PsiTreeUtil.findChildrenOfType(psiDocComment, PsiInlineDocTag.class).stream()
                    .filter(JdcrPsiTreeUtils::isCompleteJavaDocTag)
                    .forEach(this::checkInlineJavaDocTags);
        }

        // 单行注释, 暂不处理
//        for (PsiComment psiComment : PsiTreeUtil.findChildrenOfType(root, PsiComment.class)) {
//            foldingGroup = FoldingGroup.newGroup("JDCR fold: " + psiComment.getTextRange().toString());
//            checkSingleLineComment(psiComment);
//        }

        System.out.printf("File: %-20s  Folding time: %6d,  Total folds created: %6d\n",
                root.getContainingFile().getName(),
                (System.currentTimeMillis() - startTime),
                foldingDescriptors.size());

        return foldingDescriptors.toArray(new FoldingDescriptor[0]);
    }

    /**
     * Add FoldingDescriptors for inline JavaDoc tags: {@link JdcrStringUtils#CODE_TAGS} {@link
     * JdcrStringUtils#LINK_TAGS}
     */
    private void checkInlineJavaDocTags(@NotNull PsiInlineDocTag psiInlineDocTag) {
        String tagName = psiInlineDocTag.getName();
        if (JdcrStringUtils.CODE_TAGS.contains(tagName)) {
            foldJavaDocTagStartEnd(psiInlineDocTag);
        } else if (JdcrStringUtils.LINK_TAGS.contains(tagName)) {
            foldJavaDocTagStartEnd(psiInlineDocTag);

            // Folding label part of @link tag
            Arrays.stream(psiInlineDocTag.getChildren())
                    .filter(
                            child ->
                                    // link through # within current file
                                    (child.getNode().getElementType() == JavaDocElementType.DOC_METHOD_OR_FIELD_REF
                                            // link to outer file
                                            || child.getNode().getElementType()
                                            == JavaDocElementType.DOC_REFERENCE_HOLDER)
                                            // @link tag has text label part to fold
                                            && child.getNextSibling() != psiInlineDocTag.getLastChild())
                    .findFirst()
                    .map(
                            linkToShow ->
                                    new TextRange(
                                            linkToShow.getTextRange().getEndOffset()
                                                    - psiInlineDocTag.getTextRange().getStartOffset(),
                                            psiInlineDocTag.getTextLength() - LENGTH_DOC_INLINE_TAG_END))
                    .ifPresent(
                            labelToFold ->
                                    JdcrPsiTreeUtils.excludeLineBreaks(psiInlineDocTag, labelToFold)
                                            .forEach(range -> addFoldingDescriptor(psiInlineDocTag, range)));
        }
    }

    /**
     * Use only for <b>complete</b> DocTag
     */
    private void foldJavaDocTagStartEnd(@NotNull PsiInlineDocTag psiInlineDocTag) {
        // fold JavaDoc tag Start
        int tagStartLength = 2 /* `{@` */ + psiInlineDocTag.getName().length(); // `code`, `link` ...
        if (tagStartLength != psiInlineDocTag.getLastChild().getStartOffsetInParent()) {
            tagStartLength += 1; /* include space after tag name if any*/
        }
        JdcrPsiTreeUtils.excludeLineBreaks(psiInlineDocTag, new TextRange(0, tagStartLength))
                .forEach(range -> addFoldingDescriptor(psiInlineDocTag, range));
        // fold JavaDoc tag End: `}`
        addFoldingDescriptor(psiInlineDocTag.getLastChild(), new TextRange(0, 1));
    }

    /**
     * Add FoldingDescriptors for single line comments
     */
    private void checkSingleLineComment(@NotNull PsiComment psiDocToken) {
//        String docTokenText = psiDocToken.getText();
//        System.out.println("docTokenText=" + docTokenText);
//        System.out.println("psiDocToken=" + psiDocToken.getTokenType().getClass());

        if (psiDocToken.getTokenType()
                == JavaTokenType.END_OF_LINE_COMMENT) {
            System.out.println("发现单行注释");
            TextRange range = new TextRange(0, 2);
            addFoldingDescriptor(psiDocToken, range, " ");
        }
    }

    // 删除头部*号
    private void removeLeadingAsterisks(@NotNull PsiDocToken psiDocToken) {
        String docTokenText = psiDocToken.getText();
//        System.out.println("checkHtmlTagsAndEscapedChars docTokenText=" + docTokenText);
//        System.out.println("psiDocToken=" + psiDocToken.getTokenType().getClass());

        if(JavaDocBundle.containsKey(docTokenText)) {
//            System.out.println("发现" + docTokenText);
            TextRange range = new TextRange(0, docTokenText.length());
            addFoldingDescriptor(psiDocToken, range, JavaDocBundle.message(docTokenText));
            return;
        }

        if (psiDocToken.getTokenType()
                == JavaDocTokenType.DOC_COMMENT_LEADING_ASTERISKS) {
            TextRange range = new TextRange(0, 1);
            addFoldingDescriptor(psiDocToken, range, " ");
        }
    }

    // 替换部分固定的Annotation
    private void replaceAnnotation(@NotNull PsiAnnotation annotation) {
        String qualifiedName = annotation.getQualifiedName();
        System.out.println("replaceAnnotation.qualifiedName=" + qualifiedName);

//        if(javaannotationTagReplaces.containsKey(qualifiedName)) {
        if(JavaDocBundle.containsKey(qualifiedName)) {
            System.out.println("发现 PsiAnnotation " + qualifiedName);
            PsiJavaCodeReferenceElement nameElement = annotation.getNameReferenceElement();
            if(nameElement != null) {
                TextRange range = new TextRange(0, nameElement.getText().length());
                addFoldingDescriptor(nameElement, range, JavaDocBundle.message(qualifiedName));
            }
        }

    }

    /**
     * Add FoldingDescriptors for HTML tags and Escaped Chars
     */
    private void checkHtmlTagsAndEscapedChars(@NotNull PsiDocToken psiDocToken) {
        String docTokenText = psiDocToken.getText();
//        System.out.println("checkHtmlTagsAndEscapedChars docTokenText=" + docTokenText);
//        System.out.println("psiDocToken=" + psiDocToken.getTokenType().getClass());

        for (TextRange range : JdcrStringUtils.getHtmlTags(docTokenText)) {
            String tagsToFold = range.substring(docTokenText).toLowerCase();
            if (tagsToFold.contains("<li>")) {
                addFoldingDescriptor(psiDocToken, range, " - ");
                //              } else if (tagsToFold.contains("<td>")) {
                //                addFoldingDescriptor(psiDocToken, textRange, "\t");
            } else {
                addFoldingDescriptor(psiDocToken, range);
            }
        }
        // Check for Multi-line tag.
        JdcrPsiTreeUtils.getMultiLineTagRangesInParent(psiDocToken)
                .forEach(textRange -> addFoldingDescriptor(psiDocToken.getParent(), textRange));

        for (TextRange textRange : JdcrStringUtils.getHtmlEscapedChars(docTokenText)) {
            addFoldingDescriptor(
                    psiDocToken, textRange, Parser.unescapeEntities(textRange.substring(docTokenText), true));
        }
    }

    private void addFoldingDescriptor(@NotNull PsiElement element, @NotNull TextRange range) {
        addFoldingDescriptor(element, range, ""); // "◊"
    }

    private void addFoldingDescriptor(
            @NotNull PsiElement element, @NotNull TextRange range, String placeholderText) {

        // reducing folding regions amount by joint sequential regions into one: <i><b>...
        TextRange absoluteNewRange = range.shiftRight(element.getTextRange().getStartOffset());
        if (!foldingDescriptors.isEmpty()
                && foldingDescriptors.peek().getRange().getEndOffset()
                == absoluteNewRange.getStartOffset()) {
            FoldingDescriptor prevFoldingDescriptor = foldingDescriptors.pop();
            absoluteNewRange =
                    new TextRange(
                            prevFoldingDescriptor.getRange().getStartOffset(), absoluteNewRange.getEndOffset());
            placeholderText = prevFoldingDescriptor.getPlaceholderText() + placeholderText;
        }

        foldingDescriptors.push(
                new FoldingDescriptor(
                        element.getNode(), absoluteNewRange, foldingGroup, placeholderText));
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        return null;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return true;
    }
}
