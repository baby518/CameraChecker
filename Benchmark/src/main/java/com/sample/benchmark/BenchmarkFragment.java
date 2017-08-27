package com.sample.benchmark;

import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener, OperationDelegate {
    private static final String TAG = "BenchmarkFragment";
    private static final String KEY_CAMERA_ID = "pref_camera_id";

    private CameraAgent mCameraAgent;
    private ProgressCallback mProgressCallback;
    private boolean mPaused = false;
    private boolean mInitialized = false;
    private int mCameraId = 0;
    private List<Integer> mCameraIdList;
    private TextureView mPreviewTextureView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInitialized = false;
        addPreferencesFromResource(R.xml.benchmark_preferences);

        mCameraAgent = CameraAgent.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.benchmark_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPreviewTextureView = (TextureView) view.findViewById(R.id.preview_texture_view);
        int num = Camera.getNumberOfCameras();
        if (num < 1) {
            Log.w(TAG, "getNumberOfCameras " + num);
            return;
        } else {
            mCameraIdList = new ArrayList<>(2);
            for (int i = 0; i < num; i++) {
                mCameraIdList.add(i);
            }
        }

        ListPreference preference = (ListPreference) findPreference(KEY_CAMERA_ID);
        setEntries(preference);
        preference.setValue(String.valueOf(mCameraId));
        setSummary(preference);

        loadPreference(mCameraId);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPaused = false;
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        mPaused = true;
        stop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        setSummary(preference);
        if (key.equals(KEY_CAMERA_ID)) {
            mCameraId = Integer.parseInt(((ListPreference) preference).getValue());
            loadPreference(mCameraId);
        }
    }

    private void fillEntriesAndSummaries(PreferenceGroup group) {
        for (int i = 0; i < group.getPreferenceCount(); ++i) {
            Preference pref = group.getPreference(i);
            if (pref instanceof PreferenceGroup) {
                fillEntriesAndSummaries((PreferenceGroup) pref);
            }
            setEntries(pref);
            setSummary(pref);
        }
    }

    private void setSummary(Preference preference) {
        if (preference instanceof ListPreference) {
            preference.setSummary(((ListPreference) preference).getEntry());
        }
    }

    private void setEntries(Preference preference) {
        if (!(preference instanceof ListPreference)) {
            return;
        }

        ListPreference listPreference = (ListPreference) preference;
        if (listPreference.getKey().equals(KEY_CAMERA_ID)) {
            setEntriesForSelection(mCameraIdList, listPreference);
        }
    }

    private void setEntriesForSelection(List allEntries, ListPreference preference) {
        if (allEntries == null) {
            return;
        }

        String[] entries = new String[allEntries.size()];
        String[] entryValues = new String[allEntries.size()];
        for (int i = 0; i < allEntries.size(); i++) {
            entries[i] = String.valueOf(allEntries.get(i));
            entryValues[i] = String.valueOf(allEntries.get(i));
        }
        preference.setEntries(entries);
        preference.setEntryValues(entryValues);
    }

    // function of load preference start
    interface CameraOpenCallback {
        void onCameraOpened(Camera.Parameters parameters);
    }

    static class OpenCameraAsyncTask extends AsyncTask<Void, String, Camera.Parameters> {
        int mCameraId;
        CameraOpenCallback mCompleteCallback;
        public OpenCameraAsyncTask(int cameraId, CameraOpenCallback callback) {
            mCameraId = cameraId;
            mCompleteCallback = callback;
        }

        @Override
        protected Camera.Parameters doInBackground(Void... voids) {
            CameraAgent.getInstance().runJob(CameraAgent.Action.ACTION_OPEN_CAMERA, mCameraId);
            final Camera.Parameters[] parametersHolder = new Camera.Parameters[1];
            CameraAgent.getInstance().runJobSync(CameraAgent.Action.ACTION_GET_PARAMETERS, parametersHolder);
            CameraAgent.getInstance().runJobSync(CameraAgent.Action.ACTION_RELEASE_CAMERA);
            return parametersHolder[0];
        }

        @Override
        protected void onPostExecute(Camera.Parameters parameters) {
            if (mCompleteCallback != null) {
                mCompleteCallback.onCameraOpened(parameters);
            }
        }
    }

    private void loadPreference(int cameraId) {
        mInitialized = false;
        final CameraOpenCallback completeCallback = new CameraOpenCallback() {
            @Override
            public void onCameraOpened(Camera.Parameters parameters) {
                onPreferenceLoaded(parameters);
            }
        };
        new OpenCameraAsyncTask(cameraId, completeCallback).execute(null, null, null);
    }

    private void onPreferenceLoaded(Camera.Parameters parameters) {
        fillEntriesAndSummaries(getPreferenceScreen());
        mInitialized = true;
    }
    // function of load preference end


    // function of benchmark start
    private void initParameters(Camera.Parameters parameters) {
        if (parameters == null) return;
    }

    @Override
    public void start() {
        if (mPaused) return;
        int cameraId = mCameraId;

        mCameraAgent.runJob(CameraAgent.Action.ACTION_OPEN_CAMERA, cameraId);
        final Camera.Parameters[] parametersHolder = new Camera.Parameters[1];
        mCameraAgent.runJobSync(CameraAgent.Action.ACTION_GET_PARAMETERS, parametersHolder);

        Camera.Parameters parameters = parametersHolder[0];
        initParameters(parameters);
        mCameraAgent.runJob(CameraAgent.Action.ACTION_SET_PARAMETERS, parameters);
        mCameraAgent.runJob(CameraAgent.Action.ACTION_SET_PREVIEW_TEXTURE, mPreviewTextureView.getSurfaceTexture());
        mCameraAgent.runJob(CameraAgent.Action.ACTION_START_PREVIEW);
        mCameraAgent.runJob(CameraAgent.Action.ACTION_TAKE_PICTURE, new CameraAgent.ActionCallback() {
            @Override
            public void onActionDone(CameraAgent.Action action, Object result) {
                mCameraAgent.runJob(CameraAgent.Action.ACTION_STOP_PREVIEW);
                mCameraAgent.runJob(CameraAgent.Action.ACTION_RELEASE_CAMERA);
            }
        });
    }

    @Override
    public void stop() {
        mCameraAgent.clearAllJobs();
    }

    public void setProgressCallback(ProgressCallback progressCallback) {
        mProgressCallback = progressCallback;
    }
    // function of benchmark end
}
