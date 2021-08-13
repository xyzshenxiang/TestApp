package com.xs.testapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.LogUtils;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class TwoFragment extends Fragment {

    Custom mCustom;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        LogUtils.d("onCreateView");
        return inflater.inflate(R.layout.fragment_two, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Navigation.findNavController(v).popBackStack();
//                ObjectAnimator.ofFloat(mCustom,"translationX",0,300).setDuration(500).start();
                mCustom.smoothScrollTo(-200,0);
            }
        });
        mCustom = getView().findViewById(R.id.cv);

//        getView().findViewById(R.id.cv).setAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.translate));
    }

}