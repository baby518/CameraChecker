package com.sample.camerafeature.fragment;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;

import com.sample.camerafeature.utils.FeatureChecker;

public class OverviewCapabilitiesContent extends BaseCapabilitiesContent {
    private boolean mConfigCameraSoundForced;
    private String mNetworkOperator;

    public OverviewCapabilitiesContent(Context context) {
        mConfigCameraSoundForced = getConfigCameraSoundForced(context);
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mNetworkOperator = telManager.getNetworkOperator();
    }

    public void generateCapabilities() {
        addItem(new BaseCapabilitiesContent.CapabilitiesItem("Hardware", FeatureChecker.getHardware()));

        addItem(new BaseCapabilitiesContent.CapabilitiesItem("Board", FeatureChecker.getHardwareBoard() + ", " + (FeatureChecker.is64bit() ? "64 bit" : "32 bit")));
        addItem(new BaseCapabilitiesContent.CapabilitiesItem("Manufacturer", FeatureChecker.getHardwareManufacturer()));
        addItem(new BaseCapabilitiesContent.CapabilitiesItem("Brand", FeatureChecker.getHardwareBrand()));
        addItem(new BaseCapabilitiesContent.CapabilitiesItem("Product", FeatureChecker.getHardwareProduct()));
        addItem(new BaseCapabilitiesContent.CapabilitiesItem("Device", FeatureChecker.getHardwareDevice()));
        addItem(new BaseCapabilitiesContent.CapabilitiesItem("Model", FeatureChecker.getHardwareModel()));
        addItem(new BaseCapabilitiesContent.CapabilitiesItem("Android Version", FeatureChecker.getReleaseVersion() + ", API " + FeatureChecker.getReleaseSDK() + ", " + FeatureChecker.getBuildType()));
        addItem(new BaseCapabilitiesContent.CapabilitiesItem("Screen Resolution", FeatureChecker.getScreenResolution()));
        addItem(new BaseCapabilitiesContent.CapabilitiesItem("Screen Density", FeatureChecker.getScreenDensityDpi() + "dpi, " + FeatureChecker.getScreenDensity() + "x"));
        addItem(new BaseCapabilitiesContent.CapabilitiesItem("Resources folder", FeatureChecker.getResourcesFolder()));
        addItem(new BaseCapabilitiesContent.CapabilitiesItem("camera2.portability.force_api", SystemProperties.get("camera2.portability.force_api", "N/A")));
        addItem(new BaseCapabilitiesContent.CapabilitiesItem("audio.camerasound.force", SystemProperties.get("audio.camerasound.force", "N/A")));
        addItem(new BaseCapabilitiesContent.CapabilitiesItem("config_camera_sound_forced", Boolean.toString(this.mConfigCameraSoundForced)));
        addItem(new BaseCapabilitiesContent.CapabilitiesItem("MCC+MNC", this.mNetworkOperator));
    }

    public boolean getConfigCameraSoundForced(Context context) {
        return context.getResources()
                .getBoolean(com.android.internal.R.bool.config_camera_sound_forced);
    }
}
