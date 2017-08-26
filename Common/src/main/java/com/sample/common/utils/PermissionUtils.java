package com.sample.common.utils;

import android.app.Activity;
import android.content.pm.PackageManager;

import java.util.ArrayList;

public class PermissionUtils {
    public static boolean checkPermissions(final Activity activity, String[] permissions,
            int operationHandle) {
        if (permissions == null)
            return true;
        boolean isPermissionGranted = true;
        ArrayList<String> permissionList = new ArrayList<String>();
        for (String permission : permissions) {
            if (PackageManager.PERMISSION_GRANTED != activity.checkSelfPermission(permission)) {
                permissionList.add(permission);
                isPermissionGranted = false;
            }
        }

        if (!isPermissionGranted) {
            String[] permissionArray = new String[permissionList.size()];
            permissionList.toArray(permissionArray);
            activity.requestPermissions(permissionArray, operationHandle);
        }

        return isPermissionGranted;
    }

    public static boolean checkPermissionResult(String[] permissions, int[] grantResults) {
        if (permissions == null || grantResults == null || permissions.length == 0
                || grantResults.length == 0) {
            return false;
        }

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
