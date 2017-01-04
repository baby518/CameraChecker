package com.sample.camerafeature.fragment;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.media.MediaRecorder;
import android.util.Range;
import android.util.Rational;
import android.util.Size;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Camera2Capabilities extends BaseCapabilities {
    private String mCameraId;
    private CameraCharacteristics mCharacteristics;

    public Camera2Capabilities(Context context, int cameraId) {
        mCameraId = Integer.toString(cameraId);
        mCharacteristics = initCameraManager(context);
        clearCameraCapabilities();
        generateCapabilities();
    }

    private CameraCharacteristics initCameraManager(Context context) {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            return manager.getCameraCharacteristics(mCameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void generateCapabilities() {
//        addItem(new CapabilitiesItem("Capabilities", getAllSupportCapabilities()));
        addItem(new CapabilitiesItem("CameraFacing", getCameraFacing()));
        addItem(new CapabilitiesItem("HardwareLevel", getCameraHardwareLevel()));
        addItem(new CapabilitiesItem("RequestAvailable", getRequestAvailableCapabilities()));
        addItem(new CapabilitiesItem("Sensor Orientation", getSensorOrientation()));
        addItem(new CapabilitiesItem("Flash Support", isFlashAvailable()));
        addItem(new CapabilitiesItem("FocalLengths", getSupportedFocalLengths()));
        addItem(new CapabilitiesItem("MinimumFocalDistance", getMinFocalDistance()));
        addItem(new CapabilitiesItem("Apertures", getSupportedApertures()));
        addItem(new CapabilitiesItem("OpticalStabilization", getSupportedLensOISMode()));
        addItem(new CapabilitiesItem("VideoStabilization", getSupportedVideoStabilization()));
        addItem(new CapabilitiesItem("PhotoSize-Jpeg", getSupportedJpegPictureSizes()));
        addItem(new CapabilitiesItem("PreviewSize", getSupportedPreviewSizes()));
        addItem(new CapabilitiesItem("VideoSize", getSupportedVideoSizes()));
        addItem(new CapabilitiesItem("MaxZoom", getMaxDigitalZoom()));
        addItem(new CapabilitiesItem("SceneModes", getSupportedSceneModes()));
        addItem(new CapabilitiesItem("ExposureCompensation", getSupportedExposureCompensation()));
        addItem(new CapabilitiesItem("AWBModes", getSupportedAwbModes()));
        addItem(new CapabilitiesItem("AFModes", getSupportedAfModes()));
        addItem(new CapabilitiesItem("AEModes", getSupportedAeMode()));
        addItem(new CapabilitiesItem("ColorEffects", getSupportedColorEffects()));
        addItem(new CapabilitiesItem("PreviewFpsRange", getSupportedPreviewFpsRanges()));
        addItem(new CapabilitiesItem("Antibanding", getSupportedAntibanding()));
        addItem(new CapabilitiesItem("ISO Range", getISORange()));
        addItem(new CapabilitiesItem("ExposureTime range", getExposureTimeRange()));
        addItem(new CapabilitiesItem("FaceDetectModes", getSupportFaceDetectModes()));
        addItem(new CapabilitiesItem("MaxFaceDetectCount", getMaxFaceDetectCount()));
    }

    private List<String> getAllSupportCapabilities() {
        List<String> result = new ArrayList<>();
        if (mCharacteristics != null) {
            List<CameraCharacteristics.Key<?>> keys = mCharacteristics.getKeys();
            for (CameraCharacteristics.Key<?> key : keys) {
                result.add(key.getName());
            }
        } else {
            result.add(NA);
        }
        return result;
    }

    private static List<String> getSupportedList(int[] array, HashMap<Integer, String> MAP) {
        List<String> result = new ArrayList<>();
        if (array != null) {
            for (int i : array) {
                if (MAP.containsKey(i)) {
                    result.add(MAP.get(i));
                }
            }
        } else {
            result.add(NA);
        }
        return result;
    }

    private String getCameraFacing() {
        if (mCharacteristics != null) {
            int facing = mCharacteristics.get(CameraCharacteristics.LENS_FACING);
            if (facing == CameraMetadata.LENS_FACING_FRONT) {
                return "Front";
            } else if (facing == CameraMetadata.LENS_FACING_BACK) {
                return "Back";
            } else if (facing == CameraMetadata.LENS_FACING_EXTERNAL) {
                return "External";
            }
        }
        return UNKNOWN;
    }

    private String getCameraHardwareLevel() {
        if (mCharacteristics != null) {
            int level = mCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            if (level == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED) {
                return "Limited";
            } else if (level == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL) {
                return "Full";
            } else if (level == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                return "Legacy";
            } else if (level == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3) {
                return "Level 3";
            }
        }
        return UNKNOWN;
    }

    private List<String> getRequestAvailableCapabilities() {
        HashMap<Integer, String> MAP = new HashMap();
        MAP.put(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE, "BACKWARD_COMPATIBLE");
        MAP.put(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR, "MANUAL_SENSOR");
        MAP.put(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING, "MANUAL_POST_PROCESSING");
        MAP.put(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW, "RAW");
        MAP.put(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING, "PRIVATE_REPROCESSING");
        MAP.put(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS, "READ_SENSOR_SETTINGS");
        MAP.put(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE, "BURST_CAPTURE");
        MAP.put(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING, "YUV_REPROCESSING");
        MAP.put(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT, "DEPTH_OUTPUT");
        MAP.put(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO, "CONSTRAINED_HIGH_SPEED_VIDEO");

        int[] array = mCharacteristics == null ? null : mCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
        return getSupportedList(array, MAP);
    }

    private String getSensorOrientation() {
        if (mCharacteristics != null) {
            int orientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            return Integer.toString(orientation);
        }
        return UNKNOWN;
    }

    private boolean isFlashAvailable() {
        if (mCharacteristics != null) {
            return mCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        }
        return false;
    }

    private List<String> getSupportedFocalLengths() {
        List<String> result = new ArrayList<>();
        if (mCharacteristics != null) {
            float[] array = mCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
            if (array != null) {
                for (float f : array) {
                    result.add(f + " mm");
                }
            } else {
                result.add(NA);
            }
        }
        return result;
    }

    private String getMinFocalDistance() {
        if (mCharacteristics != null) {
            Float valve = mCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
            if (valve != null) {
                return valve.toString();
            }
        }
        return NA;
    }

    private List<String> getSupportedApertures() {
        List<String> result = new ArrayList<>();
        if (mCharacteristics != null) {
            float[] array = mCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);
            if (array != null) {
                for (float f : array) {
                    result.add("F" + f);
                }
            } else {
                result.add(NA);
            }
        }
        return result;
    }

    private List<String> getSupportedLensOISMode() {
        HashMap<Integer, String> MAP = new HashMap();
        MAP.put(CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF, "MODE_OFF");
        MAP.put(CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON, "MODE_ON");

        int[] array = mCharacteristics == null ? null : mCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
        return getSupportedList(array, MAP);
    }

    private List<String> getSupportedVideoStabilization() {
        HashMap<Integer, String> MAP = new HashMap();
        MAP.put(CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF, "MODE_OFF");
        MAP.put(CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON, "MODE_ON");

        int[] array = mCharacteristics == null ? null : mCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
        return getSupportedList(array, MAP);
    }

    private List<String> getSupportedJpegPictureSizes() {
        List<String> result = new ArrayList<>();
        if (mCharacteristics != null) {
            Size[] array = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            if (array != null) {
                for (Size size : array) {
                    result.add(size.getWidth() + "x" + size.getHeight());
                }
            } else {
                result.add(NA);
            }
        }
        return result;
    }

    private List<String> getSupportedPreviewSizes() {
        List<String> result = new ArrayList<>();
        if (mCharacteristics != null) {
            Size[] array = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(SurfaceTexture.class);
            if (array != null) {
                for (Size size : array) {
                    result.add(size.getWidth() + "x" + size.getHeight());
                }
            } else {
                result.add(NA);
            }
        }
        return result;
    }

    private List<String> getSupportedVideoSizes() {
        List<String> result = new ArrayList<>();
        if (mCharacteristics != null) {
            Size[] array = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(MediaRecorder.class);
            if (array != null) {
                for (Size size : array) {
                    result.add(size.getWidth() + "x" + size.getHeight());
                }
            } else {
                result.add(NA);
            }
        }
        return result;
    }

    private String getMaxDigitalZoom() {
        if (mCharacteristics != null) {
            return Float.toString((mCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)).floatValue());
        }
        return NA;
    }

    private List<String> getSupportedSceneModes() {
        HashMap<Integer, String> MAP = new HashMap();
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_DISABLED, "DISABLED");
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_FACE_PRIORITY, "FACE_PRIORITY");
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_ACTION, "ACTION");
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_PORTRAIT, "PORTRAIT");
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_LANDSCAPE, "LANDSCAPE");
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_NIGHT, "NIGHT");
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_NIGHT_PORTRAIT, "NIGHT_PORTRAIT");
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_THEATRE, "THEATRE");
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_BEACH, "BEACH");
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_SNOW, "SNOW");
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_SUNSET, "SUNSET");
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_STEADYPHOTO, "STEADYPHOTO");
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_FIREWORKS, "FIREWORKS");
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_SPORTS, "SPORTS");
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_PARTY, "PARTY");
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_CANDLELIGHT, "CANDLELIGHT");
        MAP.put(CameraMetadata.CONTROL_SCENE_MODE_BARCODE, "BARCODE");

        int[] array = mCharacteristics == null ? null : mCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES);
        return getSupportedList(array, MAP);
    }

    private List<String> getSupportedExposureCompensation() {
        List<String> result = new ArrayList<>();
        if (mCharacteristics != null) {
            Range range = mCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
            if (range != null) {
                int max = (int) range.getUpper();
                int min = (int) range.getLower();
                Rational rational = mCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP);
                if (rational != null) {
                    float step = rational.floatValue();
                    for (int i = max; i >= min; i--) {
                        float value = Math.round(i * step * 1000.0F) / 1000.0F;
                        result.add(Float.toString(value));
                    }
                }
            }
        }
        return result;
    }

    private List<String> getSupportedAwbModes() {
        HashMap<Integer, String> MAP = new HashMap();
        MAP.put(CameraMetadata.CONTROL_AWB_MODE_OFF, "OFF");
        MAP.put(CameraMetadata.CONTROL_AWB_MODE_AUTO, "AUTO");
        MAP.put(CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT, "INCANDESCENT");
        MAP.put(CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT, "FLUORESCENT");
        MAP.put(CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT, "WARM_FLUORESCENT");
        MAP.put(CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT, "DAYLIGHT");
        MAP.put(CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT, "CLOUDY_DAYLIGHT");
        MAP.put(CameraMetadata.CONTROL_AWB_MODE_TWILIGHT, "TWILIGHT");
        MAP.put(CameraMetadata.CONTROL_AWB_MODE_SHADE, "SHADE");

        int[] array = mCharacteristics == null ? null : mCharacteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
        return getSupportedList(array, MAP);
    }

    private List<String> getSupportedAfModes() {
        HashMap<Integer, String> MAP = new HashMap();
        MAP.put(CameraMetadata.CONTROL_AF_MODE_OFF, "OFF");
        MAP.put(CameraMetadata.CONTROL_AF_MODE_AUTO, "AUTO");
        MAP.put(CameraMetadata.CONTROL_AF_MODE_MACRO, "MACRO");
        MAP.put(CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO, "CONTINUOUS_VIDEO");
        MAP.put(CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE, "CONTINUOUS_PICTURE");
        MAP.put(CameraMetadata.CONTROL_AF_MODE_EDOF, "EDOF");

        int[] array = mCharacteristics == null ? null : mCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        return getSupportedList(array, MAP);
    }

    private List<String> getSupportedAeMode() {
        HashMap<Integer, String> MAP = new HashMap();
        MAP.put(CameraMetadata.CONTROL_AE_MODE_OFF, "OFF");
        MAP.put(CameraMetadata.CONTROL_AE_MODE_ON, "ON");
        MAP.put(CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH, "ON_AUTO_FLASH");
        MAP.put(CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH, "ON_ALWAYS_FLASH");
        MAP.put(CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE, "ON_AUTO_FLASH_REDEYE");

        int[] array = mCharacteristics == null ? null : mCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
        return getSupportedList(array, MAP);
    }

    private List<String> getSupportedColorEffects() {
        HashMap<Integer, String> MAP = new HashMap();
        MAP.put(CameraMetadata.CONTROL_EFFECT_MODE_OFF, "OFF");
        MAP.put(CameraMetadata.CONTROL_EFFECT_MODE_MONO, "MONO");
        MAP.put(CameraMetadata.CONTROL_EFFECT_MODE_NEGATIVE, "NEGATIVE");
        MAP.put(CameraMetadata.CONTROL_EFFECT_MODE_SOLARIZE, "SOLARIZE");
        MAP.put(CameraMetadata.CONTROL_EFFECT_MODE_SEPIA, "SEPIA");
        MAP.put(CameraMetadata.CONTROL_EFFECT_MODE_POSTERIZE, "POSTERIZE");
        MAP.put(CameraMetadata.CONTROL_EFFECT_MODE_WHITEBOARD, "WHITEBOARD");
        MAP.put(CameraMetadata.CONTROL_EFFECT_MODE_BLACKBOARD, "BLACKBOARD");
        MAP.put(CameraMetadata.CONTROL_EFFECT_MODE_AQUA, "AQUA");

        int[] array = mCharacteristics == null ? null : mCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS);
        return getSupportedList(array, MAP);
    }

    /** <p><b>Units</b>: Frames per second (FPS)</p> */
    private List<String> getSupportedPreviewFpsRanges() {
        List<String> result = new ArrayList<>();
        if (mCharacteristics != null) {
            Range[] array = mCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            if (array != null) {
                for (Range range : array) {
                    result.add(range.getLower() + ", " + range.getUpper());
                }
            } else {
                result.add(NA);
            }
        }
        return result;
    }

    private List<String> getSupportedAntibanding() {
        HashMap<Integer, String> MAP = new HashMap();
        MAP.put(CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_OFF, "OFF");
        MAP.put(CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_50HZ, "50HZ");
        MAP.put(CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_60HZ, "60HZ");
        MAP.put(CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_AUTO, "AUTO");

        int[] array = mCharacteristics == null ? null : mCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);
        return getSupportedList(array, MAP);
    }

    private String getISORange() {
        if (mCharacteristics != null) {
            Range range = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
            if (range != null) {
                return range.toString();
            }
        }
        return NA;
    }

    private String getExposureTimeRange() {
        if (mCharacteristics != null) {
            Range range = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
            if (range != null) {
                return range.toString();
            }
        }
        return NA;
    }

    private List<String> getSupportFaceDetectModes() {
        HashMap<Integer, String> MAP = new HashMap();
        MAP.put(CameraMetadata.STATISTICS_FACE_DETECT_MODE_OFF, "OFF");
        MAP.put(CameraMetadata.STATISTICS_FACE_DETECT_MODE_SIMPLE, "SIMPLE");
        MAP.put(CameraMetadata.STATISTICS_FACE_DETECT_MODE_FULL, "FULL");

        int[] array = mCharacteristics == null ? null : mCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
        return getSupportedList(array, MAP);
    }

    private String getMaxFaceDetectCount() {
        if (mCharacteristics != null) {
            int count = mCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT);
            return Integer.toString(count);
        }
        return NA;
    }
}
