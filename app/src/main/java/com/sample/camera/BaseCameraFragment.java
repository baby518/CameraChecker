package com.sample.camera;

import android.app.Fragment;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.sample.camera.settings.CameraSettings;
import com.sample.camera.storage.MediaSaver;
import com.sample.camera.view.AutoFitTextureView;
import com.sample.camerafeature.R;

public abstract class BaseCameraFragment extends Fragment
        implements TextureView.SurfaceTextureListener {
    private static final String TAG = "CAM_FRAGMENT";
    private static final String KEY_CAMERA_ID = CameraActivity.KEY_CAMERA_ID;
    protected int mCameraId = -1;
    protected boolean mIsFirstFrameReceived = false;
    protected AutoFitTextureView mCameraTextureView;
    protected View mCloseButton;
    private CameraActivity.OnBackPressedListener mOnBackPressedListener;
    protected View mShutterButton;
    protected View mVideoShutterButton;

    protected MediaSaver mMediaSaver;
    private OrientationManager mOrientationManager;

    protected boolean NEED_FOCUS_BEFORE_CAPTURE = false;

    protected CameraSettings mCameraSettings;

    public CameraActivity.OnBackPressedListener getOnBackPressedListener() {
        return mOnBackPressedListener;
    }

    protected enum STATE {
        UNOPENED, OPENED, PREVIEW_PREPARING, PREVIEWING
    }

    protected enum OPERATION {
        NONE, IDLE, CAPTURING, FOCSING
    }

    protected STATE mDeviceState = STATE.UNOPENED;
    protected OPERATION mCameraOperation = OPERATION.NONE;

    protected void findRes(View root) {
        mCameraTextureView = (AutoFitTextureView) root.findViewById(R.id.camera_texture_view);
        mCloseButton = root.findViewById(R.id.close_button);
        mShutterButton = root.findViewById(R.id.shutter_button);
        mVideoShutterButton = root.findViewById(R.id.video_shutter_button);
    }

    protected void initRes(View root) {
        mCameraTextureView.setSurfaceTextureListener(this);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mShutterButton.setEnabled(false);
        mShutterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShutterButtonClick();
            }
        });

        mVideoShutterButton.setEnabled(false);
        mVideoShutterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onVideoShutterButtonClick();
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "lifecycle onCreate E " + this);
        super.onCreate(savedInstanceState);
        Bundle argument = getArguments();
        if (argument != null) {
            mCameraId = (int) argument.get(KEY_CAMERA_ID);
        } else {
            mCameraId = 0;
        }
        Log.i(TAG, "lifecycle onCreate X " + this + ", mCameraId " + mCameraId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "lifecycle onCreateView " + this);
        int id = getRootLayoutId();
        if (id == 0) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        View view = inflater.inflate(id, container, false);
        if (view != null) {
            findRes(view);
            initRes(view);
            return view;
        } else {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "lifecycle onActivityCreated E " + this);
        super.onActivityCreated(savedInstanceState);
        mCameraSettings = new CameraSettings(getContext());
        mOnBackPressedListener = new CameraActivity.OnBackPressedListener() {
            @Override
            public boolean onBackPressed() {
                return !readyForFinish();
            }
        };
        mMediaSaver = new MediaSaver(getContext().getContentResolver(),
                getContext().getString(R.string.image_file_name_format));
        mOrientationManager = new OrientationManager(getContext());
        Log.i(TAG, "lifecycle onActivityCreated x " + this);
    }

    @Override
    public void onResume() {
        Log.i(TAG, "lifecycle onResume " + this);
        super.onResume();
        mOrientationManager.resume();
        if (mCameraId == -1) return;
        openCamera(mCameraId);
    }

    protected void onCameraOpened() {
        setCameraState(STATE.OPENED);
        initCameraCapabilities();
        setupPreview();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "lifecycle onPause " + this);
        super.onPause();
        mOrientationManager.pause();
        stopPreview();
        closeCamera();
        mCameraTextureView.setSurfaceTextureListener(null);
        setCameraState(STATE.UNOPENED);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "lifecycle onDestroy " + this);
        super.onDestroy();
    }

    private void onShutterButtonClick() {
        if (NEED_FOCUS_BEFORE_CAPTURE) {
            focusAndCapture();
        } else {
            capture();
        }
    }

    private void onVideoShutterButtonClick() {

    }

    protected boolean readyForFinish() {
        return true;
    }

    protected void finish() {
        if (readyForFinish()) {
            getActivity().onBackPressed();
        }
    }

    protected void setCameraState(STATE state) {
        Log.i(TAG, "setCameraState " + mDeviceState + " --> " + state);
        mDeviceState = state;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureAvailable");
        setupPreview();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i(TAG, "onSurfaceTextureDestroyed");
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        if (!mIsFirstFrameReceived) {
            mIsFirstFrameReceived = true;
            onPreviewStarted();
        }
    }

    protected void onPreviewStarted() {
        Log.i(TAG, "onPreviewStarted");
        setCameraState(STATE.PREVIEWING);

        mShutterButton.setEnabled(true);
        mVideoShutterButton.setEnabled(true);
    }

    private void setupPreview() {
        if (mDeviceState == STATE.PREVIEW_PREPARING || mDeviceState == STATE.PREVIEWING) return;
        if (!checkPreviewPreconditions()) return;
        setDisplayOrientation();
        applySettingsBeforeStartPreview();
        startPreview();
        mCameraOperation = OPERATION.IDLE;
        mIsFirstFrameReceived = false;
    }

    protected int getDeviceOrientation() {
        if (mOrientationManager == null) return 0;
        return mOrientationManager.getDeviceOrientation();
    }

    protected int getDisplayRotation() {
        if (mOrientationManager == null) return 0;
        return mOrientationManager.getDisplayRotation();
    }

    protected abstract int getRootLayoutId();

    protected abstract void openCamera(int cameraId);
    protected abstract void initCameraCapabilities();
    protected abstract void closeCamera();
    protected abstract boolean checkPreviewPreconditions();
    protected abstract void setDisplayOrientation();
    /** such as preview size, picture size. */
    protected abstract void applySettingsBeforeStartPreview();
    protected abstract void startPreview();
    protected abstract void stopPreview();
    protected abstract void focusAndCapture();
    protected abstract void capture();
    protected abstract void startVideoRecording();
    protected abstract void stopVideoRecording();
}
