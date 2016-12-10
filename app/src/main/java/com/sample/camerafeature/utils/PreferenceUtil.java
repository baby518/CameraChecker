package com.sample.camerafeature.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtil {
    public static String NAME = "preferences";
    public static String KEY_USE_CAMERA2 = "key_use_camera2";

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(PreferenceUtil.NAME, Context.MODE_PRIVATE);
        return settings.getBoolean(key, defaultValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(PreferenceUtil.NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
}
