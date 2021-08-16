package com.xs.testapp.watermark;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.xs.testapp.MainActivity;
import com.xs.testapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WaterMarkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_mark);

        show();
    }

    private void show() {

        SimpleDateFormat createTimeSdf1 = new SimpleDateFormat("yyyy-MM-dd");

        List<String> labels = new ArrayList<>();
        labels.add("用户名：张三");
        labels.add("日期："+ createTimeSdf1.format(new Date()));
        labels.add("不可扩散");

        ViewGroup rootView = findViewById(android.R.id.content);
        FrameLayout layout = new FrameLayout(this);
        layout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setBackground(new WaterMarkBg(WaterMarkActivity.this,labels,-30,13));
        rootView.addView(layout);
    }
}