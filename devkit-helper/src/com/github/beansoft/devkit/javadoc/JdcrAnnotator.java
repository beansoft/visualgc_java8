package com.github.beansoft.devkit.javadoc;


import com.github.beansoft.devkit.javadoc.utils.JdcrPsiTreeUtils;
import com.github.beansoft.devkit.javadoc.utils.JdcrStringUtils;
import com.github.beansoft.devkit.javadoc.utils.Tag;
import com.github.beansoft.devkit.provider.BrowserJvmSpecIntentionAction;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.*;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaDocTokenType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.impl.source.javadoc.PsiDocMethodOrFieldRef;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.javadoc.PsiInlineDocTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class JdcrAnnotator implements Annotator {

  private AnnotationHolder holder;
  private PsiElement element;
  private List<TextRange> foundHtmlTags = EMPTY_LIST;
  private List<TextRange> multiLineTagRangesInParent = EMPTY_LIST;

  private static final Tag CODE_TAG = new Tag("<code>", "</code>");
  private static final Tag TT_TAG = new Tag("<tt>", "</tt>");
  private static final Tag PRE_TAG = new Tag("<pre>", "</pre>");
  private static final Tag A_HREF_TAG = new Tag("<a href=", "</a>");
  private static final Tag A_NAME_TAG = new Tag("<a name=", "</a>");
  private static final Tag BOLD_TAG = new Tag("<b>", "</b>");
  private static final Tag ITALIC_TAG = new Tag("<i>", "</i>");
  private static final Tag EM_TAG = new Tag("<em>", "</em>");

  private static final List<TextRange> EMPTY_LIST = Collections.emptyList();

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    this.holder = holder;
    this.element = element;

    if (element instanceof PsiIdentifier) {
      PsiIdentifier identifier = (PsiIdentifier)element;
      String qualifiedName = identifier.getText();
      if("GETSTATIC".equals(qualifiedName)) {// JavaDocBundle.containsKey(qualifiedName)
        System.out.println("JdcrAnnotator 发现 PsiIdentifier " + qualifiedName);
        StringUtil.escapeXmlEntities("Get Static");//JavaDocBundle.message(qualifiedName));
        String explain = "Get Static";
        TextRange tagStart = new TextRange(0, explain.length());
        TextRange range = tagStart.shiftRight(element.getTextRange().getStartOffset());
        AnnotationBuilder builder = holder
                .newAnnotation(HighlightSeverity.INFORMATION, explain)
        .tooltip("beansoft")
        .withFix(new BrowserJvmSpecIntentionAction("test"))
        .range(element)
//        .highlightType(ProblemHighlightType.INFORMATION)
        .textAttributes(JdcrColorSettingsPage.CODE_TAG);
        builder.create();
//        annotation.setTextAttributes(JdcrColorSettingsPage.CODE_TAG);

        return;
      }

//            return "\"" + renderPropertyValue((PsiIdentifier)element) + "\"" + getLocationString(element);
    }

    if (element instanceof PsiDocToken
        && ((PsiDocToken) element).getTokenType() == JavaDocTokenType.DOC_COMMENT_DATA
        && JdcrPsiTreeUtils.isNotInsideCodeOrLiteralTag((PsiDocToken) element)) {

      foundHtmlTags = JdcrStringUtils.getHtmlTags(element.getText());
      multiLineTagRangesInParent = JdcrPsiTreeUtils.getMultiLineTagRangesInParent(element);
      if (!foundHtmlTags.isEmpty() || !multiLineTagRangesInParent.isEmpty()) {

        // Annotate Font style HTML tags
        annotateTagValue(BOLD_TAG, JdcrColorSettingsPage.BOLD_FONT);
        annotateTagValue(ITALIC_TAG, JdcrColorSettingsPage.ITALIC_FONT);
        annotateTagValue(EM_TAG, JdcrColorSettingsPage.ITALIC_FONT);

        // Annotate Code HTML tags
        annotateTagValue(CODE_TAG, JdcrColorSettingsPage.CODE_TAG);
        annotateTagValue(TT_TAG, JdcrColorSettingsPage.CODE_TAG);
        annotateTagValue(PRE_TAG, JdcrColorSettingsPage.CODE_TAG);

        // Annotate HTML link< a href=...> tags
        annotateTagValue(A_HREF_TAG, JdcrColorSettingsPage.HTML_LINK_TAG);

        // Annotate <a name=...> tags
        annotateTagValue(A_NAME_TAG, JdcrColorSettingsPage.BOLD_FONT);

        // Annotate Html Tags (including multiline Tags)
        annotateHtmlTags();
      }
      // Annotate Html Escaped Chars
      annotateHtmlEscapedChars();

    } else if (element instanceof PsiInlineDocTag) {
      String tagName = ((PsiInlineDocTag) element).getName();
      if (JdcrStringUtils.CODE_TAGS.contains(tagName)
          || JdcrStringUtils.LINK_TAGS.contains(tagName)) {
        annotateJavaDocTagStartEnd((PsiInlineDocTag) element);
      }
      if (tagName.equals("code")) { // @code
        annotateDocTagValue((PsiInlineDocTag) element);
      }
    } else if (element instanceof PsiDocMethodOrFieldRef) { // @link @linkplain @value
      // fix {@link #to_method_call} is not highlighted:
      // https://youtrack.jetbrains.com/issue/IDEA-197760
      annotateLinkTagMethodRef();
    }
    this.holder = null;
    this.element = null;
    foundHtmlTags = EMPTY_LIST;
    multiLineTagRangesInParent = EMPTY_LIST;
  }

  private void annotateJavaDocTagStartEnd(@NotNull PsiInlineDocTag psiInlineDocTag) {
    // annotate JavaDoc tag Start
    TextRange tagStart = new TextRange(0, 2 /* `{@` */ + psiInlineDocTag.getName().length());
    doAnnotate(
        tagStart.shiftRight(psiInlineDocTag.getTextRange().getStartOffset()),
        JdcrColorSettingsPage.BORDERED);
    // annotate JavaDoc tag End
    if (JdcrPsiTreeUtils.isCompleteJavaDocTag(psiInlineDocTag)) {
      doAnnotate(
          psiInlineDocTag.getLastChild().getTextRange() /* } */, JdcrColorSettingsPage.BORDERED);
    }
  }

  private void annotateLinkTagMethodRef() {
    TextRange linkRefRangeInElement =
        new TextRange(element.getTextOffset(), element.getTextRange().getEndOffset())
            .shiftLeft(element.getTextRange().getStartOffset());
    // multiline link reference case: {@link Integer#toString(
    // ) toString}
    for (TextRange range : JdcrPsiTreeUtils.excludeLineBreaks(element, linkRefRangeInElement)) {
      doAnnotate(
          range.shiftRight(element.getTextRange().getStartOffset()),
          JdcrColorSettingsPage.LINK_TAG);
    }
  }

  private void annotateDocTagValue(@NotNull PsiInlineDocTag psiInlineDocTag) {
    final TextRange valueRange =
        new TextRange(
            2 /* {@ */ + psiInlineDocTag.getName().length(),
            psiInlineDocTag.getTextLength()
                - (JdcrPsiTreeUtils.isCompleteJavaDocTag(psiInlineDocTag) ? 1 /* } */ : 0));
    for (TextRange eachLineRange :
        JdcrPsiTreeUtils.excludeLineBreaks(psiInlineDocTag, valueRange)) {
      doAnnotate(
          eachLineRange.shiftRight(psiInlineDocTag.getTextRange().getStartOffset()),
          JdcrColorSettingsPage.CODE_TAG);
    }
  }

  private void annotateHtmlTags() {
    for (TextRange range : foundHtmlTags) {
      doAnnotate(
          range.shiftRight(element.getTextRange().getStartOffset()),
          JdcrColorSettingsPage.BORDERED);
    }
    // Annotate multiline Tag, fix https://youtrack.jetbrains.com/issue/IDEA-198738
    for (TextRange range : multiLineTagRangesInParent) {
      range = range.shiftRight(element.getParent().getTextRange().getStartOffset());
      doAnnotate(range, DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP);
      doAnnotate(range, JdcrColorSettingsPage.BORDERED);
    }
  }

  private void annotateHtmlEscapedChars() {
    for (TextRange textRange : JdcrStringUtils.getHtmlEscapedChars(element.getText())) {
      doAnnotate(
          textRange.shiftRight(element.getTextRange().getStartOffset()),
          JdcrColorSettingsPage.BORDERED);
    }
  }

  private void annotateTagValue(Tag tag, @NotNull TextAttributesKey textAttributesKey) {
    ArrayList<TextRange> rangesToAnnotate = new ArrayList<>();

    for (TextRange tagValue :
        JdcrStringUtils.getValuesOfTag(element.getText(), tag, foundHtmlTags)) {
      if (tagValue.getEndOffset() == element.getTextLength()) {
        // lonely open tag found withing current PsiDocToken
        // possible start of multiline value of tag.
        int tagValueStartInParent = tagValue.getStartOffset() + element.getStartOffsetInParent();
        rangesToAnnotate.addAll(getTagValueRanges(tag, tagValueStartInParent));
      } else if (tagValue.getStartOffset() != 0) {
        // don't annotate lonely close tag, should be covered in above case
        rangesToAnnotate.add(tagValue.shiftRight(element.getTextRange().getStartOffset()));
      }
    }
    // Check for Multi-line open tag.
    if (!multiLineTagRangesInParent.isEmpty()
        && tag.openIn(getMultilineTagText(element.getParent(), multiLineTagRangesInParent))) {
      int tagValueStartInParent = getLast(multiLineTagRangesInParent).getEndOffset();
      rangesToAnnotate.addAll(getTagValueRanges(tag, tagValueStartInParent));
    }

    rangesToAnnotate.forEach(range -> doAnnotate(range, textAttributesKey));
  }

  private <T> T getLast(@NotNull List<T> list) {
    if (list.isEmpty()) throw new NoSuchElementException();
    return list.get(list.size() - 1);
  }

  private String getMultilineTagText(
      @NotNull PsiElement parent, @NotNull List<TextRange> multilineTagRangesInParent) {
    return multilineTagRangesInParent.stream()
        .map(range -> range.substring(parent.getText()))
        .reduce("", String::concat);
  }

  /**
   * Look ahead for close tag.
   *
   * @param tag to check
   * @param tagValueStartInParent offset <i>relatively</i> to {@code element.getParent()}
   * @return absolute ranges of Tag Value
   */
  private List<TextRange> getTagValueRanges(Tag tag, int tagValueStartInParent) {

    int tagValueEndInParent = -1;
    PsiElement inspectingElement = element;
    while (inspectingElement != null && tagValueEndInParent == -1) {
      if (inspectingElement.getNode().getElementType() == JavaDocTokenType.DOC_COMMENT_DATA) {

        int startOffsetInParent = inspectingElement.getStartOffsetInParent();
        tagValueEndInParent =
            JdcrStringUtils.getValuesOfTag(inspectingElement.getText(), tag).stream()
                .filter(range1 -> range1.getStartOffset() == 0)
                // close tag found
                .findFirst()
                .map(TextRange::getEndOffset)
                .map(endOffsetInsideInspecting -> endOffsetInsideInspecting + startOffsetInParent)
                .filter(endOffsetInParent -> tagValueStartInParent <= endOffsetInParent)
                // </tag>...<tag> case
                .orElse(-1);

        if (tagValueEndInParent == -1) {
          // Check for Multi-line close tag.
          List<TextRange> multiLineTagRangesInParent =
              JdcrPsiTreeUtils.getMultiLineTagRangesInParent(inspectingElement);
          if (!multiLineTagRangesInParent.isEmpty()
              && tag.closeIn(
                  getMultilineTagText(element.getParent(), multiLineTagRangesInParent))) {
            tagValueEndInParent = getFirst(multiLineTagRangesInParent).getStartOffset();
          }
        }
      }
      inspectingElement = inspectingElement.getNextSibling();
    }

    return (tagValueEndInParent == -1)
        ? EMPTY_LIST
        : JdcrPsiTreeUtils.excludeLineBreaks(
                element.getParent(), new TextRange(tagValueStartInParent, tagValueEndInParent))
            .stream()
            .map(range -> range.shiftRight(element.getParent().getTextRange().getStartOffset()))
            .collect(Collectors.toCollection(ArrayList::new));
  }

  private <T> T getFirst(@NotNull List<T> list) {
    if (list.isEmpty()) throw new NoSuchElementException();
    return list.get(0);
  }

  private static int countAnnotation = 0;

  private void doAnnotate(
      @NotNull TextRange absoluteRange, @NotNull TextAttributesKey textAttributesKey) {
    Annotation annotation =
        holder.createInfoAnnotation(absoluteRange, textAttributesKey.getExternalName());
    annotation.setTooltip(null);
    annotation.setTextAttributes(textAttributesKey);
    /*
        countAnnotation++;
        if (countAnnotation % 1000 == 1)
          System.out.printf("%s  %6d annotations\n",
              LocalDateTime.now(),
              countAnnotation
          );
    */
  }
}
