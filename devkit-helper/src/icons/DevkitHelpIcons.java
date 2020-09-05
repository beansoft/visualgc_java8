package icons;

import com.intellij.icons.AllIcons;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DevkitHelpIcons {
    public static Icon XmlFile_12 = scaleIconToSize(AllIcons.FileTypes.Xml, 12);

    public static @NotNull
    Icon scaleIconToSize(Icon icon, int size) {
        int width = icon.getIconWidth();
        if (width == size) return icon;

        float scale = size / (float)width;
        icon = IconUtil.scale(icon, null, scale);
        return icon;
    }
}
