package com.sample.feature;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.sample.camera.CameraActivity;
import com.sample.camerafeature.R;
import com.sample.feature.fragment.BaseFragment;
import com.sample.feature.fragment.CameraInfoFragment;
import com.sample.feature.fragment.OverviewFragment;
import com.sample.preference.SettingsManager;
import com.sample.feature.utils.ProcCameraInfoParse;
import com.sample.feature.utils.Shell;
import com.sample.utils.PermissionUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private SettingsManager mSettingsManager;
    private boolean mUseCameraApi2 = false;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPreferenceManager();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                initOpenCameraButton(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        ProcCameraInfoParse.parseAndSave(mSettingsManager);

        if (checkPermissions()) {
            initSectionsPagerAdapter(mUseCameraApi2);
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            return PermissionUtils.checkPermissions(this, PERMISSION_REQUEST_CODE);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (!PermissionUtils.checkPermissionResult(permissions, grantResults)) {
            Snackbar snackbar = Snackbar.make(mViewPager, getString(R.string.permission_tips), Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        initSectionsPagerAdapter(mUseCameraApi2);
    }

    private void initOpenCameraButton(int position) {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        final int cameraId = mSectionsPagerAdapter.getCameraId(position);

        if (cameraId >= 0) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openCamera(cameraId, mSectionsPagerAdapter.isUseApi2());
                }
            });
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setOnClickListener(null);
            fab.setVisibility(View.GONE);
        }
    }
    
    private void openCamera(int cameraId, boolean useApi2) {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_CAMERA_ID, cameraId);
        intent.putExtra(CameraActivity.KEY_USE_API2, useApi2);
        startActivity(intent);
    }

    private void initPreferenceManager() {
        mSettingsManager = SettingsManager.getInstance(getApplicationContext());
        mUseCameraApi2 = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                SettingsManager.KEY_USE_CAMERA2, false);
    }

    private void initSectionsPagerAdapter(boolean api2) {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), api2);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    private void reloadSectionsPagerAdapter(boolean api2) {
        if (mSectionsPagerAdapter != null) {
            mSectionsPagerAdapter.changeToApi2(api2);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        boolean isChecked = mUseCameraApi2;
        MenuItem item = menu.findItem(R.id.action_use_camera2);
        if (item != null) {
            item.setChecked(isChecked);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null) {
            if (item.isCheckable()) {
                item.setChecked(!item.isChecked());
            }

            int id = item.getItemId();
            if (id == R.id.action_use_camera2) {
                onCameraChecked(item);
                return true;
            } else if (id == R.id.action_cat_camera_info) {
                String result = Shell.exec("cat /proc/camerainfo");
                if (result == null || result.equals("")) {
                    result = getString(R.string.need_root_tips);
                }
                new AlertDialog.Builder(this)
                        .setTitle(R.string.action_cat_camera_info)
                        .setMessage(result)
                        .setPositiveButton(R.string.dialog_dismiss,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                    }
                                })
                        .show();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void onCameraChecked(MenuItem item) {
        boolean api2 = item.isChecked();
        if (mUseCameraApi2 != api2) {
            mUseCameraApi2 = api2;
            reloadSectionsPagerAdapter(mUseCameraApi2);
        }
        mSettingsManager.set(SettingsManager.SCOPE_GLOBAL, SettingsManager.KEY_USE_CAMERA2,
                item.isChecked());
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private CameraManager mCameraManager;
        private ArrayList<BaseFragment> fragmentList = new ArrayList<>();
        private boolean mUseApi2 = false;

        public SectionsPagerAdapter(FragmentManager fm, boolean api2) {
            super(fm);
            fragmentList.add(OverviewFragment.newInstance(api2));
            int num = 0;
            mUseApi2 = api2;
            if (api2) {
                mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                try {
                    num = mCameraManager.getCameraIdList().length;
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            } else {
                num = Camera.getNumberOfCameras();
            }
            for (int i = 0; i < num; i++) {
                CameraInfoFragment fragment = CameraInfoFragment.newInstance(i, api2);
                fragmentList.add(fragment);
            }
        }

        public void changeToApi2(boolean api2) {
            mUseApi2 = api2;
            if (fragmentList != null) {
                for (BaseFragment fragment : fragmentList) {
                    fragment.setApi2(api2);
                }
            }
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentList.get(position).getName(getResources());
        }

        public boolean isUseApi2() {
            return mUseApi2;
        }

        public int getCameraId(int position) {
            // because first page is Overview fragment.
            return position - 1;
        }
    }
}
