package com.sample.camerafeature.fragment;

public class OverviewFragment extends BaseFragment {
    public static String getName() {
        return "Overview";
    }

    public void initCapabilities() {
        super.initCapabilities();
        this.mCapabilitiesContent = new OverviewCapabilitiesContent(getContext());
        this.mCapabilitiesContent.clearCameraCapabilities();
        this.mCapabilitiesContent.generateCapabilities();
    }
}
