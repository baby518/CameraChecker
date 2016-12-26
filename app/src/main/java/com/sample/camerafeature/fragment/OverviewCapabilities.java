package com.sample.camerafeature.fragment;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;

import com.sample.camerafeature.utils.FeatureChecker;

public class OverviewCapabilities extends BaseCapabilities {
    private boolean mConfigCameraSoundForced;
    private String mNetworkOperator;

    public OverviewCapabilities(Context context) {
        mConfigCameraSoundForced = getConfigCameraSoundForced(context);
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mNetworkOperator = telManager.getNetworkOperator();

        clearCameraCapabilities();
        generateCapabilities();
    }

    @Override
    protected void generateCapabilities() {
        addItem(new CapabilitiesItem("Hardware", FeatureChecker.getHardware()));
        addItem(new CapabilitiesItem("Board", FeatureChecker.getHardwareBoard() + ", " + (FeatureChecker.is64bit() ? "64 bit" : "32 bit")));
        addItem(new CapabilitiesItem("Manufacturer", FeatureChecker.getHardwareManufacturer()));
        addItem(new CapabilitiesItem("Brand", FeatureChecker.getHardwareBrand()));
        addItem(new CapabilitiesItem("Product", FeatureChecker.getHardwareProduct()));
        addItem(new CapabilitiesItem("Device", FeatureChecker.getHardwareDevice()));
        addItem(new CapabilitiesItem("Model", FeatureChecker.getHardwareModel()));
        addItem(new CapabilitiesItem("Android Version", FeatureChecker.getReleaseVersion() + ", API " + FeatureChecker.getReleaseSDK() + ", " + FeatureChecker.getBuildType()));
        addItem(new CapabilitiesItem("Screen Resolution", FeatureChecker.getScreenResolution()));
        addItem(new CapabilitiesItem("Screen Density", FeatureChecker.getScreenDensityDpi() + "dpi, " + FeatureChecker.getScreenDensity() + "x"));
        addItem(new CapabilitiesItem("Resources folder", FeatureChecker.getResourcesFolder()));
        addItem(new CapabilitiesItem("camera2.portability.force_api", SystemProperties.get("camera2.portability.force_api", "N/A")));
        addItem(new CapabilitiesItem("audio.camerasound.force", SystemProperties.get("audio.camerasound.force", "N/A")));
        addItem(new CapabilitiesItem("config_camera_sound_forced", Boolean.toString(this.mConfigCameraSoundForced)));
        addItem(new CapabilitiesItem("MCC+MNC", this.mNetworkOperator));
    }

    private boolean getConfigCameraSoundForced(Context context) {
        return context.getResources()
                .getBoolean(com.android.internal.R.bool.config_camera_sound_forced);
    }
}
