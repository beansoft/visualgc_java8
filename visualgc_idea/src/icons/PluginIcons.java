package icons;


import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class PluginIcons {
    public static final Icon visualgcToolWindow = load("/visualgc.svg");
    private static Icon load(String path) {
        try {
            return IconLoader.getIcon(path, PluginIcons.class);
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
