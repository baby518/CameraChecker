package com.sample.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.util.Size;

import com.sample.camera.settings.CameraSettings;
import com.sample.camera.utils.CameraUtil;
import com.sample.camerafeature.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class CameraFragment extends BaseCameraFragment {
    private static final String TAG = "CAM_FRAGMENT_API1";
    private Camera mCameraDevice;
    private Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
    private int mSensorOrientation = 0;
    private static final HashMap<CameraSettings.FocusMode, String> FOCUS_MODE = new HashMap<>();

    static {
        FOCUS_MODE.put(CameraSettings.FocusMode.FOCUS_MODE_AUTO, Camera.Parameters.FOCUS_MODE_AUTO);
        FOCUS_MODE.put(CameraSettings.FocusMode.FOCUS_MODE_CONTINUOUS_PICTURE, Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        FOCUS_MODE.put(CameraSettings.FocusMode.FOCUS_MODE_CONTINUOUS_VIDEO, Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
    }

    @Override
    protected int getRootLayoutId() {
        return R.layout.camera_root_layout;
    }

    @Override
    protected void openCamera(int cameraId) {
        Camera.getCameraInfo(cameraId, mCameraInfo);
        mCameraDevice = Camera.open(cameraId);
        onCameraOpened();
    }

    @Override
    protected void initCameraCapabilities() {
        if (mCameraInfo != null) {
            mSensorOrientation = mCameraInfo.orientation;
        }
        if (mCameraDevice == null) return;
        Camera.Parameters parameters = mCameraDevice.getParameters();
        List<Size> supportedPreviewSizes = CameraUtil.convert(parameters.getSupportedPreviewSizes());
        mCameraSettings.setSupportedPreviewSizes(supportedPreviewSizes);
        List<Size> supportedPictureSizes = CameraUtil.convert(parameters.getSupportedPictureSizes());
        mCameraSettings.setSupportedPictureSizes(supportedPictureSizes);
    }

    @Override
    protected void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.release();
            mCameraDevice = null;
        }
    }

    @Override
    protected boolean checkPreviewPreconditions() {
        if (mCameraDevice == null) {
            Log.w(TAG, "mCameraDevice is null when startPreview");
            return false;
        }
        if (!mCameraTextureView.isAvailable()) {
            Log.w(TAG, "texture is null when startPreview");
            return false;
        }
        return true;
    }

    @Override
    protected void setDisplayOrientation() {
        int cameraOrientation = getCameraDisplayOrientation(mCameraInfo, getDisplayRotation());
        mCameraDevice.setDisplayOrientation(cameraOrientation);
    }

    @Override
    protected void applySettingsBeforeStartPreview() {
        // if all parameters has no change, setParameters is unnecessary.
        boolean needSetParameters = false;
        Camera.Parameters parameters = mCameraDevice.getParameters();
        // set optimal preview size
        Camera.Size currentPreviewSize = parameters.getPreviewSize();

        Size textureSize = new Size(mCameraTextureView.getWidth(), mCameraTextureView.getHeight());
        Size optimalSize = mCameraSettings.generateOptimalPreviewSize(textureSize);
        if (optimalSize != null && !optimalSize.equals(CameraUtil.convert(currentPreviewSize))) {
            parameters.setPreviewSize(optimalSize.getWidth(), optimalSize.getHeight());
            needSetParameters = true;
            Log.i(TAG, "setPreviewSize " + optimalSize.toString());
            mCameraTextureView.setTopMargin(mCameraSettings.getPreviewAspectRatioMargin());
            mCameraTextureView.setAspectRatio(optimalSize.getHeight(), optimalSize.getWidth());
        }

        // set optimal picture size
        Camera.Size currentPictureSize = parameters.getPictureSize();
        Size optimalPictureSize = mCameraSettings.generateOptimalPictureSize();
        if (optimalPictureSize != null && !optimalPictureSize.equals(CameraUtil.convert(currentPictureSize))) {
            parameters.setPictureSize(optimalPictureSize.getWidth(), optimalPictureSize.getHeight());
            needSetParameters = true;
            Log.i(TAG, "setPictureSize " + optimalPictureSize.toString());
        }

        // set focus mode
        String targetFocusMode = FOCUS_MODE.get(mCameraSettings.getFocusMode());
        String focusMode = parameters.getFocusMode();
        if (!targetFocusMode.equals(focusMode)) {
            parameters.setFocusMode(targetFocusMode);
            needSetParameters = true;
        }

        if (needSetParameters) {
            mCameraDevice.setParameters(parameters);
            mCameraSettings.onSettingApplied();
        }
    }

    @Override
    protected void startPreview() {
        SurfaceTexture texture = mCameraTextureView.getSurfaceTexture();
        try {
            mCameraDevice.setPreviewTexture(texture);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        mCameraDevice.startPreview();
        setCameraState(STATE.PREVIEW_PREPARING);
    }

    @Override
    protected void stopPreview() {
        if (mCameraDevice == null) return;
        mCameraDevice.stopPreview();
    }

    @Override
    protected void focusAndCapture() {
    }

    @Override
    protected void capture() {
        if (mCameraDevice == null) return;
        // set jpeg rotation
        boolean isFrontCamera = (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        int jpegRotation = CameraUtil.getImageRotation(mSensorOrientation, getDeviceOrientation(), isFrontCamera);

        Camera.Parameters parameters = mCameraDevice.getParameters();
        parameters.setRotation(jpegRotation);
        mCameraDevice.setParameters(parameters);

        mCameraDevice.takePicture(new ShutterCallback(), null, new JpegCallback());
    }

    @Override
    protected void startVideoRecording() {
    }

    @Override
    protected void stopVideoRecording() {
    }

    private int getCameraDisplayOrientation(Camera.CameraInfo info, int displayOrientation) {
        int result = 0;
        if (info == null) return result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + displayOrientation) % 360;
            result = (360 - result) % 360;
        } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            result = (info.orientation - displayOrientation + 360) % 360;
        } else {
            Log.e(TAG, "Camera is facing unhandled direction");
        }
        return result;
    }

    class ShutterCallback implements Camera.ShutterCallback {
        @Override
        public void onShutter() {

        }
    }

    class JpegCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(TAG, "onPictureTaken");
            mMediaSaver.addImage(data, 0, 0, 0, null);
            // restart preview
            stopPreview();
            startPreview();
        }
    }
}
