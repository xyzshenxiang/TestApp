package com.xs.testapp.ui.navigation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.LogUtils;
import com.xs.testapp.Custom;
import com.xs.testapp.R;
import com.xs.testapp.databinding.FragmentTwoBinding;
import com.xs.testapp.ui.base.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class TwoFragment extends BaseFragment {

    private FragmentTwoBinding mBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LogUtils.d("onCreateView");
        mBinding = FragmentTwoBinding.inflate(inflater,container,false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Navigation.findNavController(v).popBackStack();
//                ObjectAnimator.ofFloat(mCustom,"translationX",0,300).setDuration(500).start();
                mBinding.custom.smoothScrollTo(-200,0);
            }
        });

    }

}