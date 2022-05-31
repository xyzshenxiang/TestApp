package com.xs.testapp.ui.navigation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Instrumentation;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.blankj.utilcode.util.LogUtils;
import com.xs.testapp.R;

public class NavigationActivity extends AppCompatActivity {

    private Fragment mFragment = new MainFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.fl_container, mFragment);
        ft.commit();
    }
}