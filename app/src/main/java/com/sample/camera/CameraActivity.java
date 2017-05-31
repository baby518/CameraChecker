package com.sample.camera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.sample.camerafeature.R;

public class CameraActivity extends AppCompatActivity {
    public static final String KEY_CAMERA_ID = "key_camera_id";
    public static final String KEY_USE_API2 = "key_camera_api2";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_camera);
        super.onCreate(savedInstanceState);
        int cameraId = getIntent().getIntExtra(KEY_CAMERA_ID, 0);
        boolean useApi2 = getIntent().getBooleanExtra(KEY_USE_API2, false);

        if (savedInstanceState == null) {
            final BaseCameraFragment dialog = useApi2 ? new Camera2Fragment() : new CameraFragment();
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_CAMERA_ID, cameraId);
            dialog.setArguments(arguments);
            getFragmentManager().beginTransaction().replace(R.id.content, dialog).commit();
            onBackPressedListener = dialog.getOnBackPressedListener();
        }
    }

    @Override
    public void onBackPressed() {
        boolean ret = false;
        if (onBackPressedListener != null) {
            ret = onBackPressedListener.onBackPressed();
        }
        if (!ret) {
            super.onBackPressed();
        }
    }

    public interface OnBackPressedListener {
        boolean onBackPressed();
    }

    private OnBackPressedListener onBackPressedListener;
}
