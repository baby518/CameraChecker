package com.sample.camerafeature.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

public class CameraInfoFragment extends BaseFragment {
    public static final String CAMERA_ID = "camera_id";
    private int mCameraId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static CameraInfoFragment newInstance(int cameraId) {
        CameraInfoFragment fragment = new CameraInfoFragment();
        Bundle args = new Bundle();
        args.putInt(CAMERA_ID, cameraId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getName() {
        Bundle bundle = getArguments();
        return "Camera " + (bundle != null ? bundle.getInt(CAMERA_ID) : -1);
    }

    @Override
    public void initCapabilities() {
        Bundle bundle = getArguments();
        if (bundle == null) return;

        int id = bundle.getInt(CAMERA_ID);
        mCapabilities = new CameraCapabilities(id);
    }
}
