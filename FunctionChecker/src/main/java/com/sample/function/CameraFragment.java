package com.sample.function;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.Log;
import android.util.Size;

import com.sample.function.settings.CameraSettings;
import com.sample.function.utils.CameraUtil;
import com.sample.function.utils.FileUtil;
import com.sample.function.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class CameraFragment extends BaseCameraFragment {
    private static final String TAG = "CAM_FRAGMENT_API1";
    private static final String VIDEO_SIZE_KEY = "video-size";
    private Camera mCameraDevice;
    private Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
    private int mSensorOrientation = 0;
    private static final HashMap<CameraSettings.FocusMode, String> FOCUS_MODE = new HashMap<>();

    static {
        FOCUS_MODE.put(CameraSettings.FocusMode.AUTO, Camera.Parameters.FOCUS_MODE_AUTO);
        FOCUS_MODE.put(CameraSettings.FocusMode.CONTINUOUS_PICTURE, Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        FOCUS_MODE.put(CameraSettings.FocusMode.CONTINUOUS_VIDEO, Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
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
        List<Size> supportedVideoSizes = CameraUtil.convert(parameters.getSupportedVideoSizes());
        mCameraSettings.setSupportedVideoSizes(supportedVideoSizes);
    }

    @Override
    protected void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.release();
            mCameraDevice = null;
        }

        releaseMediaRecorder();
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
    protected int getImageRotation() {
        boolean isFrontCamera = (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        return CameraUtil.getImageRotation(mSensorOrientation, getDeviceOrientation(), isFrontCamera);
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

        // set optimal video size
        Size currentVideoSize = CameraUtil.strToSize(parameters.get(VIDEO_SIZE_KEY));
        mCameraSettings.generateOptimalVideoQuality();
        Log.i(TAG, "generateOptimalVideoQuality " + mCameraSettings.getVideoQuality());
        Size optimalVideoSize = mCameraSettings.getVideoSize();
        if (optimalVideoSize != null && !optimalVideoSize.equals(currentVideoSize)) {
            parameters.set(VIDEO_SIZE_KEY, CameraUtil.sizeToStr(optimalVideoSize));
            needSetParameters = true;
            Log.i(TAG, "setVideoSize " + optimalVideoSize.toString());
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
        }
        mCameraSettings.onSettingApplied();
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
        int jpegRotation = getImageRotation();

        Camera.Parameters parameters = mCameraDevice.getParameters();
        parameters.setRotation(jpegRotation);
        mCameraDevice.setParameters(parameters);

        setCameraOperation(OPERATION.CAPTURING);
        mCameraDevice.takePicture(new ShutterCallback(), null, new JpegCallback());
    }

    // ++++++++++++++++++++++++++++++  Video Recording code start +++++++++++++++++++++++++++++++++
    @Override
    protected boolean initMediaRecorder() {
        boolean ret = super.initMediaRecorder();
        if (ret) {
            if (mCameraDevice == null) {
                Log.w(TAG, "null camera within proxy");
                return false;
            }
            // Unlock the camera object before passing it to media recorder.
            mCameraDevice.unlock();
            mMediaRecorder.setCamera(mCameraDevice);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void setAVSourceForMediaRecorder(MediaRecorder mediaRecorder) {
        if (mediaRecorder == null) return;
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    }
    // ++++++++++++++++++++++++++++++  Video Recording code end +++++++++++++++++++++++++++++++++

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
            setCameraOperation(OPERATION.IDLE);
            mMediaSaver.addImage(data, 0, 0, 0, null);
            // restart preview
            stopPreview();
            startPreview();
        }
    }
}
