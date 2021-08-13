package com.xs.testapp;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.blankj.utilcode.util.LogUtils;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend;
import com.xs.testapp.watermark.WaterMarkBg;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.StartStyle);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = findViewById(R.id.pb_progress);

//        Context context;
        ActivityManager ac = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        Log.d("getMemoryClass", String.valueOf(ac.getMemoryClass()));

        Log.d("getLargeMemoryClass", String.valueOf(ac.getLargeMemoryClass()));
        showDeviceInfo();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.fl_container,mFragment);
        ft.commit();

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
        layout.setBackground(new WaterMarkBg(MainActivity.this,labels,-30,13));
        rootView.addView(layout);
    }

    Fragment mFragment = new MainFragment();

    public void showDeviceInfo() {
        Point outSize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(outSize);
        int x = outSize.x;
        int y = outSize.y;
        Log.w("MainActivity", "x = " + x + ",y = " + y);
    }


    public void te(final int temp) {

        Log.d("te", String.valueOf(temp));

        new Thread(new Runnable() {

            @Override
            public void run() {

                LogThread.per();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("Runnable", String.valueOf(temp));
            }
        }).start();

    }

    public void test(View v) {
//        for (int i = 0; i < 5; i++) {
//            te(i);
//        }
        Intent intent = new Intent("com_unistrong_auth_logout");
        intent.putExtra("application_id",BuildConfig.APPLICATION_ID);
        sendBroadcast(intent);
//        startActivity(new Intent(this, Tip.class));
    }

    public void print(View v) {
        LogThread.print();

        int i = 10;
        int j = 5;
        System.out.println("i:" + i + " j:" + j);
        swap(i, j);
        System.out.println("i:" + i + " j:" + j);
    }


    public void swap(int a, int b) {
        int temp = a;
        a = b;
        b = temp;
        System.out.println("a:" + a + " b:" + b);
    }



    public void onStartService(View v) {
        ComponentName service = new ComponentName("com.unistrong.weixin",
                "com.unistrong.weixin.weixinmng.service.WeixinService");

        Intent intent = new Intent();
        intent.setComponent(service);
        startService(intent);
//        startForegroundService(intent);

//        Intent intent = new Intent();
//        intent.setAction("com.unistrong.weixin.weixinmng.service.WeixinService");
//        intent.setClassName("com.unistrong.weixin.weixinmng.service",
//                "com.unistrong.weixin.weixinmng.service.WeixinService");
//        startForegroundService(intent);


    }

    DownloadTask task;

    public void onDownload(View v) {

        File file = Environment.getExternalStoragePublicDirectory("testDownloadApp");
        if (!file.exists())
            file.mkdirs();

        task = new DownloadTask.Builder("http://192.168.28.231:2181/test1.sql", file)
//                .setFilename(filename)
                // the minimal interval millisecond for callback progress
                .setMinIntervalMillisCallbackProcess(30)
                // do re-download even if the task has already been completed in the past.
                .setPassIfAlreadyCompleted(false)
                .build();
        task.enqueue(mDownloadListener4WithSpeed);

    }

    public void onCancel(View v) {
        task.cancel();
    }


    private final DownloadListener4WithSpeed mDownloadListener4WithSpeed = new DownloadListener4WithSpeed() {

        private long totalLength;

        @Override
        public void taskStart(@NonNull DownloadTask task) {
            LogUtils.d("【1、taskStart】");
        }

        @Override
        public void infoReady(@NonNull DownloadTask task, @NonNull BreakpointInfo info, boolean fromBreakpoint, @NonNull Listener4SpeedAssistExtend.Listener4SpeedModel model) {
            totalLength = info.getTotalLength();
            Util.humanReadableBytes(totalLength, true);
            float percent = 0;
            if (totalLength > 0) {
                percent = (float) (info.getTotalOffset() / totalLength) * 100;
            }

            LogUtils.d(("【2、infoReady】当前进度" + percent + "%" + "，" + info.toString()));
        }

        @Override
        public void connectStart(@NonNull DownloadTask task, int blockIndex, @NonNull Map<String, List<String>> requestHeaders) {
            LogUtils.d(("【3、connectStart】" + blockIndex));
        }

        @Override
        public void connectEnd(@NonNull DownloadTask task, int blockIndex, int responseCode, @NonNull Map<String, List<String>> responseHeaders) {
            LogUtils.d(("【4、connectEnd】" + blockIndex + "，" + responseCode));
        }

        @Override
        public void progressBlock(@NonNull DownloadTask task, int blockIndex, long currentBlockOffset, @NonNull SpeedCalculator blockSpeed) {
            LogUtils.d(("【5、progressBlock】" + blockIndex + "，" + currentBlockOffset));
        }

        @Override
        public void progress(@NonNull DownloadTask task, long currentOffset, @NonNull SpeedCalculator taskSpeed) {
            BreakpointInfo info = task.getInfo();
            long totalLength = info.getTotalLength();

            String readableOffset = Util.humanReadableBytes(currentOffset, true);
            String progressStatus = readableOffset + "/" + Util.humanReadableBytes(totalLength, true);
            String speed = taskSpeed.speed();
            float percent = 0;
            if (info.getTotalLength() > 0) {
                percent = currentOffset / (float) totalLength * 100;
            }

            if (currentOffset > 500 * 1024) {
//                task.cancel();
            }

            mProgressBar.setProgress((int) percent);

            LogUtils.d(("【6、progress】" + currentOffset + "[" + progressStatus + "]，速度：" + speed + "，进度：" + percent + "%"));
        }

        @Override
        public void blockEnd(@NonNull DownloadTask task, int blockIndex, BlockInfo info, @NonNull SpeedCalculator blockSpeed) {
            LogUtils.d(("【7、blockEnd】" + blockIndex));
        }

        @Override
        public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull SpeedCalculator taskSpeed) {
            LogUtils.d(("【8、taskEnd】" + cause.name() + "：" + (realCause != null ? realCause.getMessage() : "无异常")));
        }
    };
}