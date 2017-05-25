package com.sample.camerafeature.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    public static final String SCOPE_GLOBAL = "default_scope";
    private static String DEFAULT_NAME = "preferences";
    public static String SCOPE_PREFIX = "preferences_";
    public static String KEY_USE_CAMERA2 = "key_use_camera2";

    private static SettingsManager mSettingsManager;

    private final Object mLock;
    private final Context mContext;
    private SharedPreferences mDefaultPreferences;

    public static SettingsManager getInstance(Context context) {
        if (mSettingsManager == null) {
            mSettingsManager = new SettingsManager(context);
        }
        return mSettingsManager;
    }

    private SettingsManager(Context context) {
        mLock = new Object();
        mContext = context;
        mDefaultPreferences = context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE);
    }

    public static String getCameraIdScope(int cameraId) {
        return SCOPE_PREFIX + cameraId;
    }

    private SharedPreferences getPreferencesFromScope(String scope) {
        synchronized (mLock) {
            if (scope.equals(SCOPE_GLOBAL)) {
                return mDefaultPreferences;
            }

            return openPreferences(scope);
        }
    }

    public SharedPreferences openPreferences(String scope) {
        synchronized (mLock) {
            SharedPreferences preferences;
            preferences = mContext.getSharedPreferences(scope, Context.MODE_PRIVATE);
            return preferences;
        }
    }

    public boolean getBoolean(String scope, String key, boolean defaultValue) {
        synchronized (mLock) {
            String defaultValueString = defaultValue ? "1" : "0";
            String value = getString(scope, key, defaultValueString);
            return convertToBoolean(value);
        }
    }

    // final
    public String getString(String scope, String key, String defaultValue) {
        synchronized (mLock) {
            SharedPreferences preferences = getPreferencesFromScope(scope);
            try {
                return preferences.getString(key, defaultValue);
            } catch (ClassCastException e) {
                preferences.edit().remove(key).apply();
                return defaultValue;
            }
        }
    }

    public void set(String scope, String key, boolean value) {
        synchronized (mLock) {
            set(scope, key, convert(value));
        }
    }

    // final
    public void set(String scope, String key, String value) {
        synchronized (mLock) {
            SharedPreferences preferences = getPreferencesFromScope(scope);
            preferences.edit().putString(key, value).apply();
        }
    }

    static boolean convertToBoolean(String value) {
        return Integer.parseInt(value) != 0;
    }

    static String convert(boolean value) {
        return value ? "1" : "0";
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        if (mDefaultPreferences == null) return;
        mDefaultPreferences.registerOnSharedPreferenceChangeListener(listener);
    }
}
