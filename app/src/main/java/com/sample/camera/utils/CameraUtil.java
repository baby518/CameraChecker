package com.sample.camera.utils;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.util.Log;
import android.util.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CameraUtil {
    protected static final String TAG = "CameraUtil";

    public static int[] sVideoQualities = new int[] {
            CamcorderProfile.QUALITY_2160P,
            CamcorderProfile.QUALITY_1080P,
            CamcorderProfile.QUALITY_720P,
            CamcorderProfile.QUALITY_480P,
            CamcorderProfile.QUALITY_CIF,
            CamcorderProfile.QUALITY_QVGA,
            CamcorderProfile.QUALITY_QCIF
    };

    public static Size getOptimalSize(List<Size> sizes, double targetRatio) {
        return getOptimalSize(sizes, targetRatio, new Size(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    /** @param maxSize such as screen size. */
    public static Size getOptimalSize(List<Size> sizes, double targetRatio, Size maxSize) {
        int optimalPickIndex = getOptimalSizeIndex(sizes, targetRatio, maxSize);
        if (optimalPickIndex == -1) {
            return null;
        } else {
            return sizes.get(optimalPickIndex);
        }
    }
    
    private static int getOptimalSizeIndex(List<Size> sizes, double targetRatio, Size maxSize) {
        final double aspectRatioTolerance = 0.02;

        int optimalSizeIndex = -1;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(maxSize.getWidth(), maxSize.getHeight());

        // Try to find an size match aspect ratio and size
        for (int i = 0; i < sizes.size(); i++) {
            Size size = sizes.get(i);
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > aspectRatioTolerance) {
                continue;
            }

            double heightDiff = Math.abs(size.getHeight() - targetHeight);
            if (heightDiff < minDiff) {
                optimalSizeIndex = i;
                minDiff = heightDiff;
            } else if (heightDiff == minDiff) {
                // Prefer resolutions smaller-than-display when an equally close
                // larger-than-display resolution is available
                if (size.getHeight() < targetHeight) {
                    optimalSizeIndex = i;
                    minDiff = heightDiff;
                }
            }
        }

        // Cannot find the one match the aspect ratio. This should not happen.
        // Ignore the requirement.
        if (optimalSizeIndex == -1) {
            Log.w(TAG, "No preview size match the aspect ratio. available sizes: " + sizes);
            minDiff = Double.MAX_VALUE;
            for (int i = 0; i < sizes.size(); i++) {
                Size size = sizes.get(i);
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSizeIndex = i;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }

        return optimalSizeIndex;
    }

    public static int getOptimalVideoQuality(int cameraId, List<Size> sizes, double targetRatio) {
        final double aspectRatioTolerance = 0.02;
        for (int quality : sVideoQualities) {
            boolean has = CamcorderProfile.hasProfile(cameraId, quality);
            if (has) {
                CamcorderProfile profile = CamcorderProfile.get(cameraId, quality);
                Size size = new Size(profile.videoFrameWidth, profile.videoFrameHeight);
                double ratio = (double) size.getWidth() / size.getHeight();
                if (Math.abs(ratio - targetRatio) > aspectRatioTolerance) {
                    continue;
                }
                if (sizes.contains(size)) {
                    return quality;
                }
            }
        }
        // default
        return CamcorderProfile.QUALITY_HIGH;
    }

    public static List<Size> convert(List<Camera.Size> sizes) {
        ArrayList<Size> converted = new ArrayList<>(sizes.size());
        for (Camera.Size size : sizes) {
            converted.add(new Size(size.width, size.height));
        }
        return converted;
    }

    public static Size convert(Camera.Size size) {
        return new Size(size.width, size.height);
    }

    public static List<Size> convert(Size[] sizes) {
        return Arrays.asList(sizes);
    }

    public static Size strToSize(String str) {
        if (str == null) return null;

        int pos = str.indexOf('x');
        if (pos != -1) {
            String width = str.substring(0, pos);
            String height = str.substring(pos + 1);
            return new Size(Integer.parseInt(width),
                    Integer.parseInt(height));
        }
        Log.e(TAG, "Invalid size parameter string=" + str);
        return null;
    }

    public static String sizeToStr(Size size) {
        if (size == null) return null;
        return Integer.toString(size.getWidth()) + "x" + Integer.toString(size.getHeight());
    }

    /**
     * Given the camera sensor orientation and device orientation, this returns a clockwise angle
     * which the final image needs to be rotated to be upright on the device screen.
     *
     * @param sensorOrientation Clockwise angle through which the output image needs to be rotated
     *                          to be upright on the device screen in its native orientation.
     * @param deviceOrientation Clockwise angle of the device orientation from its
     *                          native orientation when front camera faces user.
     * @param isFrontCamera True if the camera is front-facing.
     * @return The angle to rotate image clockwise in degrees. It should be 0, 90, 180, or 270.
     */
    public static int getImageRotation(int sensorOrientation,
                                       int deviceOrientation,
                                       boolean isFrontCamera) {
        // The sensor of front camera faces in the opposite direction from back camera.
        if (isFrontCamera) {
            deviceOrientation = (360 - deviceOrientation) % 360;
        }
        return (sensorOrientation + deviceOrientation) % 360;
    }
}
