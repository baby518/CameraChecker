package com.sample.camerafeature.utils;

/** parse /proc/camerainfo file content. */
public class ProcCameraInfoParse {
    private static final String ID_PREFIX = "CAMERA_";
    private static final String KEY_TITLE = "title";

    public static void parseAndSave(SettingsManager settingsManager) {
        String info = Shell.exec("cat /proc/camerainfo");
        if (info == null) return;

        String[] resultArray = info.split("\n");
        if (resultArray == null) return;

        int cameraId = 0;
        for (String string : resultArray) {
            if (string.startsWith(ID_PREFIX)) {
                String idString = string.substring(ID_PREFIX.length(), ID_PREFIX.length() + 1);
                cameraId = Integer.parseInt(idString);
                saveTitle(cameraId, string, settingsManager);
            }

            saveValue(cameraId, string, settingsManager);
        }
    }

    private static void saveTitle(int cameraId, String value, SettingsManager settingsManager) {
        settingsManager.set(SettingsManager.getCameraIdScope(cameraId), KEY_TITLE, value);
    }

    private static void saveValue(int cameraId, String string, SettingsManager settingsManager) {
        if (string == null) return;
        if (!string.contains(":")) return;
        String[] resultArray = string.split(":");
        String key = resultArray[0];
        String value = resultArray[1];
        settingsManager.set(SettingsManager.getCameraIdScope(cameraId), key, value);
    }
}
