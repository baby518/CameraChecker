package com.sample.feature.fragment;

import android.content.Context;

import com.sample.preference.SettingsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseCameraCapabilities extends BaseCapabilities {
    private final SettingsManager mSettingsManager;
    private final String mSettingsScope;
    private static final String KEY_TITLE = "title";

    protected BaseCameraCapabilities(Context context, int cameraId) {
        mSettingsManager = SettingsManager.getInstance(context);
        mSettingsScope = SettingsManager.getCameraInfoScope(cameraId);
    }

    @Override
    protected void generateCapabilities() {
    }

    protected String getCameraDeviceInfoTitle() {
        if (mSettingsManager != null) {
            return mSettingsManager.openPreferences(mSettingsScope).getString(KEY_TITLE, null);
        }
        return null;
    }

    protected List<String> getCameraDeviceInfo() {
        List<String> result = new ArrayList<>();
        if (mSettingsManager != null) {
            Map<String, ?> map = mSettingsManager.openPreferences(mSettingsScope).getAll();
            if (map.size() > 0) {
                for (Map.Entry<String, ?> entry : map.entrySet()) {
                    if (entry.getKey().equals(KEY_TITLE)) continue;
                    result.add(entry.getKey() + ":" + entry.getValue());
                }
            }
        }
        return result;
    }
}
