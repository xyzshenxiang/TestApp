package com.xs.testapp.ui.navigation;

import android.app.Instrumentation;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.blankj.utilcode.util.LogUtils;
import com.xs.testapp.R;
import com.xs.testapp.databinding.FragmentOneBinding;
import com.xs.testapp.ui.base.BaseFragment;

import static java.sql.DriverManager.println;

/**
 * @author xiang.shen
 * @create 2021/07/08
 * @Describe
 */
public class OneFragment extends BaseFragment {

    private FragmentOneBinding mBinding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentOneBinding.inflate(inflater,container,false);
        LogUtils.d("onCreateView");
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.btnGo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Navigation.findNavController(v).navigate(R.id.twoFragment);
//                sortBigArray();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Instrumentation i1 = new Instrumentation();
//                        i1.sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
                        i1.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
                    }
                }).start();
            }
        });
        mBinding.btnGo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Navigation.findNavController(v).navigate(R.id.threeFragment);
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Instrumentation i1 = new Instrumentation();
//                        i1.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
                        i1.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
                    }
                }).start();
            }
        });
        mBinding.btnGo3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Navigation.findNavController(v).navigate(R.id.threeFragment);
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Instrumentation i1 = new Instrumentation();
//                        i1.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
//                        i1.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.UP));
                    }
                }).start();
            }
        });
        mBinding.btnGo4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Navigation.findNavController(v).navigate(R.id.threeFragment);
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Instrumentation i1 = new Instrumentation();
                        i1.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
//                i1.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
                    }
                }).start();
            }
        });
    }

    private void sortBigArray() {
        long currTime = System.currentTimeMillis();
        int[] random = new int[1000000];
        for (int i = 0; i <random.length; i++) {
            random[i] = (int) (Math.random() * 10000000);
        }

        sort(random);
        println("耗时" + (System.currentTimeMillis() - currTime) + "ms");
        for (int temp: random) {
            System.out.println(temp);
        }
    }

    private void sort(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int num = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = num;
                }
            }
        }
    }
}