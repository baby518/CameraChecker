package com.sample.feature.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import com.sample.camerafeature.R;

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
    public String getName(Resources res) {
        Bundle bundle = getArguments();
        int cameraId = (bundle != null ? bundle.getInt(CAMERA_ID) : -1);
        return res.getString(R.string.fragment_title_camera, cameraId);
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
