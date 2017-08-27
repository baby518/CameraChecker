package com.sample.benchmark;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

public class CameraAgent {
    private static CameraAgent mCameraAgent;
    private CameraHandler mCameraHandler;

    public static CameraAgent getInstance() {
        if (mCameraAgent == null) {
            mCameraAgent = new CameraAgent();
        }
        return mCameraAgent;
    }

    private CameraAgent() {
        HandlerThread mCameraHandlerThread = new HandlerThread("Benchmark Thread");
        mCameraHandlerThread.start();
        mCameraHandler = new CameraHandler(mCameraHandlerThread.getLooper());
    }

    public void runJob(Action action) {
        runJob(action, 0, 0, null);
    }

    public void runJob(Action action, Object obj) {
        runJob(action, 0, 0, obj);
    }

    public void runJob(Action action, int arg) {
        runJob(action, arg, 0, null);
    }

    public void runJob(Action action, int arg1, int arg2, Object obj) {
        mCameraHandler.obtainMessage(action.ordinal(), arg1, arg2, obj).sendToTarget();
    }

    public void runJobSync(Action action) {
        runJobSync(action, 0, 0, null);
    }

    public void runJobSync(Action action, Object obj) {
        runJobSync(action, 0, 0, obj);
    }

    public void runJobSync(Action action, int arg) {
        runJobSync(action, arg, 0, null);
    }

    public void runJobSync(Action action, int arg1, int arg2, Object obj) {
        mCameraHandler.obtainMessage(action.ordinal(), arg1, arg2, obj).sendToTarget();
        waitDone();
    }

    public void clearAllJobs() {
        mCameraHandler.removeCallbacksAndMessages(null);
        mCameraHandler.obtainMessage(Action.ACTION_RELEASE_CAMERA.ordinal()).sendToTarget();
    }

    private void waitDone() {
        final Object lock = new Object();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        };

        mCameraHandler.post(runnable);
        synchronized (lock) {
            try {
                lock.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface ActionCallback {
        void onActionDone(Action action, Object result);
    }

    public enum Action {
        ACTION_OPEN_CAMERA,
        ACTION_RELEASE_CAMERA,
        ACTION_GET_PARAMETERS,
        ACTION_SET_PARAMETERS,
        ACTION_SET_PREVIEW_TEXTURE,
        ACTION_START_PREVIEW,
        ACTION_STOP_PREVIEW,
        ACTION_TAKE_PICTURE,
    }

    private static class ParametersCache {
        private static final String TAG = "ParametersCache";
        private Camera.Parameters mParameters;
        private Camera mCamera;

        public ParametersCache(Camera camera) {
            mCamera = camera;
        }

        public synchronized void invalidate() {
            mParameters = null;
        }

        /**
         * Access parameters from the cache. If cache is empty, block by
         * retrieving parameters directly from Camera, but if cache is present,
         * returns immediately.
         */
        public synchronized Camera.Parameters getBlocking() {
            if (mParameters == null) {
                mParameters = mCamera.getParameters();
                if (mParameters == null) {
                    Log.e(TAG, "Camera object returned null parameters!");
                    throw new IllegalStateException("camera.getParameters returned null");
                }
            }
            return mParameters;
        }
    }

    private static class CameraHandler extends Handler {
        private static final String TAG = "CameraHandler";
        private Camera mCamera;
        private int mCameraId = -1;
        private ParametersCache mParameterCache;

        public CameraHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            final Action action = Action.values()[msg.what];
            Log.d(TAG, "CameraHandler handleMessage action " + action.name() + ", " + mCameraId + " START.");
            final long start = System.currentTimeMillis();
            final long startForCallback = start;
            switch (action) {
                case ACTION_OPEN_CAMERA:
                    final int cameraId = msg.arg1;
                    mCamera = android.hardware.Camera.open(cameraId);
                    if (mCamera != null) {
                        mCameraId = cameraId;
                        mParameterCache = new ParametersCache(mCamera);
                    }
                    break;
                case ACTION_RELEASE_CAMERA:
                    if (mCamera != null) {
                        mCamera.release();
                        mCamera = null;
                        mCameraId = -1;
                    } else {
                        Log.w(TAG, "Releasing camera without any camera opened.");
                    }
                    break;
                case ACTION_GET_PARAMETERS:
                    if (mCamera != null) {
                        Object[] holder = (Object[]) msg.obj;
                        Camera.Parameters parameters = mParameterCache.getBlocking();
                        holder[0] = parameters;
                    }
                    break;
                case ACTION_SET_PARAMETERS:
                    if (mCamera != null) {
                        Camera.Parameters cache = mParameterCache.getBlocking();
                        Camera.Parameters parameters = (Camera.Parameters) msg.obj;
                        cache.unflatten(parameters.flatten());
                        mCamera.setParameters(parameters);
                        mParameterCache.invalidate();
                    }
                    break;
                case ACTION_SET_PREVIEW_TEXTURE: {
                    if (mCamera != null) {
                        try {
                            mCamera.setPreviewTexture((SurfaceTexture) msg.obj);
                        } catch (IOException e) {
                            Log.e(TAG, "Could not set preview texture", e);
                        }
                    }
                    break;
                }
                case ACTION_START_PREVIEW: {
                    if (mCamera != null) {
                        mCamera.startPreview();
                    }
                    break;
                }
                case ACTION_STOP_PREVIEW: {
                    if (mCamera != null) {
                        mCamera.stopPreview();
                    }
                    break;
                }
                case ACTION_TAKE_PICTURE: {
                    final ActionCallback callback = (ActionCallback) msg.obj;
                    if (mCamera != null) {
                        mCamera.takePicture(null, null, new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] data, Camera camera) {
                                long cost = System.currentTimeMillis() - startForCallback;
                                Log.d(TAG, "CameraHandler handleMessage action " + action.name() + " done, cost " + cost + " ms.");
                                callback.onActionDone(Action.ACTION_TAKE_PICTURE, true);
                            }
                        });
                        Log.d(TAG, "CameraHandler handleMessage action " + action.name() + " wait action done.");
                    } else {
                        callback.onActionDone(Action.ACTION_TAKE_PICTURE, false);
                    }
                    break;
                }
                default:
                    break;
            }
            long cost = System.currentTimeMillis() - start;
            Log.d(TAG, "CameraHandler handleMessage action " + action.name() + " END, cost " + cost + " ms.");
        }
    }
}
