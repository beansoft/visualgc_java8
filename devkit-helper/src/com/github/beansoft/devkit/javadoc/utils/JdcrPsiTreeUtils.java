package com.github.beansoft.devkit.javadoc.utils;

import com.intellij.openapi.editor.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.JavaDocTokenType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.javadoc.PsiInlineDocTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.psi.JavaDocTokenType.*;

public class JdcrPsiTreeUtils {

  /**
   * Check if PsiDocToken is not inside {@link JdcrStringUtils#CODE_TAGS} -> do not interpreting the
   * text as HTML markup
   */
  public static boolean isNotInsideCodeOrLiteralTag(@NotNull PsiDocToken psiDocToken) {
    if (psiDocToken.getParent() instanceof PsiInlineDocTag) {
      String parentName = ((PsiInlineDocTag) psiDocToken.getParent()).getName();
      return !JdcrStringUtils.CODE_TAGS.contains(parentName);
    }
    return true;
  }

  public static boolean isCompleteJavaDocTag(PsiInlineDocTag psiInlineDocTag) {
    return psiInlineDocTag.getFirstChild().getNode().getElementType() == DOC_INLINE_TAG_START
        && psiInlineDocTag.getFirstChild().getNextSibling().getNode().getElementType()
            == DOC_TAG_NAME
        && psiInlineDocTag.getLastChild().getNode().getElementType() == DOC_INLINE_TAG_END;
  }

  /** Check if {@code element} is JavaDoc element */
  public static boolean isJavaDocElement(@NotNull PsiElement element) {
    return (getRootDocComment(element) != null);
  }

  @Nullable
  private static PsiDocComment getRootDocComment(@NotNull PsiElement element) {
    PsiElement parent = element.getParent();
    while (parent != null && !(parent instanceof PsiFile)) {
      if (parent instanceof PsiDocComment) return (PsiDocComment) parent;
      parent = parent.getParent();
    }
    return null;
  }

  /** Check if {@code element} inside group of folded (collapsed) regions */
  public static boolean isFolded(@NotNull PsiElement element) {
    Document document = element.getContainingFile().getViewProvider().getDocument();
    if (document == null)
      throw new RuntimeException("Document for " + element.toString() + " is NULL.");
    Editor[] editors = EditorFactory.getInstance().getEditors(document);
    for (Editor editor : editors) {
      for (FoldRegion foldRegion : editor.getFoldingModel().getAllFoldRegions()) {
        FoldingGroup foldingGroup = foldRegion.getGroup();
        PsiDocComment rootDocComment = getRootDocComment(element);
        if (foldingGroup != null
            && rootDocComment != null
            && foldingGroup.toString().equals("JDCR " + rootDocComment.getTokenType().toString())) {
          return !foldRegion.isExpanded();
        }
      }
    }
    return true;
  }

  /**
   * Look inside {@code range} in JavaDoc {@code element} for line breaks
   *
   * @param element javadoc PsiElement
   * @param range inside <tt>element</tt> to check
   * @return List of TextRanges <b>relative</b> to {@code element} between line breaks inside {@code
   *     range}
   */
  public static List<TextRange> excludeLineBreaks(
      @NotNull PsiElement element, @NotNull TextRange range) {
    List<TextRange> result = new ArrayList<>();
    int prevLineBreak = range.getStartOffset();
    // todo: fetch only children inside the range.
    for (PsiElement child : element.getChildren()) {
      if (range.intersectsStrict(getTextRangeInParent(child))) {
        if (child instanceof PsiWhiteSpace
            && child.getNextSibling() != null
            && child.getNextSibling().getNode().getElementType()
                == JavaDocTokenType.DOC_COMMENT_LEADING_ASTERISKS) {
          // new line PsiWhiteSpace element found
          PsiElement ws = child;
          PsiElement la = ws.getNextSibling();
          if (ws.getStartOffsetInParent() > prevLineBreak) {
            result.add(new TextRange(prevLineBreak, ws.getStartOffsetInParent()));
          }
          prevLineBreak = la.getStartOffsetInParent() + la.getTextLength() /*length of '*'s */;
        } else if (child instanceof PsiDocTag) {
          // break nested elements
          /*
                    Deque<TextRange> subElementRanges =
                        excludeLineBreaks(child, new TextRange(0, child.getTextLength())).stream()
                            .map(nestedRange -> nestedRange.shiftRight(child.getStartOffsetInParent()))
                            .collect(Collectors.toCollection(ArrayDeque::new));
          */
          final List<TextRange> nestedRanges =
              excludeLineBreaks(child, new TextRange(0, child.getTextLength()));
          Deque<TextRange> subElementRanges = new ArrayDeque<>(nestedRanges.size());
          for (TextRange nestedRange : nestedRanges) {
            subElementRanges.add(nestedRange.shiftRight(child.getStartOffsetInParent()));
          }
          if (!subElementRanges.isEmpty()) {
            int subElementRangesStart = subElementRanges.getFirst().getStartOffset();
            if (prevLineBreak < subElementRangesStart) { // ...<b>FOR_THAT_PART{@link test1}...
              subElementRanges.addFirst(new TextRange(prevLineBreak, subElementRangesStart));
            }
            result.addAll(subElementRanges);
            prevLineBreak = subElementRanges.getLast().getEndOffset();
          }
        }
      }
    }
    if (prevLineBreak < range.getEndOffset()) {
      result.add(new TextRange(prevLineBreak, range.getEndOffset()));
    }
    return removeLeadingSpace(element, result);
  }

  // don't include ' ' at the begging of line (after leading asterisks) if any.
  private static List<TextRange> removeLeadingSpace(
      @NotNull PsiElement element, @NotNull Collection<TextRange> ranges) {
    List<TextRange> result = new ArrayList<>(ranges.size());
    for (TextRange range : ranges) {
      if (range.substring(element.getText()).charAt(0) == ' '
          && element.getText().charAt(range.getStartOffset() - 1) == '*') {
        if (range.getLength() > 1) { // hack to avoid 0 length TextRange
          result.add(new TextRange(range.getStartOffset() + 1, range.getEndOffset()));
        }
      } else result.add(range);
    }
    return result;
  }

  private static final List<TextRange> EMPTY_LIST = Collections.emptyList();

  /**
   * Check element for incomplete HTML Tag <b>end</b> (lonely `{@code >}`) and look behind for
   * incomplete HTML Tag <b>start</b> (lonely `{@code <}`) by parsing previous Siblings.
   *
   * @param element element to check
   * @return {@link TextRange}s (<i>relatively</i> to {@code element.getParent()} element) of full
   *     multiline HTML Tag, excluding service elements (leading asterisks, etc).
   */
  public static List<TextRange> getMultiLineTagRangesInParent(@NotNull PsiElement element) {
    TextRange incompleteHtmlTagEnd = JdcrStringUtils.getIncompleteHtmlTagEnd(element.getText());
    if (incompleteHtmlTagEnd != null) {
      Deque<TextRange> foundRangesInParent = new ArrayDeque<>();
      foundRangesInParent.add(incompleteHtmlTagEnd.shiftRight(element.getStartOffsetInParent()));

      // Look behind for tag start.
      PsiElement prevSibling = element.getPrevSibling();
      while (prevSibling != null) {
        if (prevSibling.getNode().getElementType() == JavaDocTokenType.DOC_COMMENT_DATA
            || prevSibling instanceof PsiInlineDocTag) {
          TextRange incompleteHtmlTagStart =
              JdcrStringUtils.getIncompleteHtmlTagStart(prevSibling.getText());
          if (incompleteHtmlTagStart == null) {
            foundRangesInParent.addFirst(getTextRangeInParent(prevSibling));
          } else {
            foundRangesInParent.addFirst(
                incompleteHtmlTagStart.shiftRight(prevSibling.getStartOffsetInParent()));
            return removeLeadingSpace(element.getParent(), foundRangesInParent);
          }
        }
        prevSibling = prevSibling.getPrevSibling();
      }
    }
    return EMPTY_LIST;
  }

  /**
   * @return text range of element relative to its parent
   * @see PsiElement#getTextRangeInParent() - make it avaliable before 2018.3
   */
  @NotNull
  private static TextRange getTextRangeInParent(@NotNull PsiElement element) {
    return TextRange.from(element.getStartOffsetInParent(), element.getTextLength());
  }

  /**
   * Recursive (depth first) search for all elements of given {@code class}.
   *
   * <p>Don't use it. Use {@link com.intellij.psi.util.PsiTreeUtil#findChildrenOfType(PsiElement,
   * Class)}.
   *
   * @param element a PSI element to start search from.
   * @param clazz element type to search for.
   * @param <T> type to cast found elements to.
   * @return {@code List<T>} of all found elements, or empty {@code List<T>} if nothing found.
   */
  @NotNull
  public static <T extends PsiElement> List<T> myFindChildrenOfType(
      @Nullable PsiElement element, @NotNull Class<? extends T> clazz) {
    List<T> result = new ArrayList<>();
    if (element != null) {
      doFindChildrenOfType(element, clazz, result);
    }
    return result;
  }

  private static <T extends PsiElement> void doFindChildrenOfType(
      @NotNull PsiElement element, @NotNull Class<? extends T> clazz, @NotNull List<T> result) {
    for (PsiElement child : element.getChildren()) {
      if (clazz.isInstance(child)) {
        result.add(clazz.cast(child));
      }
      doFindChildrenOfType(child, clazz, result);
    }
  }
}
