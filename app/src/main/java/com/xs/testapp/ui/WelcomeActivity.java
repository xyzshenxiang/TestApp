package com.xs.testapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.xs.testapp.R;
import com.xs.testapp.watermark.WaterMarkActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void onClickWaterMark(View v) {

        startActivity(new Intent(this, WaterMarkActivity.class));
    }
}