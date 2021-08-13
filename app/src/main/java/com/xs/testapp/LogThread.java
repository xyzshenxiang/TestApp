package com.xs.testapp;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiang.shen
 * @create 2020/10/13
 * @Describe
 */
public class LogThread {

    private static final List<String> mArrayList = new ArrayList<String>();

    public static void per() {
        mArrayList.add(Thread.currentThread().getName());
    }

    public static void print() {
        for (String str : mArrayList) {
            Log.d("LogThread", str);
        }
    }
}