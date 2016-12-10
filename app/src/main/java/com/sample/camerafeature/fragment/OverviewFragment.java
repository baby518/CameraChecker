package com.sample.camerafeature.fragment;

import android.os.Bundle;

import com.sample.camerafeature.utils.FeatureChecker;

public class OverviewFragment extends BaseFragment {
    public static String getName() {
        return "Overview";
    }

    @Override
    public void onActivityCreated(Bundle paramBundle) {
        FeatureChecker.initialize(getActivity());
        super.onActivityCreated(paramBundle);
    }

    public void initCapabilities() {
        super.initCapabilities();
        this.mCapabilitiesContent = new OverviewCapabilitiesContent(getContext());
        this.mCapabilitiesContent.clearCameraCapabilities();
        this.mCapabilitiesContent.generateCapabilities();
    }
}
