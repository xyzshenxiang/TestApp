package com.xs.testapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.blankj.utilcode.util.LogUtils;

import static java.sql.DriverManager.println;

/**
 * @author xiang.shen
 * @create 2021/07/08
 * @Describe
 */
public class OneFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        LogUtils.d("onCreateView");
        return inflater.inflate(R.layout.fragment_one, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().findViewById(R.id.btn_go1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Navigation.findNavController(v).navigate(R.id.twoFragment);
                sortBigArray();
            }
        });
        getView().findViewById(R.id.btn_go2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.threeFragment);
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