package com.sample.feature.utils;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.sample.camerafeature.R;

import dalvik.system.VMRuntime;

public class FeatureChecker {
    private static float sDensity;
    private static int sDensityDpi;
    private static boolean sInitialized = false;
    private static String sResFolderCheck;
    private static int sScreenWidthLong;
    private static int sScreenWidthShort;

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
        return sScreenWidthLong + "x" + sScreenWidthShort;
    }

    public static boolean is64bit() {
        return VMRuntime.getRuntime().is64Bit();
    }

    public static void initialize(Context context) {
        if (isInitialized()) return;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        sDensity = displayMetrics.density;
        sDensityDpi = displayMetrics.densityDpi;
        sScreenWidthLong = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
        sScreenWidthShort = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        sResFolderCheck = context.getString(R.string.res_folder_check);
        sInitialized = true;
    }

    public static boolean isInitialized() {
        return sInitialized;
    }
}
