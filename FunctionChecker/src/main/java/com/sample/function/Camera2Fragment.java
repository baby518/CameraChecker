package com.sample.function;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.sample.function.settings.CameraSettings;
import com.sample.function.utils.CameraUtil;
import com.sample.function.utils.FileUtil;
import com.sample.function.R;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Camera2Fragment extends BaseCameraFragment {
    private static final String TAG = "CAM_FRAGMENT_API2";

    private String mCameraIdString;
    private CameraDevice mCameraDevice;
    private CameraCharacteristics mCharacteristics;
    private int mSensorOrientation = 0;
    private int mSensorFacing = -1;

    private CameraCaptureSession mPreviewSession;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private Handler mMainHandler;

    private CaptureRequest.Builder mPreviewRequestBuilder;
    /** for still image capture */
    private ImageReader mImageReader;

    private static final HashMap<CameraSettings.FocusMode, Integer> FOCUS_MODE = new HashMap<>();

    static {
        FOCUS_MODE.put(CameraSettings.FocusMode.AUTO, CaptureRequest.CONTROL_AF_MODE_AUTO);
        FOCUS_MODE.put(CameraSettings.FocusMode.CONTINUOUS_PICTURE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        FOCUS_MODE.put(CameraSettings.FocusMode.CONTINUOUS_VIDEO, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onResume() {
        startBackgroundThread();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBackgroundThread();
    }

    @Override
    protected int getRootLayoutId() {
        return R.layout.camera_root_layout;
    }

    private CameraManager getCameraManager() {
        Activity activity = getActivity();
        return (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void openCamera(int cameraId) {
        mCameraIdString = String.valueOf(cameraId);
        CameraManager manager = getCameraManager();
        try {
//            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
//                throw new RuntimeException("Time out waiting to lock camera opening.");
//            }
            manager.openCamera(mCameraIdString, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initCameraCapabilities() {
        CameraManager manager = getCameraManager();
        try {
            mCharacteristics = manager.getCameraCharacteristics(mCameraIdString);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return;
        }
        if (mCharacteristics.getKeys().contains(CameraCharacteristics.SENSOR_ORIENTATION)) {
            mSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        } else {
            Log.w(TAG, "SENSOR_ORIENTATION has not found.");
        }

        if (mCharacteristics.getKeys().contains(CameraCharacteristics.LENS_FACING)) {
            mSensorFacing = mCharacteristics.get(CameraCharacteristics.LENS_FACING);
        } else {
            Log.w(TAG, "LENS_FACING has not found.");
        }

        StreamConfigurationMap map = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map != null) {
            Size[] previewSizes = map.getOutputSizes(SurfaceTexture.class);
            mCameraSettings.setSupportedPreviewSizes(CameraUtil.convert(previewSizes));

            Size[] pictureSizes = map.getOutputSizes(ImageFormat.JPEG);
            mCameraSettings.setSupportedPictureSizes(CameraUtil.convert(pictureSizes));

            Size[] videoSizes = map.getOutputSizes(MediaRecorder.class);
            mCameraSettings.setSupportedVideoSizes(CameraUtil.convert(videoSizes));
        }
    }

    @Override
    protected void closeCamera() {
        closePreviewSession();
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }

        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
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
    }

    @Override
    protected int getImageRotation() {
        boolean isFrontCamera = (mSensorFacing == CameraMetadata.LENS_FACING_FRONT);
        return CameraUtil.getImageRotation(mSensorOrientation, getDeviceOrientation(), isFrontCamera);
    }

    @Override
    protected void applySettingsBeforeStartPreview() {
        if (mCharacteristics == null) return;
        StreamConfigurationMap map = mCharacteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
            return;
        }
        // setup preview size
        setPreviewSize();
        // setup picture size
        setPictureSize();
        // setup video size
        setVideoSize();

        mCameraSettings.onSettingApplied();
    }

    private void setPreviewSize() {
        SurfaceTexture texture = mCameraTextureView.getSurfaceTexture();
        if (texture == null) return;

        // set optimal preview size
        Size textureSize = new Size(mCameraTextureView.getWidth(), mCameraTextureView.getHeight());
        Size optimalSize = mCameraSettings.generateOptimalPreviewSize(textureSize);
        Log.i(TAG, "set preview size " + optimalSize);
        // We configure the size of default buffer to be the size of camera preview we want.
        texture.setDefaultBufferSize(optimalSize.getWidth(), optimalSize.getHeight());
    }

    private void setPictureSize() {
        // set optimal picture size
        Size optimalSize = mCameraSettings.generateOptimalPictureSize();
        Log.i(TAG, "set picture size " + optimalSize);
        mImageReader = ImageReader.newInstance(optimalSize.getWidth(), optimalSize.getHeight(),
                ImageFormat.JPEG, 1);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
    }

    private void setVideoSize() {
        // set optimal video size
        mCameraSettings.generateOptimalVideoQuality();
        Size optimalSize = mCameraSettings.getVideoSize();
        Log.i(TAG, "set video size " + optimalSize);
    }

    private void createCameraPreviewSession() {
        SurfaceTexture texture = mCameraTextureView.getSurfaceTexture();
        if (texture == null) return;
        // This is the output Surface we need to start preview.
        Surface surface = new Surface(texture);
        try {
            if (mPreviewRequestBuilder == null) {
                mPreviewRequestBuilder = mCameraDevice
                        .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreviewRequestBuilder.addTarget(surface);

                // Here, we create a CameraCaptureSession for camera preview.
                mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                        new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession cameraSession) {
                                // The camera is already closed
                                if (mCameraDevice == null) {
                                    return;
                                }

                                // When the session is ready, we start displaying
                                // the preview.
                                mPreviewSession = cameraSession;
                                // set focus mode
                                int targetFocusMode = FOCUS_MODE.get(mCameraSettings.getFocusMode());
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, targetFocusMode);
                                // // Flash is automatically enabled when
                                // necessary.
                                // setAutoFlash(mPreviewRequestBuilder);

                                // Finally, we start displaying the camera preview.
                                applyPreviewRequest(mPreviewCallback);
                            }

                            @Override
                            public void onConfigureFailed(
                                    @NonNull CameraCaptureSession cameraCaptureSession) {
                                // showToast("Failed");
                            }
                        }, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void startPreview() {
        if (mPreviewRequestBuilder == null) {
            createCameraPreviewSession();
        } else {
            applyPreviewRequest(mPreviewCallback);
        }
    }

    private void applyPreviewRequest(CameraCaptureSession.CaptureCallback listener) {
        try {
            // start displaying the camera preview.
            mPreviewSession.setRepeatingRequest(mPreviewRequestBuilder.build(), listener,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void stopPreview() {
        mPreviewRequestBuilder = null;
    }

    @Override
    protected void focusAndCapture() {
    }

    @Override
    protected void capture() {
        final Activity activity = getActivity();
        if (null == activity || null == mCameraDevice) {
            return;
        }

        try {
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder = mCameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Orientation
            int jpegRotation = getImageRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, jpegRotation);

            setCameraOperation(OPERATION.CAPTURING);
            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                        @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    Log.i(TAG, "onCaptureCompleted");
                    setCameraOperation(OPERATION.IDLE);
                    stopPreview();
                    startPreview();
                }
            };

            mPreviewSession.stopRepeating();
            mPreviewSession.capture(captureBuilder.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // ++++++++++++++++++++++++++++++  Video Recording code start +++++++++++++++++++++++++++++++++
    @Override
    protected void startVideoRecording() {
        closePreviewSession();
        SurfaceTexture texture = mCameraTextureView.getSurfaceTexture();
        if (texture == null) {
            stopVideoRecording(true);
        } else {
            texture.setDefaultBufferSize(mCameraSettings.getVideoSize().getWidth(),
                    mCameraSettings.getVideoSize().getHeight());
            try {
                mPreviewRequestBuilder = mCameraDevice
                        .createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                List<Surface> surfaces = new ArrayList<>();

                // Set up Surface for the camera preview
                Surface previewSurface = new Surface(texture);
                surfaces.add(previewSurface);
                mPreviewRequestBuilder.addTarget(previewSurface);

                // Set up Surface for the MediaRecorder
                Surface recorderSurface = mMediaRecorder.getSurface();
                surfaces.add(recorderSurface);
                mPreviewRequestBuilder.addTarget(recorderSurface);

                // Start a capture session
                // Once the session starts, we can update the UI and start recording
                mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        mPreviewSession = cameraCaptureSession;
                        applyPreviewRequest(null);
                        Camera2Fragment.super.startVideoRecording();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        Exception e = new Exception("onConfigureFailed");
                        onVideoRecordingError(e);
                    }
                }, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onVideoRecordStateChanged(RECORD_STATE oldState, RECORD_STATE newState) {
        super.onVideoRecordStateChanged(oldState, newState);
        if (oldState == RECORD_STATE.STOPPING && newState == RECORD_STATE.NONE) {
            // VideoRecord done
            onVideoRecordDone();
        }
    }

    private void onVideoRecordDone() {
        // VideoRecord done, restart preview
        stopPreview();
        startPreview();
    }

    @Override
    protected void setAVSourceForMediaRecorder(MediaRecorder mediaRecorder) {
        if (mediaRecorder == null) return;
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
    }

    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }
    // ++++++++++++++++++++++++++++++  Video Recording code end +++++++++++++++++++++++++++++++++

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
//            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    onCameraOpened();
                }
            });
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
//            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
//            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
//            Activity activity = getActivity();
//            if (null != activity) {
//                activity.finish();
//            }
        }
    };

    private CameraCaptureSession.CaptureCallback mPreviewCallback
            = new CameraCaptureSession.CaptureCallback() {
    };

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.i(TAG, "onImageAvailable");

            Image image = reader.acquireNextImage();

            if (image != null) {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                mMediaSaver.addImage(bytes, 0, 0, 0, null);
                image.close();
            }
        }
    };
}
