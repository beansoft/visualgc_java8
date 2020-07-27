package com.sun.jvmstat.tools.visualgc.resource;

import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Use for Spark Internationalization.
 *
 * @author Derek DeMoro
 */
public class Res {
    private static PropertyResourceBundle prb;

    private Res() {

    }

    static ClassLoader cl = Res.class.getClassLoader();

    static {
        prb = (PropertyResourceBundle)ResourceBundle.getBundle("visualgc");
    }

    public static String getString(String propertyName) {
        try {
            return prb.getString(propertyName);
        }
        catch (Exception e) {
            e.printStackTrace();
            return propertyName;
        }

    }

    public static String getString(String propertyName, Object... obj) {
        String str = prb.getString(propertyName);
        if (str == null) {
            return propertyName;
        }


        return MessageFormat.format(str, obj);
    }

    public static PropertyResourceBundle getBundle() {
        return prb;
    }
}
