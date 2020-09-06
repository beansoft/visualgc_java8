package com.github.beansoft.devkit;

import com.intellij.BundleBase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

public final class DevkitHelperBundle extends BundleBase {
  @NonNls public static final String BUNDLE = "messages.DevkitHelperBundle";
  private static ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE);

  private DevkitHelperBundle() {
  }

  @NotNull
  public static String message(@NotNull String key, @NotNull Object  ... params) {
    return message(bundle, key, params);
  }

}
