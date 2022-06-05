package com.xs.testapp.ui.camera;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
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

    private FragmentCamera2SimpleBinding mBinding;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    private boolean mIsRecord = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentCamera2SimpleBinding.inflate(inflater, container, false);
        LogUtils.d("onCreateView");
//        mSurfaceView = mBinding.surfaceView;
//        mSurfaceHolder = mSurfaceView.getHolder();

        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
//        Camera2Helper.getInstance().setPreviewSurface(mSurfaceHolder.getSurface());

//        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceCreated(SurfaceHolder surfaceHolder) {
//
//                Camera2Helper.getInstance().open();
//            }
//
//            @Override
//            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
//                Camera2Helper.getInstance().close();
//            }
//        });
        Camera2Helper.getInstance().setTextureView(mBinding.tvView);
        mBinding.tvView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
//                configureTextureViewTransform(mBinding.tvView.getWidth(),mBinding.tvView.getHeight());
//                Camera2Helper.getInstance().setPreviewSurface(new Surface(mBinding.tvView.getSurfaceTexture()));
                Camera2Helper.getInstance().open(width,height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });

        mBinding.btnOpen.setOnClickListener(v -> {

//            Camera2Helper.getInstance().startPreview();
        });
        mBinding.btnTake.setOnClickListener(view -> {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String filename = "IMG_" + sdf.format(new Date()) + ".jpg";
            String path = getContext().getFilesDir().getAbsolutePath();
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
                String path = getContext().getFilesDir().getAbsolutePath();
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

    private void configureTextureViewTransform(int viewWidth, int viewHeight) {
        if (null == mBinding.tvView) {
            return;
        }
        int rotation = 0 ;/*activity.getWindowManager().getDefaultDisplay().getRotation();*/
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
//        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
//        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
//            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
//            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
//            float scale = Math.max(
//                    (float) viewHeight / mPreviewSize.getHeight(),
//                    (float) viewWidth / mPreviewSize.getWidth());
//            matrix.postScale(scale, scale, centerX, centerY);
//            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
//        }else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
//        }
        mBinding.tvView.setTransform(matrix);
    }

}