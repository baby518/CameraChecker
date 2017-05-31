package com.sample.camera;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.sample.camera.settings.CameraSettings;
import com.sample.camera.utils.CameraUtil;
import com.sample.camerafeature.R;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class Camera2Fragment extends BaseCameraFragment {
    private static final String TAG = "CAM_FRAGMENT_API2";

    private String mCameraIdString;
    private CameraDevice mCameraDevice;
    private CameraCharacteristics mCharacteristics;
    private int mSensorOrientation = 0;
    private int mSensorFacing = -1;

    private CameraCaptureSession mCaptureSession;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private Handler mMainHandler;

    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    /** for still image capture */
    private ImageReader mImageReader;

    private static final HashMap<CameraSettings.FocusMode, Integer> FOCUS_MODE = new HashMap<>();

    static {
        FOCUS_MODE.put(CameraSettings.FocusMode.FOCUS_MODE_AUTO, CaptureRequest.CONTROL_AF_MODE_AUTO);
        FOCUS_MODE.put(CameraSettings.FocusMode.FOCUS_MODE_CONTINUOUS_PICTURE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        FOCUS_MODE.put(CameraSettings.FocusMode.FOCUS_MODE_CONTINUOUS_VIDEO, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
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
        }
    }

    @Override
    protected void closeCamera() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
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
        //
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
                            public void onConfigured(
                                    @NonNull CameraCaptureSession cameraCaptureSession) {
                                // The camera is already closed
                                if (mCameraDevice == null) {
                                    return;
                                }

                                // When the session is ready, we start displaying
                                // the preview.
                                mCaptureSession = cameraCaptureSession;
                                // set focus mode
                                int targetFocusMode = FOCUS_MODE.get(mCameraSettings.getFocusMode());
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, targetFocusMode);
                                // // Flash is automatically enabled when
                                // necessary.
                                // setAutoFlash(mPreviewRequestBuilder);

                                try {
                                    // Finally, we start displaying the camera preview.
                                    mPreviewRequest = mPreviewRequestBuilder.build();
                                    mCaptureSession.setRepeatingRequest(mPreviewRequest, mPreviewCallback,
                                            mBackgroundHandler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
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
            try {
                // Finally, we start displaying the camera preview.
                mCaptureSession.setRepeatingRequest(mPreviewRequest, mPreviewCallback,
                        mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void stopPreview() {

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
            boolean isFrontCamera = (mSensorFacing == CameraMetadata.LENS_FACING_FRONT);
            int jpegRotation = CameraUtil.getImageRotation(mSensorOrientation, getDeviceOrientation(), isFrontCamera);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, jpegRotation);

            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                        @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    Log.i(TAG, "onCaptureCompleted");
                    stopPreview();
                    startPreview();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.capture(captureBuilder.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void startVideoRecording() {

    }

    @Override
    protected void stopVideoRecording() {

    }


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
