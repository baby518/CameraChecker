package com.sample.camerafeature.fragment;

import android.content.Context;

import com.sample.camerafeature.utils.FeatureChecker;

public class OverviewFragment extends BaseFragment {
    @Override
    public String getName() {
        return "Overview";
    }

    @Override
    public void initCapabilities() {
        Context context = getContext();
        FeatureChecker.initialize(context);
        mCapabilities = new OverviewCapabilities(context);
    }
}
