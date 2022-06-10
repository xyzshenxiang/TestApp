package com.xs.testapp.ui.camera;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.xs.testapp.R;

/**
 * @author shenxiang
 */
public class CameraActivity extends AppCompatActivity {

    public static final int CODE = 0x01;

    private CameraFragment mCameraFragment = null;
    private Fragment mCamera2Fragment = null;
    private Fragment mCameraXFragment = null;
    private Fragment mCameraViewFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Camera2Helper.getInstance().init(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Camera2Helper.getInstance().release();
    }

    public void onCamera(View v) {
        if (null == mCameraFragment) {
            mCameraFragment = new CameraFragment();
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_container, mCameraFragment);
        ft.commit();
    }

    public void onCamera2(View v) {
        if (null == mCamera2Fragment) {
            mCamera2Fragment = new Camera2HelperFragment();
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_container, mCamera2Fragment);
        ft.commit();
    }

    public void onCameraX(View v) {
        if (null == mCameraXFragment) {
            mCameraXFragment = new CameraXFragment();
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_container, mCameraXFragment);
        ft.commit();
    }
    public void onCameraView(View v) {
        if (null == mCameraViewFragment) {
            mCameraViewFragment = new CameraViewFragment();
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_container, mCameraViewFragment);
        ft.commit();
    }

}