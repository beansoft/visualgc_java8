package icons;


import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.ScreenUtil;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PluginIcons {
    public static final Icon visualgcToolWindow = load("/visualgc.svg");


    public static Icon RunVisualGC = load("/images/runWithVisualGC.svg");
    public static Icon RunVisualGC_13 = load("/images/runWithVisualGC_13.svg");
    public static Icon DebugVisualGC = load("/images/debugWithVisualGC.svg");
    public static Icon DebugVisualGC_13 = load("/images/debugWithVisualGC_13.svg");

    private static Icon load(String path) {
        try {
            return IconLoader.getIcon(path, PluginIcons.class);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public static @NotNull
    Icon scaleIconToSize(Icon icon, int size) {
        int width = icon.getIconWidth();
        if (width == size) return icon;

        float scale = size / (float)width;
        icon = IconUtil.scale(icon, null, scale);
        return icon;
    }

//    private static Icon androidLoad(String path) {
//        return load(path, AndroidIcons.class);
//    }

//    private static Icon load(String path) {
//        return load(path, AllIcons.class);
//    }

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
