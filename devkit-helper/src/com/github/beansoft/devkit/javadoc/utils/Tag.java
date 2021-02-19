package com.github.beansoft.devkit.javadoc.utils;

import org.jetbrains.annotations.NotNull;

public class Tag {

  @NotNull private String open;
  @NotNull private String close;

  public Tag(@NotNull String openTag, @NotNull String closeTag) {
    this.open = removeWS(openTag);
    this.close = removeWS(closeTag);
  }

  public boolean openIn(@NotNull String text){
    return removeWS(text).contains(this.open);
  }

  public boolean closeIn(@NotNull String text){
    return removeWS(text).contains(this.close);
  }

  private static final char ws = ' ';

  /** Remove spaces from given string. Optimised copy of Apache remove() */
  @NotNull
  private String removeWS(@NotNull String text){
    int index = text.indexOf(ws);
    if (index < 0) {
      return text;
    }
    final char[] chars = text.toCharArray();
    int pos = index;
    for (int i = index; i < chars.length; i++) {
      if (chars[i] != ws) {
        chars[pos++] = chars[i];
      }
    }
    return (new String(chars, 0, pos));  }
}
