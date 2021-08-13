package com.xs.testapp;

import android.app.Application;

import com.wanjian.cockroach.Cockroach;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @author xiang.shen
 * @create 2021/07/12
 * @Describe
 */
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

//        InitializeService.start(this);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.level(HttpLoggingInterceptor.Level.BODY);
        if (!BuildConfig.DEBUG) {
            logging.redactHeader("Authorization");
            logging.redactHeader("Cookie");
        }
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS);


        Cockroach.install(new Cockroach.ExceptionHandler() {
            @Override
            public void handlerException(Thread thread, Throwable throwable) {
                throwable.printStackTrace();

//                CrashUtil.saveCrashReport(throwable);
            }
        });

    }
}