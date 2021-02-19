package com.github.beansoft.devkit.javadoc;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

public class JavaDocBundle {

  private static Reference<ResourceBundle> ourBundle;
  @NonNls private static final String BUNDLE = "messages.JavaDocBundle";

  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
    return AbstractBundle.message(getBundle(), key, params);// 修正 by BeanSoft 去掉旧API调用
  }

  public static boolean containsKey(String key) {
    return getBundle().containsKey(key);
  }

  private JavaDocBundle() {
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
