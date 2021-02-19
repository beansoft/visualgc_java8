package com.github.beansoft.devkit;

import com.intellij.AbstractBundle;
import com.intellij.BundleBase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

public final class DevkitHelperBundle {
  @NonNls public static final String BUNDLE = "messages.DevkitHelperBundle";

  private DevkitHelperBundle() {
  }

  private static Reference<ResourceBundle> ourBundle;

  @NotNull
  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
    return AbstractBundle.message(getBundle(), key, params);// 修正 by BeanSoft 去掉旧API调用
  }

  private static ResourceBundle getBundle() {
    ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(ourBundle);
    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE);
      ourBundle = new SoftReference<>(bundle);
    }
    return bundle;
  }

}
