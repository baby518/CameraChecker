package com.sample.feature.utils;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.sample.common.utils.SystemProperties;
import com.sample.feature.R;

public class FeatureChecker {
    private static float sDensity;
    private static int sDensityDpi;
    private static boolean sInitialized = false;
    private static String sResFolderCheck;
    private static int sScreenWidthLong;
    private static int sScreenWidthShort;
    private static int sScreenRealWidthLong;
    private static int sScreenRealWidthShort;

    public static String getBuildType() {
        return Build.TYPE;
    }

    public static String getHardware() {
        return Build.HARDWARE;
    }

    public static String getPlatform() {
        return SystemProperties.get("ro.board.platform", Build.UNKNOWN);
    }

    public static String getHardwareBoard() {
        return Build.BOARD;
    }

    public static String getHardwareBrand() {
        return Build.BRAND;
    }

    public static String getHardwareDevice() {
        return Build.DEVICE;
    }

    public static String getHardwareManufacturer() {
        return Build.MANUFACTURER;
    }

    public static String getHardwareModel() {
        return Build.MODEL;
    }

    public static String getHardwareProduct() {
        return Build.PRODUCT;
    }

    public static int getReleaseSDK() {
        return Build.VERSION.SDK_INT;
    }

    public static String getReleaseVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getResourcesFolder() {
        return sResFolderCheck;
    }

    public static float getScreenDensity() {
        return sDensity;
    }

    public static int getScreenDensityDpi() {
        return sDensityDpi;
    }

    public static String getScreenResolution() {
        if (sScreenRealWidthLong == sScreenWidthLong && sScreenRealWidthShort == sScreenWidthShort) {
            return sScreenWidthLong + "x" + sScreenWidthShort;
        } else {
            return sScreenWidthLong + "x" + sScreenWidthShort + "," + sScreenRealWidthLong + "x" + sScreenRealWidthShort;
        }
    }

    public static String[] getAbiList() {
        return Build.SUPPORTED_ABIS;
    }

    public static void initialize(Context context) {
        if (isInitialized()) return;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        sScreenWidthLong = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
        sScreenWidthShort = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(realDisplayMetrics);
        sDensity = realDisplayMetrics.density;
        sDensityDpi = realDisplayMetrics.densityDpi;
        sScreenRealWidthLong = Math.max(realDisplayMetrics.widthPixels, realDisplayMetrics.heightPixels);
        sScreenRealWidthShort = Math.min(realDisplayMetrics.widthPixels, realDisplayMetrics.heightPixels);
        sResFolderCheck = context.getString(R.string.res_folder_check);
        sInitialized = true;
    }

    private static boolean isInitialized() {
        return sInitialized;
    }
}
