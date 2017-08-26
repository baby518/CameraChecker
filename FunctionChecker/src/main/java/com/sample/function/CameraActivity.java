package com.sample.function;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import com.sample.common.utils.PermissionUtils;

public class CameraActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1002;
    private static String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    };
    public static final String KEY_CAMERA_ID = "key_camera_id";
    public static final String KEY_USE_API2 = "key_camera_api2";
    private boolean mUseApi2 = false;
    private int mCameraId = 0;
    private OnBackPressedListener mOnBackPressedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_camera);
        super.onCreate(savedInstanceState);
        mCameraId = getIntent().getIntExtra(KEY_CAMERA_ID, 0);
        mUseApi2 = getIntent().getBooleanExtra(KEY_USE_API2, false);

        if (savedInstanceState == null) {
            if (checkPermissions()) {
                initCameraFragment();
            }
        }
    }

    private void initCameraFragment() {
        final BaseCameraFragment dialog = mUseApi2 ? new Camera2Fragment() : new CameraFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(KEY_CAMERA_ID, mCameraId);
        dialog.setArguments(arguments);
        getFragmentManager().beginTransaction().replace(R.id.content, dialog).commit();
        mOnBackPressedListener = dialog.getOnBackPressedListener();
    }

    @Override
    public void onBackPressed() {
        boolean ret = false;
        if (mOnBackPressedListener != null) {
            ret = mOnBackPressedListener.onBackPressed();
        }
        if (!ret) {
            super.onBackPressed();
        }
    }

    public interface OnBackPressedListener {
        boolean onBackPressed();
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            return PermissionUtils.checkPermissions(this, CAMERA_PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (!PermissionUtils.checkPermissionResult(permissions, grantResults)) {
            Snackbar snackbar = Snackbar.make(getWindow().getDecorView(),
                    getString(R.string.permission_tips), Snackbar.LENGTH_LONG);
            snackbar.show();
        } else {
            initCameraFragment();
        }
    }
}
