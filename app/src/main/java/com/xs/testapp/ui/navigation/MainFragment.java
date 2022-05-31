package com.xs.testapp.ui.navigation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import com.blankj.utilcode.util.LogUtils;
import com.xs.testapp.R;
import com.xs.testapp.databinding.FragmentMainBinding;
import com.xs.testapp.ui.base.BaseFragment;


public class MainFragment extends BaseFragment {

    private FragmentMainBinding mBinding;
    private ObjectAnimator mAnimator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentMainBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mBinding.btnGo1.setOnClickListener(v -> {
            FragmentManager fm = getChildFragmentManager();
            NavHostFragment nhf = (NavHostFragment) fm.findFragmentById(R.id.nav_host);
            nhf.getNavController().navigate(R.id.oneFragment);
//                Navigation.findNavController(getView()).navigate(R.id.oneFragment);
//                NavHostFragment.findNavController(getView()).navigate(R.id.oneFragment);

        });
        mBinding.btnGo2.setOnClickListener(v -> {
            FragmentManager fm = getChildFragmentManager();
            NavHostFragment nhf = (NavHostFragment) fm.findFragmentById(R.id.nav_host);
            nhf.getNavController().navigate(R.id.twoFragment);
//                Navigation.findNavController(getView()).navigate(R.id.twoFragment);
//                NavHostFragment.findNavController(mFragment).navigate(R.id.twoFragment);

        });
        mBinding.btnGo3.setOnClickListener(v -> {
            FragmentManager fm = getChildFragmentManager();
            NavHostFragment nhf = (NavHostFragment) fm.findFragmentById(R.id.nav_host);
            nhf.getNavController().navigate(R.id.threeFragment);
//                Navigation.findNavController(getView()).navigate(R.id.threeFragment);
//                NavHostFragment.findNavController(mFragment).navigate(R.id.threeFragment);
        });

        mBinding.btnHide.setOnClickListener(v -> {
            mAnimator.start();
        });

        mAnimator = ObjectAnimator.ofFloat(mBinding.btnHide,"alpha", 0);
        mAnimator.setDuration(1000);
        mAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                LogUtils.d("onAnimationEnd");
                mBinding.btnHide.setVisibility(View.GONE);
            }
        });
    }
}