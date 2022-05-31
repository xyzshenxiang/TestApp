package com.xs.testapp.ui.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.xs.testapp.databinding.FragmentAnimationBinding;
import com.xs.testapp.ui.base.BaseFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shenxiang
 * @date 2022/4/26
 * @description
 */
public class AnimationFragment extends BaseFragment {

    private FragmentAnimationBinding mBinding;
    private ObjectAnimator mAnimator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentAnimationBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//            AssetFileDescriptor ad = getResources().getAssets().openFd("main_background.mp4");

//        playBackgroundVideo();
//        initAnimator();
        floatAnim(mBinding.btnOne,0);

        mBinding.btnOne.setOnClickListener(v -> {
            AnimationDrawable ad = (AnimationDrawable) mBinding.ivMatchstickMen.getDrawable();
            ad.start();
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initAnimator() {
        mAnimator = ObjectAnimator.ofFloat(mBinding.btnOne,"translationX", -6.0f,6.0f,-6.0f);
        mAnimator.setDuration(1500);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        mAnimator.setRepeatMode(ValueAnimator.RESTART);//
        mAnimator.start();

    }

    /**
     * 浮动效果
     * @param view 需要浮动的view
     * @param delay 延迟多少时间开始
     */
    private void floatAnim(View view,int delay){
        List<Animator> animators = new ArrayList<>();
        ObjectAnimator translationXAnim = ObjectAnimator.ofFloat(view, "translationX", -10.0f,10.0f,-10.0f);
        translationXAnim.setDuration(3000);
        translationXAnim.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        translationXAnim.setRepeatMode(ValueAnimator.REVERSE);//
        translationXAnim.start();
        animators.add(translationXAnim);
        ObjectAnimator translationYAnim = ObjectAnimator.ofFloat(view, "translationY", -5.0f,5.0f,-5.0f);
        translationYAnim.setDuration(2000);
        translationYAnim.setRepeatCount(ValueAnimator.INFINITE);
        translationYAnim.setRepeatMode(ValueAnimator.REVERSE);
        translationYAnim.start();
        animators.add(translationYAnim);

        AnimatorSet btnSexAnimatorSet = new AnimatorSet();
        btnSexAnimatorSet.playTogether(animators);
        btnSexAnimatorSet.setStartDelay(delay);
        btnSexAnimatorSet.start();
    }

    private void playBackgroundVideo() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/main_background.mp4";
        String url = "file:///android_asset/" + "main_background.mp4";
        Uri uri = Uri.parse(path);
        mBinding.video.setVideoURI(uri);
        mBinding.video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mBinding.video.start();
            }
        });
        mBinding.video.start();
    }
}
