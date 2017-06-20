package com.sample.camera;

import android.app.Fragment;
import android.content.ContentValues;
import android.graphics.SurfaceTexture;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.sample.camera.settings.CameraSettings;
import com.sample.camera.storage.MediaSaver;
import com.sample.camera.storage.Storage;
import com.sample.camera.utils.FileUtil;
import com.sample.camera.view.AutoFitTextureView;
import com.sample.camera.view.LiveInfoLayout;
import com.sample.camerafeature.R;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;

public abstract class BaseCameraFragment extends Fragment
        implements TextureView.SurfaceTextureListener {
    private static final String TAG = "CAM_FRAGMENT";
    private static final String KEY_CAMERA_ID = CameraActivity.KEY_CAMERA_ID;
    protected int mCameraId = -1;
    protected boolean mIsFirstFrameReceived = false;
    protected AutoFitTextureView mCameraTextureView;
    protected LiveInfoLayout mLiveInfoLayout;
    protected View mCloseButton;
    private CameraActivity.OnBackPressedListener mOnBackPressedListener;
    protected View mShutterButton;
    protected View mVideoShutterButton;

    protected MediaSaver mMediaSaver;
    protected String mImageNameFormat;
    protected String mVideoNameFormat;
    private OrientationManager mOrientationManager;

    protected boolean NEED_FOCUS_BEFORE_CAPTURE = false;

    protected CameraSettings mCameraSettings;

    protected MediaRecorder mMediaRecorder;
    protected String mVideoFileName;
    protected ContentValues mCurrentVideoValues;

    public CameraActivity.OnBackPressedListener getOnBackPressedListener() {
        return mOnBackPressedListener;
    }

    protected enum STATE {
        UNOPENED, OPENED, PREVIEW_PREPARING, PREVIEWING
    }

    protected enum OPERATION {
        NONE, IDLE, CAPTURING, FOCSING
    }

    protected enum RECORD_STATE {
        NONE, INITIALIZING, PREPARING, RECORDING, STOPPING
    }

    protected STATE mDeviceState = STATE.UNOPENED;
    protected OPERATION mCameraOperation = OPERATION.NONE;
    protected RECORD_STATE mRecordState = RECORD_STATE.NONE;

    protected void findRes(View root) {
        mCameraTextureView = (AutoFitTextureView) root.findViewById(R.id.camera_texture_view);
        mCloseButton = root.findViewById(R.id.close_button);
        mShutterButton = root.findViewById(R.id.shutter_button);
        mVideoShutterButton = root.findViewById(R.id.video_shutter_button);
        mLiveInfoLayout = (LiveInfoLayout) root.findViewById(R.id.live_info_layout);
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
        mCameraSettings.addCameraSettingListener(mLiveInfoLayout);

        mOnBackPressedListener = new CameraActivity.OnBackPressedListener() {
            @Override
            public boolean onBackPressed() {
                return !readyForFinish();
            }
        };
        mImageNameFormat = getContext().getString(R.string.image_file_name_format);
        mVideoNameFormat = getContext().getString(R.string.video_file_name_format);
        mMediaSaver = new MediaSaver(getContext().getContentResolver(), mImageNameFormat);
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
        mCameraSettings.setCameraId(mCameraId);
        initCameraCapabilities();
        setupPreview();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "lifecycle onPause " + this);
        super.onPause();
        mOrientationManager.pause();
        if (mRecordState == RECORD_STATE.RECORDING) {
            stopVideoRecording(false);
        }
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
        Log.i(TAG, "onVideoShutterButtonClick in mRecordState " + mRecordState);
        if (mRecordState == RECORD_STATE.RECORDING) {
            stopVideoRecording(true);
        } else if (mRecordState == RECORD_STATE.NONE) {
            if (initMediaRecorder()) {
                if (prepareVideoRecording()) {
                    startVideoRecording();
                }
            }
        } else {
            Log.i(TAG, "onVideoShutterButtonClick in mRecordState " + mRecordState + ", return.");
        }
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

    protected void setCameraOperation(OPERATION operation) {
        Log.i(TAG, "setCameraOperation " + mCameraOperation + " --> " + operation);
        mCameraOperation = operation;
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
        mIsFirstFrameReceived = false;
        setCameraOperation(OPERATION.IDLE);
        setVideoRecordState(RECORD_STATE.NONE);
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
    protected abstract int getImageRotation();
    /** such as preview size, picture size. */
    protected abstract void applySettingsBeforeStartPreview();
    protected abstract void startPreview();
    protected abstract void stopPreview();
    protected abstract void focusAndCapture();
    protected abstract void capture();

    protected void onVideoRecordingError(Exception e) {
        e.printStackTrace();
        stopVideoRecording(true);
        releaseMediaRecorder();
    }

    protected void setVideoRecordState(RECORD_STATE state) {
        onVideoRecordStateChanged(mRecordState, state);
        mRecordState = state;
    }

    protected void onVideoRecordStateChanged(RECORD_STATE oldState, RECORD_STATE newState) {
        Log.i(TAG, "onVideoRecordStateChanged " + oldState + " --> " + newState);
    }

    protected void startVideoRecording() {
        startMediaRecorder(true, new MediaRecorderCallback() {
            @Override
            public void onMediaRecorderStarted(Exception e) {
                // e != null means start failed.
                Log.i(TAG, "onMediaRecorderStarted " + e);
                if (e == null) {
                    setVideoRecordState(RECORD_STATE.RECORDING);
                } else {
                    onVideoRecordingError(e);
                }
            }
        });
    }

    protected void stopVideoRecording(boolean async) {
        if (mRecordState == RECORD_STATE.RECORDING) {
            setVideoRecordState(RECORD_STATE.STOPPING);
            stopMediaRecorder(async, new MediaRecorderCallback() {
                @Override
                public void onMediaRecorderStopped(Exception e) {
                    Log.i(TAG, "onMediaRecorderStopped " + e);
                    // e != null means stop failed.
                    if (e != null) {
                        onVideoRecordingError(e);
                        FileUtil.deleteFile(mVideoFileName);
                    } else {
                        setVideoRecordState(RECORD_STATE.NONE);
                        saveVideoFile();
                    }
                }
            });
        }
    }

    protected boolean prepareVideoRecording() {
        setVideoRecordState(RECORD_STATE.PREPARING);

        setAVSourceForMediaRecorder(mMediaRecorder);
        setProfileForMediaRecorder(mMediaRecorder);

        mMediaRecorder.setOrientationHint(getImageRotation());

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare failed for " + mVideoFileName, e);
            FileUtil.deleteFile(mVideoFileName);
            releaseMediaRecorder();
            return false;
        }

        CameraFragment.MediaRecorderErrorCallback errorCallback = new MediaRecorderErrorCallback();
        mMediaRecorder.setOnErrorListener(errorCallback);
        mMediaRecorder.setOnInfoListener(errorCallback);
        return true;
    }

    protected abstract void setAVSourceForMediaRecorder(MediaRecorder mediaRecorder);

    protected void setProfileForMediaRecorder(MediaRecorder mediaRecorder) {
        CamcorderProfile profile = CamcorderProfile.get(mCameraId, mCameraSettings.getVideoQuality());
        mediaRecorder.setProfile(profile);

        generateVideoFilename(profile);
        mediaRecorder.setOutputFile(mVideoFileName);
    }

    protected boolean initMediaRecorder() {
        setVideoRecordState(RECORD_STATE.INITIALIZING);
        mMediaRecorder = new MediaRecorder();
        return true;
    }

    protected void releaseMediaRecorder() {
        Log.i(TAG, "Releasing media recorder.");
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }

        setVideoRecordState(RECORD_STATE.NONE);

        mVideoFileName = null;
    }

    private void generateVideoFilename(CamcorderProfile profile) {
        long dateTaken = System.currentTimeMillis();
        String title = FileUtil.dateFormat(dateTaken, new SimpleDateFormat(mVideoNameFormat));
        // Used when emailing.
        String filename = title + FileUtil.convertFormatToFileExt(profile.fileFormat);
        String mime = FileUtil.convertFormatToMimeType(profile.fileFormat);
        String path = Storage.DIRECTORY + '/' + filename;
        String tmpPath = path + ".tmp";
        mCurrentVideoValues = new ContentValues(9);
        mCurrentVideoValues.put(MediaStore.Video.Media.TITLE, title);
        mCurrentVideoValues.put(MediaStore.Video.Media.DISPLAY_NAME, filename);
        mCurrentVideoValues.put(MediaStore.Video.Media.DATE_TAKEN, dateTaken);
        mCurrentVideoValues.put(MediaStore.MediaColumns.DATE_MODIFIED, dateTaken / 1000);
        mCurrentVideoValues.put(MediaStore.Video.Media.MIME_TYPE, mime);
        mCurrentVideoValues.put(MediaStore.Video.Media.DATA, path);
        mCurrentVideoValues.put(MediaStore.Video.Media.WIDTH, profile.videoFrameWidth);
        mCurrentVideoValues.put(MediaStore.Video.Media.HEIGHT, profile.videoFrameHeight);
        mCurrentVideoValues.put(MediaStore.Video.Media.RESOLUTION,
                Integer.toString(profile.videoFrameWidth) + "x" +
                        Integer.toString(profile.videoFrameHeight));
        mVideoFileName = tmpPath;
        Log.i(TAG, "New video filename: " + mVideoFileName);
    }

    protected void saveVideoFile() {
        mMediaSaver.addVideo(mVideoFileName, mCurrentVideoValues, null);
    }

    protected void startMediaRecorder(boolean async, final MediaRecorderCallbackInterface callback) {
        if (async) {
            new StartMediaRecorderTask(mMediaRecorder, callback).execute();
        } else {
            try {
                mMediaRecorder.start();
                if (callback != null) {
                    callback.onMediaRecorderStarted(null);
                }
            } catch (RuntimeException e) {
                if (callback != null) {
                    callback.onMediaRecorderStarted(e);
                }
            }
        }
    }

    protected void stopMediaRecorder(boolean async, final MediaRecorderCallbackInterface callback) {
        mMediaRecorder.setOnErrorListener(null);
        mMediaRecorder.setOnInfoListener(null);
        if (async) {
            new StopMediaRecorderTask(mMediaRecorder, callback).execute();
        } else {
            try {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                if (callback != null) {
                    callback.onMediaRecorderStopped(null);
                }
            } catch (RuntimeException e) {
                if (callback != null) {
                    callback.onMediaRecorderStopped(e);
                }
            }
        }
    }

    static class StartMediaRecorderTask extends AsyncTask<Void, Void, Exception> {
        private final WeakReference<MediaRecorder> mMediaRecorderReference;
        private final WeakReference<MediaRecorderCallbackInterface> mMediaRecorderCallbackReference;
        public StartMediaRecorderTask(MediaRecorder mediaRecorder, MediaRecorderCallbackInterface callback) {
            mMediaRecorderReference = new WeakReference<MediaRecorder>(mediaRecorder);
            mMediaRecorderCallbackReference = new WeakReference<MediaRecorderCallbackInterface>(callback);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            MediaRecorder mediaRecorder = mMediaRecorderReference.get();
            if (mediaRecorder == null) return null;
            try {
                mediaRecorder.start();
                return null;
            } catch (RuntimeException e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception e) {
            MediaRecorderCallbackInterface callback = mMediaRecorderCallbackReference.get();
            if (callback != null) {
                callback.onMediaRecorderStarted(e);
            }
        }
    }

    static class StopMediaRecorderTask extends AsyncTask<Void, Void, Exception> {
        private final WeakReference<MediaRecorder> mMediaRecorderReference;
        private final WeakReference<MediaRecorderCallbackInterface> mMediaRecorderCallbackReference;
        public StopMediaRecorderTask(MediaRecorder mediaRecorder, MediaRecorderCallbackInterface callback) {
            mMediaRecorderReference = new WeakReference<MediaRecorder>(mediaRecorder);
            mMediaRecorderCallbackReference = new WeakReference<MediaRecorderCallbackInterface>(callback);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            MediaRecorder mediaRecorder = mMediaRecorderReference.get();
            if (mediaRecorder == null) return null;
            try {
                mediaRecorder.stop();
                mediaRecorder.reset();
                return null;
            } catch (RuntimeException e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception e) {
            MediaRecorderCallbackInterface callback = mMediaRecorderCallbackReference.get();
            if (callback != null) {
                callback.onMediaRecorderStopped(e);
            }
        }
    }

    class MediaRecorderCallback implements MediaRecorderCallbackInterface {
        @Override
        public void onMediaRecorderStarted(Exception e) {
        }

        @Override
        public void onMediaRecorderStopped(Exception e) {
        }
    }

    private interface MediaRecorderCallbackInterface {
        public void onMediaRecorderStarted(Exception e);
        public void onMediaRecorderStopped(Exception e);
    }

    class MediaRecorderErrorCallback
            implements MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            String msg = "MediaRecorder onError " + what + ", " + extra;
            Exception e = new Exception("MediaRecorderError " + msg);
            onVideoRecordingError(e);
        }

        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            String msg = "MediaRecorder onInfo " + what + ", " + extra;
            Exception e = new Exception("MediaRecorderInfo " + msg);
            onVideoRecordingError(e);
        }
    }
}
