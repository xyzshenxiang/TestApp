package com.xs.testapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

/**
 * @author xiang.shen
 * @create 2021/07/12
 * @Describe
 */
public class InitializeService extends IntentService {

    public static final String ACTION_INIT_APP_CREATE = "com.example.testapp.init";

    public static void start(Context context) {
        Intent intent = new Intent(context, InitializeService.class);
        intent.setAction(ACTION_INIT_APP_CREATE);
        context.startService(intent);
    }

    public InitializeService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (null != intent && ACTION_INIT_APP_CREATE.equals(intent.getAction())) {
            //加载第三方库，或者初始化
        }
    }
}