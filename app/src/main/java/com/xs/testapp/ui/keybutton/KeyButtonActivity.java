package com.xs.testapp.ui.keybutton;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Instrumentation;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;
import com.xs.testapp.R;
import com.xs.testapp.databinding.ActivityKeyButtonBinding;

/**
 *
 */
public class KeyButtonActivity extends AppCompatActivity {

    private ActivityKeyButtonBinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityKeyButtonBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mBinding.btnMenu.setOnClickListener(v -> {
            new Thread(() -> {
                Instrumentation i = new Instrumentation();
//                        i1.sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
                i.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
            }).start();
        });
        mBinding.btnUp.setOnClickListener(v -> {
            new Thread(() -> {
                Instrumentation i = new Instrumentation();
//                        i.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
                i.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, 0x1001));
            }).start();
        });
        mBinding.btnDown.setOnClickListener(v -> {
            new Thread(() -> {
                Instrumentation i = new Instrumentation();
//                i.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
                i.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, 0x1002));
            }).start();
        });
        mBinding.btnLeft.setOnClickListener(v -> {
            new Thread(() -> {
                Instrumentation i = new Instrumentation();
//                i.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
                i.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BUTTON_3));
            }).start();
        });
        mBinding.btnRight.setOnClickListener(v -> {
            new Thread(() -> {
                Instrumentation i = new Instrumentation();
//                i.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
                i.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BUTTON_1));
            }).start();
        });
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