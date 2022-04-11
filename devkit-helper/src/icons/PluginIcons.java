package icons;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PluginIcons {
    public static final Icon Gutter_Plugin = load("/icons/gutter/plugin.svg");

    @NotNull private static Icon load(String path) {
        try {
            return IconLoader.getIcon(path, PluginIcons.class);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public static @NotNull Icon scaleIconToSize(Icon icon, int size) {
        int width = icon.getIconWidth();
        if (width == size) return icon;

        float scale = size / (float)width;
        icon = IconUtil.scale(icon, null, scale);
        return icon;
    }
    /**
     * see https://github.com/flutter/flutter-intellij/issues/4937
     * I've found the reason, the Jetbrains' developer had removed this method overlayIcons(javax.swing.Icon[]) from latest IJ CE code in ElementBase.java, but the LayeredIcon.java stays untouched.
     * The git log said:
     * platform.core.impl — remove dependency on intellij.platform.util.ui and intellij.platform.core.ui
     * java.psi.iml — remove dependency on intellij.platform.core.ui
     * @param icons
     * @return
     */
    @NotNull
    public static Icon overlayIcons(@NotNull Icon  ... icons) {
        final LayeredIcon icon = new LayeredIcon(icons.length);
        int i = 0;
        for (Icon ic : icons) {
            icon.setIcon(ic, i++);
        }
        return icon;
    }
}
