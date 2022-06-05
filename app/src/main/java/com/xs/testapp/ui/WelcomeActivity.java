package com.xs.testapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;
import com.xs.testapp.R;
import com.xs.testapp.ui.anim.AnimationActivity;
import com.xs.testapp.ui.camera.CameraActivity;
import com.xs.testapp.ui.keybutton.KeyButtonActivity;
import com.xs.testapp.ui.navigation.NavigationActivity;
import com.xs.testapp.ui.watermark.WaterMarkActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

    }

    public void onClickOpenNavigation(View v) {

        startActivity(new Intent(this, NavigationActivity.class));
    }

    public void onClickWaterMark(View v) {

        startActivity(new Intent(this, WaterMarkActivity.class));
    }

    public void onClickOpenCamera(View v) {

        startActivity(new Intent(this, CameraActivity.class));
    }
    public void onClickAnimation(View v) {

        startActivity(new Intent(this, AnimationActivity.class));
    }

    public void onClickKey(View v) {

        startActivity(new Intent(this, KeyButtonActivity.class));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        LogUtils.d("dispatchKeyEvent",event);
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtils.d("onKeyDown",event);
        return super.onKeyDown(keyCode,event);
    }
}