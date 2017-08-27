package com.sample.benchmark;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.sample.common.utils.PermissionUtils;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1002;
    private static String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private OperationDelegate mDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getTitle());
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            if (checkPermissions()) {
                initBenchmarkFragment();
            }
        }

        initOperationButton();
    }

    private void initBenchmarkFragment() {
        final BenchmarkFragment fragment = new BenchmarkFragment();
        mDelegate = fragment;
        getFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
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
            return;
        }
        initBenchmarkFragment();
    }

    private void initOperationButton() {
        FloatingActionButton start = (FloatingActionButton) findViewById(R.id.start_button);
        start.setVisibility(View.VISIBLE);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDelegate != null) {
                    mDelegate.start();
                }
            }
        });

        FloatingActionButton stop = (FloatingActionButton) findViewById(R.id.stop_button);
        stop.setVisibility(View.VISIBLE);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDelegate != null) {
                    mDelegate.stop();
                }
            }
        });
    }
}
