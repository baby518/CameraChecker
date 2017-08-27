package com.sample.benchmark;

public interface ProgressCallback {
    void onStart();
    void onFinish();
    void onJobComplete(String title, String result);
}
