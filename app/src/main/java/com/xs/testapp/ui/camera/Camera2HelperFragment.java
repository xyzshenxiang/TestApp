package com.xs.testapp.ui.camera;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.xs.testapp.databinding.FragmentCamera2HelperBinding;
import com.xs.testapp.databinding.FragmentCamera2SimpleBinding;
import com.xs.testapp.ui.base.BaseFragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author xiang.shen
 * @create 2021/07/08
 * @Describe
 */
public class Camera2HelperFragment extends BaseFragment {

    private FragmentCamera2HelperBinding mBinding;

    private boolean mIsRecord = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentCamera2HelperBinding.inflate(inflater, container, false);
        LogUtils.d("onCreateView");

        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        Camera2Helper.getInstance().setTextureView(mBinding.tvView);
        mBinding.tvView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                Camera2Helper.getInstance().openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                Camera2Helper.getInstance().closeCamera();
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });

        Camera2Helper.getInstance().setTakePictureListener(new Camera2Helper.TakePictureListener() {
            @Override
            public void callbackPicture(String image) {
                mBinding.ivImage.post(new Runnable() {
                    @Override
                    public void run() {

                        Glide.with(getActivity()).load(image).into(mBinding.ivImage);
                    }
                });
            }
        });

        mBinding.btnOpen.setOnClickListener(v -> {


        });
        mBinding.btnTake.setOnClickListener(view -> {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String filename = "IMG_" + sdf.format(new Date()) + ".jpg";

            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File picture = new File(dir, filename);

            Camera2Helper.getInstance().setTakePicturePath(picture.getAbsolutePath());
            Camera2Helper.getInstance().takePicture();
        });
        mBinding.btnRecord.setOnClickListener(view -> {

            if (mIsRecord) {
                mIsRecord = false;
                mBinding.btnRecord.setText("start record");
                Camera2Helper.getInstance().stopVideo();
            } else {
                mIsRecord = true;
                mBinding.btnRecord.setText("stop record");

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String filename = "VIDEO_" + sdf.format(new Date()) + ".mp4";
                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File video = new File(path + File.separator + filename);

                Camera2Helper.getInstance().setRecordVideoPath(video.getAbsolutePath());
                Camera2Helper.getInstance().takeVideo();
            }
        });
        return mBinding.getRoot();
    }

}