package com.github.beansoft.devkit.javadoc.utils;

import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JdcrStringUtils {
  public static final Set<String> CODE_TAGS = new HashSet<>(Arrays.asList("code","literal"));
  public static final Set<String> LINK_TAGS = new HashSet<>(Arrays.asList("link","linkplain"));

  private static final String T_START = "<(?!!--)"; // excluding Html comment beginning <!--
  private static final String T_END = "(?<!--)>"; // excluding Html comment ending -->
  private static final String T_BODY = "[^<>]+";
  private static final Pattern HTML_TAG = Pattern.compile(T_START + T_BODY + T_END);
  private static final Pattern HTML_INCOMPLETE_TAG_START = Pattern.compile(T_START + T_BODY);
  private static final Pattern HTML_INCOMPLETE_TAG_END = Pattern.compile(T_BODY + T_END);
  private static final Pattern HTML_ESC_CHAR = Pattern.compile("&#?[a-zA-Z0-9]+;");

  /**
   * Parse given text to find HTML tags
   *
   * @param text given text
   * @return list of TextRange of HTML tags inside text
   */
  @NotNull
  public static List<TextRange> getHtmlTags(String text) {
    return getElementsInText(text, HTML_TAG);
  }

  /**
   * Parse given text to find Start of <b>incomplete</b> HTML tag
   *
   * @param text given text
   * @return TextRange of incomplete HTML tag inside text
   */
  @Nullable
  public static TextRange getIncompleteHtmlTagStart(String text) {
    return getElementsInText(text, HTML_INCOMPLETE_TAG_START)
        .stream()
        .filter(range -> range.getEndOffset() == text.length())
        .findAny()
        .orElse(null);
  }

  /**
   * Parse given text to find End of <b>incomplete</b> HTML tag
   *
   * @param text given text
   * @return TextRange of incomplete HTML tag inside text
   */
  @Nullable
  public static TextRange getIncompleteHtmlTagEnd(@NotNull String text) {
    // Pre-filtering. See http://www.fasterj.com/articles/regex2.shtml
    // todo: Rid of regexp at all?
    int tagEndIndex = text.indexOf('>');
    if (tagEndIndex != -1) {
      int tagStartIndex = text.indexOf('<');
      if (tagStartIndex == -1 || tagStartIndex > tagEndIndex) {

        return getElementsInText(text, HTML_INCOMPLETE_TAG_END)
            .stream()
            .filter(range -> range.getStartOffset() == 0)
            .findAny()
            .orElse(null);
        //        return new TextRange(0, tagEndIndex);
      }
    }
    return null;
  }

  @NotNull
  private static List<TextRange> getCombinedElementsInText(String text, @NotNull Pattern pattern) {
    Stack<TextRange> result = new Stack<>();
    Matcher matcher = pattern.matcher(text);
    int start;
    while (matcher.find()) {
      start =
          (!(result.empty()) && result.peek().getEndOffset() == matcher.start())
              ? result.pop().getStartOffset()
              : matcher.start();
      result.push(new TextRange(start, matcher.end()));
    }
    return result;
  }

  /**
   * Parse given text to find HTML escaped chars, such as "&nbsp"
   *
   * @param text given text
   * @return list of TextRange of HTML escaped chars inside text
   */
  @NotNull
  public static List<TextRange> getHtmlEscapedChars(String text) {
    return getElementsInText(text, HTML_ESC_CHAR);
  }

  @NotNull
  private static List<TextRange> getElementsInText(String text, @NotNull Pattern pattern) {
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) {
      List<TextRange> result = new ArrayList<>();
      do {
        assert matcher.start() < matcher.end()
            : "matcher.start()="
                + matcher.start()
                + " matcher.end()="
                + matcher.end()
                + "  text: "
                + text
                + "pattern: "
                + matcher.pattern().pattern();
        result.add(new TextRange(matcher.start(), matcher.end()));
      } while (matcher.find());
      return result;
    }
    return EMPTY_ARRAY;
  }

  private static final int EMPTY_INDEX = -2;
  private static final List<TextRange> EMPTY_ARRAY = Collections.emptyList();;

  /**
   * Parse given text to find given HTML tags
   *
   * @param text text to parse
   * @param tag HTML tag to search for
   * @return list of TextRange of HTML tag's values inside text if any or {@link #EMPTY_ARRAY}. If
   *     open or close tag not found: Range to the end / from beginning of {@code text} added (to
   *     search in "upper" method for close/open tag in siblings).
   */
  @NotNull
  public static List<TextRange> getValuesOfTag(@NotNull String text, @NotNull Tag tag) {
    return getValuesOfTag(text, tag, getHtmlTags(text));
  }

  /**
   * @see {@link #getValuesOfTag(String, Tag)}
   * @param foundHtmlTags pre-fetched ranges of html Tags in {@code text}
   */
  @NotNull
  public static List<TextRange> getValuesOfTag(
      @NotNull String text, @NotNull Tag tag, @NotNull List<TextRange> foundHtmlTags) {
    if (foundHtmlTags.isEmpty()) {
      return EMPTY_ARRAY;
    }
    List<TextRange> result = new ArrayList<>();
    int start = EMPTY_INDEX, end = EMPTY_INDEX;
    for (TextRange textRange : foundHtmlTags) {
      if (tag.openIn(textRange.substring(text))) {
        start = textRange.getEndOffset();
      }
      if (tag.closeIn(textRange.substring(text))) {
        end = textRange.getStartOffset();
      }
      if (start != EMPTY_INDEX && end != EMPTY_INDEX && start < end) {
        //        assert start <= end : "Start="+start+" End="+end+" at: "+element.getText();
        result.add(new TextRange(start, end));
        start = EMPTY_INDEX;
        end = EMPTY_INDEX;
      }
      // possible end of multiline tag
      if (end != EMPTY_INDEX && start == EMPTY_INDEX) {
        result.add(new TextRange(0, end));
        end = EMPTY_INDEX;
      }
    }
    // possible start of multiline tag
    if (start != EMPTY_INDEX && end == EMPTY_INDEX) {
      result.add(new TextRange(start, text.length()));
    }
    return result;
  }
}
