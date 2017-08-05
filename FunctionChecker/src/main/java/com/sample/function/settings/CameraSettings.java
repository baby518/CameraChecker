package com.sample.function.settings;

import android.content.Context;
import android.media.CamcorderProfile;
import android.util.Log;
import android.util.Size;

import com.sample.function.utils.CameraUtil;
import com.sample.function.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraSettings {
    private static final String TAG = "CameraSettings";

    private int mCameraId = -1;

    private List<Size> mSupportedPreviewSizes = new ArrayList<>();
    private List<Size> mSupportedPictureSizes = new ArrayList<>();
    private List<Size> mSupportedVideoSizes = new ArrayList<>();
    private Size mPreviewSize;
    private Size mPictureSize;
    private Size mVideoSize;
    private int mVideoQuality;
    private AspectRatio mPreviewAspectRatio = AspectRatio.RATIO_169;

    public enum AspectRatio {
        RATIO_43, RATIO_169
    }

    private final int mPreviewTopMargin43;
    private final int mPreviewTopMargin169;

    public enum FocusMode {
        AUTO, CONTINUOUS_PICTURE, CONTINUOUS_VIDEO
    }

    private FocusMode mFocusMode = FocusMode.CONTINUOUS_PICTURE;

    public interface CameraSettingListener {
        void onSettingApplied(Map<String, String> settings);
    }

    private ArrayList<CameraSettingListener> listeners = new ArrayList<>();

    public void addCameraSettingListener(CameraSettingListener listener) {
        listeners.add(listener);
    }

    public CameraSettings(Context context) {
        mPreviewTopMargin169 = 0;
        mPreviewTopMargin43 = context.getResources()
                .getDimensionPixelSize(R.dimen.camera_option_bar_height);
    }

    public void setCameraId(int cameraId) {
        mCameraId = cameraId;
    }

    public void setPreviewAspectRatio(AspectRatio aspectRatio) {
        mPreviewAspectRatio = aspectRatio;
    }

    public AspectRatio getPreviewAspectRatio() {
        return mPreviewAspectRatio;
    }

    public double getPreviewAspectRatioDouble() {
        if (mPreviewAspectRatio == AspectRatio.RATIO_43) {
            return 4.0f/3.0f;
        } else if (mPreviewAspectRatio == AspectRatio.RATIO_169) {
            return 16.0f/9.0f;
        }
        return -1f;
    }

    public int getPreviewAspectRatioMargin() {
        if (mPreviewAspectRatio == AspectRatio.RATIO_43) {
            return mPreviewTopMargin43;
        } else if (mPreviewAspectRatio == AspectRatio.RATIO_169) {
            return mPreviewTopMargin169;
        }
        return 0;
    }

    public Size generateOptimalPreviewSize(Size textureSize) {
        mPreviewSize = CameraUtil.getOptimalSize(mSupportedPreviewSizes,
                getPreviewAspectRatioDouble(), textureSize);

        if (mPreviewSize == null) {
            Log.w(TAG, "not found suitable preview size, use largest instead.");
            mPreviewSize = Collections.max(mSupportedPreviewSizes, new CompareSizesByArea());
        }
        return mPreviewSize;
    }

    public List<Size> getSupportedPreviewSizes() {
        return mSupportedPreviewSizes;
    }

    public void setSupportedPreviewSizes(List<Size> supportedPreviewSizes) {
        this.mSupportedPreviewSizes = supportedPreviewSizes;
    }

    public Size generateOptimalPictureSize() {
        mPictureSize = CameraUtil.getOptimalSize(mSupportedPictureSizes,
                getPreviewAspectRatioDouble());

        if (mPictureSize == null) {
            Log.w(TAG, "not found suitable picture size, use largest instead.");
            mPictureSize = Collections.max(mSupportedPictureSizes, new CompareSizesByArea());
        }
        return mPictureSize;
    }

    public List<Size> getSupportedPictureSizes() {
        return mSupportedPictureSizes;
    }

    public void setSupportedPictureSizes(List<Size> supportedPictureSizes) {
        this.mSupportedPictureSizes = supportedPictureSizes;
    }

    public void setSupportedVideoSizes(List<Size> supportedVideoSizes) {
        this.mSupportedVideoSizes = supportedVideoSizes;
    }

    public List<Size> getSupportedVideoSizes() {
        return mSupportedVideoSizes;
    }

    public Size getVideoSize() {
        return mVideoSize;
    }

    public int getVideoQuality() {
        return mVideoQuality;
    }

    public void generateOptimalVideoQuality() {
        mVideoQuality = CameraUtil.getOptimalVideoQuality(mCameraId, mSupportedVideoSizes,
                getPreviewAspectRatioDouble());

        CamcorderProfile profile = CamcorderProfile.get(mCameraId, mVideoQuality);
        mVideoSize = new Size(profile.videoFrameWidth, profile.videoFrameHeight);
    }

    public FocusMode getFocusMode() {
        return mFocusMode;
    }

    public void setFocusMode(FocusMode focusMode) {
        this.mFocusMode = focusMode;
    }

    public void onSettingApplied() {
        Map<String, String> settings = generateSettingMap();
        for (CameraSettingListener listener : listeners) {
            if (listener != null) {
                listener.onSettingApplied(settings);
            }
        }
    }

    private Map<String, String> generateSettingMap() {
        Map<String, String> settings = new HashMap<>();
        settings.put("PreviewSize", mPreviewSize.toString());
        settings.put("VideoSize", mVideoSize.toString());
        settings.put("VideoQuality", Integer.toString(mVideoQuality));
        settings.put("PictureSize", mPictureSize.toString());
        settings.put(mFocusMode.getClass().getSimpleName(), mFocusMode.name());
        return settings;
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight()
                    - (long) rhs.getWidth() * rhs.getHeight());
        }
    }
}
