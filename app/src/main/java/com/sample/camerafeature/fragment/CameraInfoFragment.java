package com.sample.camerafeature.fragment;

import android.content.Context;
import android.os.Bundle;

public class CameraInfoFragment extends BaseFragment {
    public static final String CAMERA_ID = "camera_id";

    public static CameraInfoFragment newInstance(int cameraId, boolean api2) {
        CameraInfoFragment fragment = new CameraInfoFragment();
        Bundle args = new Bundle();
        args.putInt(CAMERA_ID, cameraId);
        fragment.setArguments(args);
        fragment.init(api2);
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
        Context context = getContext();
        if (context == null) return;
        mCapabilities = mUseApi2 ? new Camera2Capabilities(context, id) : new CameraCapabilities(context, id);
    }
}
