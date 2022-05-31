package com.xs.testapp.ui.camera;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.xs.testapp.databinding.FragmentCameraBinding;
import com.xs.testapp.ui.base.BaseFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author xiang.shen
 * @create 2021/07/08
 * @Describe
 */
public class CameraFragment extends BaseFragment {

    private FragmentCameraBinding mBinding;
    private Camera mcamera;
    private MediaRecorder mMediaRecorder;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentCameraBinding.inflate(inflater, container, false);
        LogUtils.d("onCreateView");
        mSurfaceView = mBinding.surfaceView;
        mSurfaceHolder = mSurfaceView.getHolder();
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mcamera = Camera.open();
        mMediaRecorder = new MediaRecorder();
        mcamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {

            }
        });
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LogUtils.d("surfaceCreated");

                startPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                LogUtils.d("surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                LogUtils.d("surfaceDestroyed");
                releaseCamera();
            }
        });

        mBinding.btnOpen.setOnClickListener(v -> {

            takePicture();

        });

        mBinding.btnStart.setOnClickListener(v -> {
            initMediaRecorder();
            mMediaRecorder.start();

        });
        mBinding.btnStop.setOnClickListener(v -> {

            mMediaRecorder.stop();
            releaseMediaRecorder();
        });
    }

    private void takePicture() {
        if (null != mcamera) {
            mcamera.takePicture(new Camera.ShutterCallback() {
                @Override
                public void onShutter() {

                }
            }, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {

                }
            }, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    //调用takePickture后预览会停止，想要继续预览需要调用startPreview()函数
//                    mcamera.startPreview();

                    LogUtils.d("onPictureTaken");

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssZ");
                    String filename = "IMG"+sdf.format(new Date())+".jpg";
                    String path = getContext().getFilesDir().getAbsolutePath()+"/takePicture";
                    File dir = new File(path);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File picture = new File(path+File.separator+filename);

                    try {
                        FileOutputStream fos = new FileOutputStream(picture);
                        fos.write(data);
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
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

        mcamera.unlock();
        mMediaRecorder.setCamera(mcamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));

        mMediaRecorder.setOutputFile(video.getAbsolutePath());

        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseMediaRecorder() {
        if (null != mMediaRecorder) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mcamera.lock();
        }
    }

    private void startPreview() {
        try {
            mcamera.setPreviewDisplay(mSurfaceHolder);
            mcamera.setDisplayOrientation(90);
            mcamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        if (null != mcamera) {
            mcamera.stopPreview();
            mcamera.setPreviewCallback(null);
            mcamera.release();;
            mcamera = null;
        }
    }
}