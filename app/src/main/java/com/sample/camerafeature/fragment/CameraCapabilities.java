package com.sample.camerafeature.fragment;

import android.content.Context;
import android.hardware.Camera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CameraCapabilities extends BaseCameraCapabilities {
    private Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
    private Camera.Parameters mParameters;
    private static HashMap<Integer, Camera.Parameters> ParametersCache = new HashMap<>();
    private static HashMap<Integer, Camera.CameraInfo> InfosCache = new HashMap<>();

    interface CameraOpenListener {
        void onCameraOpened(Camera.Parameters parameters);
    }

    public CameraCapabilities(Context context, int cameraId) {
        super(context, cameraId);
        clearCameraCapabilities();
        mCameraInfo = initCameraInfo(cameraId);
        mParameters = initCameraParameters(cameraId);
        generateCapabilities();
    }

    private static Camera.CameraInfo initCameraInfo(int cameraId) {
        Camera.CameraInfo cache = InfosCache.get(cameraId);
        if (cache != null) {
            return cache;
        }

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        InfosCache.put(cameraId, info);
        return info;
    }

    private static Camera.Parameters initCameraParameters(int cameraId) {
        Camera.Parameters cache = ParametersCache.get(cameraId);
        if (cache != null) {
            return cache;
        }

        // maybe use a thread to open camera.
        try {
            Camera camera = Camera.open(cameraId);
            Camera.Parameters parameters = camera.getParameters();
            camera.release();
            ParametersCache.put(cameraId, parameters);
            return parameters;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initCameraParameters(int cameraId, CameraOpenListener listener) {
        // maybe use a thread to open camera.
        Camera.Parameters parameters = initCameraParameters(cameraId);
        if (listener != null) {
            listener.onCameraOpened(parameters);
        }
    }

    @Override
    protected void generateCapabilities() {
        String cameraDeviceInfoTitle = getCameraDeviceInfoTitle();
        if (cameraDeviceInfoTitle != null) {
            addItem(new CapabilitiesItem(cameraDeviceInfoTitle, getCameraDeviceInfo()));
        }
        addItem(new CapabilitiesItem("CameraFacing", getCameraFacing()));
        addItem(new CapabilitiesItem("Sensor Orientation", getSensorOrientation()));
        addItem(new CapabilitiesItem("Flash Support", isFlashAvailable()));
        addItem(new CapabilitiesItem("PhotoSize-Jpeg", getSupportedJpegPictureSizes()));
        addItem(new CapabilitiesItem("PreviewSize", getSupportedPreviewSizes()));
        addItem(new CapabilitiesItem("VideoSize", getSupportedVideoSizes()));
        addItem(new CapabilitiesItem("MaxZoom", getMaxDigitalZoom()));
        addItem(new CapabilitiesItem("SceneModes", getSupportedSceneModes()));
        addItem(new CapabilitiesItem("ExposureCompensation", getSupportedExposureCompensation()));
        addItem(new CapabilitiesItem("AWBModes", getSupportedAwbModes()));
        addItem(new CapabilitiesItem("AFModes", getSupportedAfModes()));
        addItem(new CapabilitiesItem("ColorEffects", getSupportedColorEffects()));
        addItem(new CapabilitiesItem("PreviewFpsRange", getSupportedPreviewFpsRanges()));
        addItem(new CapabilitiesItem("Antibanding", getSupportedAntibanding()));
//        addItem(new CapabilitiesItem("ISO Range", getSupportedISORange()));
        addItem(new CapabilitiesItem("MaxFaceDetectCount", getMaxFaceDetectCount()));
    }

    private String getCameraFacing() {
        if (mCameraInfo != null) {
            if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return "Back";
            } else if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return "Front";
            }
        }
        return UNKNOWN;
    }

    private String getSensorOrientation() {
        if (mCameraInfo != null) {
            return Integer.toString(mCameraInfo.orientation);
        }
        return UNKNOWN;
    }

    private boolean isFlashAvailable() {
        if (mParameters != null) {
            List<String> supportedFlashModes = mParameters.getSupportedFlashModes();
            return supportedFlashModes != null && supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON);
        }
        return false;
    }

    private List<String> getSupportedJpegPictureSizes() {
        List<String> result = new ArrayList<>();
        if (mParameters != null) {
            List<Camera.Size> list = mParameters.getSupportedPictureSizes();
            if (list != null) {
                for (Camera.Size size : list) {
                    result.add(size.width + "x" + size.height);
                }
            } else {
                result.add(NA);
            }
        }
        return result;
    }

    private List<String> getSupportedPreviewSizes() {
        List<String> result = new ArrayList<>();
        if (mParameters != null) {
            List<Camera.Size> list = mParameters.getSupportedPreviewSizes();
            if (list != null) {
                for (Camera.Size size : list) {
                    result.add(size.width + "x" + size.height);
                }
            } else {
                result.add(NA);
            }
        }
        return result;
    }

    private List<String> getSupportedVideoSizes() {
        List<String> result = new ArrayList<>();
        if (mParameters != null) {
            List<Camera.Size> list = mParameters.getSupportedVideoSizes();
            if (list != null) {
                for (Camera.Size size : list) {
                    result.add(size.width + "x" + size.height);
                }
            } else {
                result.add(NA);
            }
        }
        return result;
    }

    private String getMaxDigitalZoom() {
        if (mParameters != null) {
            List<Integer> zoomRatios = mParameters.getZoomRatios();
            int max = mParameters.getMaxZoom();
            return Float.toString(zoomRatios.get(max) / 100.0f);
        }
        return NA;
    }

    private List<String> getSupportedSceneModes() {
        List<String> result = new ArrayList<>();
        if (mParameters != null) {
            List<String> list = mParameters.getSupportedSceneModes();
            if (list != null) {
                return list;
            } else {
                result.add(NA);
            }
        }
        return result;
    }

    private List<String> getSupportedExposureCompensation() {
        List<String> result = new ArrayList<>();
        if (mParameters != null) {
            int max = mParameters.getMaxExposureCompensation();
            int min = mParameters.getMinExposureCompensation();
            float step = mParameters.getExposureCompensationStep();

            if (max == min) {
                result.add(NA);
            } else {
                for (int i = max; i >= min; i --) {
                    float value = Math.round(i * step * 1000.0F) / 1000.0F;
                    result.add(Float.toString(value));
                }
            }
        }
        return result;
    }

    private List<String> getSupportedAwbModes() {
        List<String> result = new ArrayList<>();
        if (mParameters != null) {
            List<String> list = mParameters.getSupportedWhiteBalance();
            if (list != null) {
                return list;
            } else {
                result.add(NA);
            }
        }
        return result;
    }

    private List<String> getSupportedAfModes() {
        List<String> result = new ArrayList<>();
        if (mParameters != null) {
            List<String> list = mParameters.getSupportedFocusModes();
            if (list != null) {
                return list;
            } else {
                result.add(NA);
            }
        }
        return result;
    }

    private List<String> getSupportedColorEffects() {
        List<String> result = new ArrayList<>();
        if (mParameters != null) {
            List<String> list = mParameters.getSupportedColorEffects();
            if (list != null) {
                return list;
            } else {
                result.add(NA);
            }
        }
        return result;
    }

    /**
     * The values are
     * multiplied by 1000 and represented in integers. For example, if frame
     * rate is 26.623 frames per second, the value is 26623.
     */
    private List<String> getSupportedPreviewFpsRanges() {
        List<String> result = new ArrayList<>();
        if (mParameters != null) {
            List<int[]> list = mParameters.getSupportedPreviewFpsRange();
            if (list != null) {
                for (int[] array : list) {
                    result.add(Arrays.toString(array));
                }
            } else {
                result.add(NA);
            }
        }
        return result;
    }

    private List<String> getSupportedAntibanding() {
        List<String> result = new ArrayList<>();
        if (mParameters != null) {
            List<String> list = mParameters.getSupportedAntibanding();
            if (list != null) {
                return list;
            } else {
                result.add(NA);
            }
        }
        return result;
    }

    private List<String> getSupportedISORange() {
        List<String> result = new ArrayList<>();
        return result;
    }

    private String getMaxFaceDetectCount() {
        if (mParameters != null) {
            return Integer.toString(mParameters.getMaxNumDetectedFaces());
        }
        return NA;
    }
}
