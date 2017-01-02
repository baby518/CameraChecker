package com.sample.camerafeature.fragment;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;

public class Camera2Capabilities extends BaseCapabilities {
    private String mCameraId;
    private CameraCharacteristics mCharacteristics;
    private CameraManager mCameraManager;

    public Camera2Capabilities(Context context, int cameraId) {
        mCameraId = Integer.toString(cameraId);
        initCameraManager(context);
        clearCameraCapabilities();
        generateCapabilities();
    }

    private void initCameraManager(Context context) {
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            mCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void generateCapabilities() {
        addItem(new CapabilitiesItem("CameraFacing", getCameraFacing()));
    }

    private String getCameraFacing() {
        if (mCharacteristics != null) {
            int facing = mCharacteristics.get(CameraCharacteristics.LENS_FACING);
            if (facing == CameraMetadata.LENS_FACING_FRONT) {
                return "Front";
            } else if (facing == CameraMetadata.LENS_FACING_BACK) {
                return "Back";
            } else if (facing == CameraMetadata.LENS_FACING_EXTERNAL) {
                return "External";
            }
        }
        return UNKNOWN;
    }
}
