package com.xs.testapp.ui.mqtt;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ZipUtils;
import com.xs.testapp.R;

import java.io.IOException;

/**
 * @author shenxiang
 * @date 2022/9/30
 * @description
 */
public class MqttActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt);

        findViewById(R.id.btn_send).setOnClickListener(v -> {
            MqttHelper.getInstance().sendOpenDoor();
        });

        MqttHelper.getInstance().init();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String srcPath = "/sdcard/Download/abc/安全证书.zip";
                String dstPath = "/sdcard/Download/abc/";
                try {
                    ZipUtils.unzipFile(srcPath,dstPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
