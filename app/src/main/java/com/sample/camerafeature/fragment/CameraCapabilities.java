package com.sample.camerafeature.fragment;

import android.hardware.Camera;

public class CameraCapabilities extends BaseCapabilities {
    private int mCameraId;

    public CameraCapabilities(int cameraId) {
        mCameraId = cameraId;

        clearCameraCapabilities();
        generateCapabilities();
    }

    @Override
    protected void generateCapabilities() {
        addItem(new CapabilitiesItem("CameraFacing", getCameraFacing()));
//        addItem(new CapabilitiesItem("HardwareLevel", getCameraHardwareLevel()));
//        addItem(new CapabilitiesItem("Capabilities", getSupportCapabilities()));
//        addItem(new CapabilitiesItem("sensor.orientation", getSensorOrientation()));
//        addItem(new CapabilitiesItem("flash.info.available", isFlashAvailable()));
//        addItem(new CapabilitiesItem("FocalLengths", getSupportFocalLengths()));
//        addItem(new CapabilitiesItem("MinimumFocalDistance", getMinFocalDistance()));
//        addItem(new CapabilitiesItem("Apertures", getSupportApertures()));
//        addItem(new CapabilitiesItem("OpticalStabilization", getSupportLensOISMode()));
//        addItem(new CapabilitiesItem("VideoStabilization", getSupportVideoStabilization()));
//        addItem(new CapabilitiesItem("PhotoSize-Jpeg", getSupportJpegPictureSize()));
//        addItem(new CapabilitiesItem("PreviewSize", getSupportPreviewSize()));
//        addItem(new CapabilitiesItem("VideoSize", getSupportVideoSize()));
//        addItem(new CapabilitiesItem("MaxZoom", getMaxDigitalZoom()));
//        addItem(new CapabilitiesItem("SceneModes", getSupportSceneMode()));
//        addItem(new CapabilitiesItem("ExposureCompensation", getSupportExposureCompensation()));
//        addItem(new CapabilitiesItem("AWBModes", getSupportAwbMode()));
//        addItem(new CapabilitiesItem("AFModes", getSupportAFMode()));
//        addItem(new CapabilitiesItem("AEModes", getSupportAEMode()));
//        addItem(new CapabilitiesItem("ColorEffects", getSupportEffects()));
//        addItem(new CapabilitiesItem("MaxFrameDuration", getMaxFrameDuration()));
//        addItem(new CapabilitiesItem("Antibanding", getSupportAntibanding()));
//        addItem(new CapabilitiesItem("ISO Range", getISORange()));
//        addItem(new CapabilitiesItem("ExposureTime range", getExposureTimeRange()));
//        addItem(new CapabilitiesItem("FaceDetectModes", getSupportFaceDetectModes()));
//        addItem(new CapabilitiesItem("MaxFaceDetectCount", getMaxFaceDetectCount()));
    }

    private String getCameraFacing() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            return "Back";
        } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return "Front";
        }
        return UNKNOWN;
    }
}
