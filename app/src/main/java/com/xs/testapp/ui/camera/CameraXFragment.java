package com.xs.testapp.ui.camera;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.LogUtils;
import com.google.common.util.concurrent.ListenableFuture;
import com.xs.testapp.databinding.FragmentCameraxBinding;
import com.xs.testapp.ui.base.BaseFragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * @author xiang.shen
 * @create 2021/07/08
 * @Describe
 */
public class CameraXFragment extends BaseFragment {


    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private FragmentCameraxBinding mBinding;

    private MediaRecorder mMediaRecorder;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture mImageCapture;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentCameraxBinding.inflate(inflater, container, false);
        LogUtils.d("onCreateView");

        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        cameraProviderFuture.addListener(() -> {
            ProcessCameraProvider cameraProvider = null;
            try {
                cameraProvider = cameraProviderFuture.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bindPreview(cameraProvider);
        },ContextCompat.getMainExecutor(getContext()));
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMediaRecorder = new MediaRecorder();

        mBinding.btnTake.setOnClickListener(v -> {
            takePicture();
        });

        mBinding.btnRecoder.setOnClickListener(v -> {
            initMediaRecorder();
            mMediaRecorder.start();

        });
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(mBinding.preview.getSurfaceProvider());

//        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview);

        mImageCapture = new ImageCapture.Builder()
                .setTargetRotation(getView().getDisplay().getRotation())
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, mImageCapture, preview);
    }

    private void takePicture() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssZ");
        String filename = "IMG"+sdf.format(new Date())+".jpg";
        String path = getContext().getFilesDir().getAbsolutePath()+"/takePicture";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File picture = new File(path+File.separator+filename);

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(picture).build();

        mImageCapture.takePicture(outputFileOptions, Executors.newSingleThreadExecutor(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                LogUtils.d(outputFileResults);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {

                LogUtils.e(exception);
            }
        });

//        cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, imageAnalysis, preview);
    }

    private void recorderVideo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssZ");
        String filename = "VIDEO"+sdf.format(new Date())+".mp4";
        String path = getContext().getFilesDir().getAbsolutePath();
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File video = new File(path+File.separator+filename);
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Video.Media.DISPLAY_NAME, filename);
    }

    private void initMediaRecorder() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssZ");
        String filename = "VIDEO"+sdf.format(new Date())+".mp4";
        String path = getContext().getFilesDir().getAbsolutePath()+"/video";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File video = new File(path+File.separator+filename);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));

        mMediaRecorder.setOutputFile(video.getAbsolutePath());

//        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}