package com.sample.camerafeature;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.sample.camerafeature.fragment.BaseFragment;
import com.sample.camerafeature.fragment.CameraInfoFragment;
import com.sample.camerafeature.fragment.OverviewFragment;
import com.sample.camerafeature.utils.SettingsManager;
import com.sample.camerafeature.utils.ProcCameraInfoParse;
import com.sample.camerafeature.utils.Shell;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
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

        initSectionsPagerAdapter(mUseCameraApi2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ProcCameraInfoParse.parseAndSave(mSettingsManager);
    }

    private void initPreferenceManager() {
        mSettingsManager = SettingsManager.getInstance(this);
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

        public SectionsPagerAdapter(FragmentManager fm, boolean api2) {
            super(fm);
            fragmentList.add(OverviewFragment.newInstance(api2));
            int num = 0;
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
    }
}
