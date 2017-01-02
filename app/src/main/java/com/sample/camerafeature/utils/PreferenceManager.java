package com.sample.camerafeature.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static String NAME = "preferences";
    public static String KEY_USE_CAMERA2 = "key_use_camera2";

    private SharedPreferences mSettingPreference;

    public PreferenceManager(Context context) {
        mSettingPreference = context.getSharedPreferences(PreferenceManager.NAME, Context.MODE_PRIVATE);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        if (mSettingPreference == null) return false;
        return mSettingPreference.getBoolean(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        if (mSettingPreference == null) return;
        SharedPreferences.Editor editor = mSettingPreference.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        if (mSettingPreference == null) return;
        mSettingPreference.registerOnSharedPreferenceChangeListener(listener);
    }
}
