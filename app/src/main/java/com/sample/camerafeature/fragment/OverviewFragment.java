package com.sample.camerafeature.fragment;

import android.content.Context;

import com.sample.camerafeature.utils.FeatureChecker;

public class OverviewFragment extends BaseFragment {
    public static OverviewFragment newInstance(boolean api2) {
        OverviewFragment fragment = new OverviewFragment();
        fragment.init(api2);
        return fragment;
    }

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

    @Override
    public void onApiLevelChanged() {
    }
}
