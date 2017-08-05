package com.sample.function;

import android.content.Context;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

public class OrientationManager {
    private static final String TAG = "OrientationManager";
    // DeviceOrientation hysteresis amount used in rounding, in degrees
    private static final int ORIENTATION_HYSTERESIS = 5;
    private int mLastDeviceOrientation = 0;
    private final Context mContext;

    private final MyOrientationEventListener mOrientationListener;

    public OrientationManager(Context context) {
        mContext = context;
        mOrientationListener = new MyOrientationEventListener(context);
    }

    public void resume() {
        mOrientationListener.enable();
    }

    public void pause() {
        mOrientationListener.disable();
    }

    public int getDeviceOrientation() {
        return mLastDeviceOrientation;
    }

    protected int getDisplayRotation() {
        WindowManager windowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    // This listens to the device orientation, so we can update the compensation.
    private class MyOrientationEventListener extends OrientationEventListener {
        public MyOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == ORIENTATION_UNKNOWN) {
                return;
            }

            final int roundedDeviceOrientation =
                    roundOrientation(mLastDeviceOrientation, orientation);
            if (roundedDeviceOrientation == mLastDeviceOrientation) {
                return;
            }
            Log.i(TAG, "orientation changed (from:to) " + mLastDeviceOrientation +
                    ":" + roundedDeviceOrientation);
            mLastDeviceOrientation = roundedDeviceOrientation;
        }
    }

    private static int roundOrientation(int oldDeviceOrientation,
                                                      int newRawOrientation) {
        int dist = Math.abs(newRawOrientation - oldDeviceOrientation);
        dist = Math.min(dist, 360 - dist);
        boolean isOrientationChanged = (dist >= 45 + ORIENTATION_HYSTERESIS);

        if (isOrientationChanged) {
            int newRoundedOrientation = ((newRawOrientation + 45) / 90 * 90) % 360;
            return newRoundedOrientation;
        }
        return oldDeviceOrientation;
    }
}
