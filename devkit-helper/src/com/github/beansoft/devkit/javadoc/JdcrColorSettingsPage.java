package com.github.beansoft.devkit.javadoc;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.java.JavaSyntaxHighlighterFactory;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;

public class JdcrColorSettingsPage implements ColorSettingsPage {
  public static final TextAttributesKey CODE_TAG =
      TextAttributesKey.createTextAttributesKey(
          "code_tag", DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP);
  public static final TextAttributesKey LINK_TAG =
      TextAttributesKey.createTextAttributesKey(
          "link_tag", DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final TextAttributesKey HTML_LINK_TAG =
      TextAttributesKey.createTextAttributesKey(
          "html_link_tag", DefaultLanguageHighlighterColors.NUMBER);

  public static final TextAttributesKey BOLD_FONT =
      TextAttributesKey.createTextAttributesKey("MY_BOLD");
  public static final TextAttributesKey ITALIC_FONT =
      TextAttributesKey.createTextAttributesKey("MY_ITALIC");
  public static final TextAttributesKey BORDERED =
      TextAttributesKey.createTextAttributesKey("MY_BORDERED");

  private static final AttributesDescriptor[] DESCRIPTORS =
      new AttributesDescriptor[] {
        new AttributesDescriptor("Tag value of: <code> | <tt> | @code | <pre>", CODE_TAG),
        new AttributesDescriptor("Tag value of html link: <a href=...>...</a>", HTML_LINK_TAG),
        new AttributesDescriptor("Tag value of: @link", LINK_TAG),
        new AttributesDescriptor("Tag value of: <b> | <a name=...>", BOLD_FONT),
        new AttributesDescriptor("Tag value of: <i> | <em>", ITALIC_FONT),
        new AttributesDescriptor("Html / Javadoc Tags and Escaped Chars additional Mark Up", BORDERED)
      };

  @Nullable
  @Override
  public Icon getIcon() {
    return null;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return JavaSyntaxHighlighterFactory.getSyntaxHighlighter(JavaLanguage.INSTANCE, null, null);
  }

  @NotNull
  @Override
  public String getDemoText() {
    return "/**\n"
        + " * To convert any <_bor><tt></_bor><_code>object</_code><_bor></tt></_bor> of "
        + "{@code <_code>Object</_code>} class to "
        + "<_bor><code></_bor><_code>String</_code><_bor></code></_bor> use \n"
        + " * {@link java.lang.Object#<_link>toString()</_link> toString()} method.\n"
        + " * html link <_bor><a href=\"http://www.jetbrains.org\"></_bor>"
        + "<_a>JetBrains</_a><_bor></a></_bor>.\n"
        + " * <_bor><b></_bor><_b>bold text</_b><_bor></b></_bor> "
        + "and <_bor><i></_bor><_i>italic text</_i><_bor></i></_bor>.\n"
        + " */";
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ContainerUtil.newHashMap(
        Arrays.asList("_code", "_link", "_a", "_b", "_i", "_bor"),
        Arrays.asList(CODE_TAG, LINK_TAG, HTML_LINK_TAG, BOLD_FONT, ITALIC_FONT, BORDERED));
  }

  @NotNull
  @Override
  public AttributesDescriptor[] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @NotNull
  @Override
  public ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "JavaDoc Read";
  }
}
