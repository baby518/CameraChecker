package com.sample.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SystemProperties {
    private static Method mGetMethod;

    static {
        try {
            final Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            mGetMethod = systemProperties.getMethod("get", String.class, String.class);
        } catch (Exception e) {
            // This should never happen
            e.printStackTrace();
        }
    }

    /**
     * Gets system properties set by <code>adb shell setprop <em>key</em> <em>value</em></code>
     *
     * @param key the property key.
     * @param defaultValue the value to return if the property is undefined or empty (this parameter
     *            may be {@code null}).
     * @return the system property value or the default value.
     */
    public static String get(String key, String defaultValue) {
        if (mGetMethod != null) {
            try {
                return (String) mGetMethod.invoke(null, key, defaultValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return defaultValue;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private SystemProperties() {
    }
}
