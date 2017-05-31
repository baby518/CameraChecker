package com.sample.camera.settings;

import android.content.Context;
import android.util.Log;
import android.util.Size;

import com.sample.camera.utils.CameraUtil;
import com.sample.camerafeature.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraSettings {
    private static final String TAG = "CameraSettings";

    private List<Size> mSupportedPreviewSizes = new ArrayList<>();
    private List<Size> mSupportedPictureSizes = new ArrayList<>();
    private Size mPreviewSize;
    private Size mPictureSize;
    private AspectRatio mPreviewAspectRatio = AspectRatio.RATIO_169;

    public enum AspectRatio {
        RATIO_43, RATIO_169
    }

    private final int mPreviewTopMargin43;
    private final int mPreviewTopMargin169;

    public enum FocusMode {
        FOCUS_MODE_AUTO, FOCUS_MODE_CONTINUOUS_PICTURE, FOCUS_MODE_CONTINUOUS_VIDEO
    }

    private FocusMode mFocusMode = FocusMode.FOCUS_MODE_CONTINUOUS_PICTURE;

    public CameraSettings(Context context) {
        mPreviewTopMargin169 = 0;
        mPreviewTopMargin43 = context.getResources()
                .getDimensionPixelSize(R.dimen.camera_option_bar_height);
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

    public FocusMode getFocusMode() {
        return mFocusMode;
    }

    public void setFocusMode(FocusMode focusMode) {
        this.mFocusMode = focusMode;
    }

    public void onSettingApplied() {
        // TODO update live info.
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
