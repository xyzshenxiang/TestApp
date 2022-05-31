package com.xs.testapp.ui.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xs.testapp.databinding.FragmentCamera2SimpleBinding;
import com.xs.testapp.ui.base.BaseFragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author xiang.shen
 * @create 2021/07/08
 * @Describe
 */
public class Camera2SimpleFragment extends BaseFragment {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private FragmentCamera2SimpleBinding mBinding;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    private ImageReader mImageReader;

    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private String mCameraId;
    private Handler childHandler;
    private CameraCaptureSession mCameraCaptureSession;
    private MediaRecorder mMediaRecorder;
    private Surface mSurface;

    private boolean mIsRecord = false;

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {

            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {

            ToastUtils.showShort("摄像头开启失败");
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentCamera2SimpleBinding.inflate(inflater, container, false);
        LogUtils.d("onCreateView");
        mSurfaceView = mBinding.surfaceView;

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                initCamera2();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
            }
        });

        mBinding.btnOpen.setOnClickListener(v -> {

            try {
                initPreviewSession();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        });
        mBinding.btnTake.setOnClickListener(view -> {

            try {
                initTakeSession();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        });
        mBinding.btnRecord.setOnClickListener(view -> {

            if (mIsRecord) {
                mIsRecord = false;
                mBinding.btnRecord.setText("start record");
                stopRecord();
            } else {
                mIsRecord = true;
                mBinding.btnRecord.setText("stop record");
                startRecord();

            }
        });
        return mBinding.getRoot();
    }

    private void initCamera2() {
        mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);

        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper());
        getCameraId();

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        try {
            mCameraManager.openCamera(mCameraId, stateCallback, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void releaseCamera2() {

    }

    /**
     * 获取摄像头id
     */
    private void getCameraId() {
        try {
            for (String cmid : mCameraManager.getCameraIdList()) {

                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cmid);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (null != facing && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    continue;
                }
                mCameraId = cmid;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void initPreviewSession() throws CameraAccessException {
        if (null == mCameraDevice) {
            return;
        }

        if (null != mCameraCaptureSession) {
            mCameraCaptureSession.close();
        }

        mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface()),
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        if (null == mCameraDevice) {
                            return;
                        }
                        mCameraCaptureSession = cameraCaptureSession;

                        showPreview();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                        ToastUtils.showShort("配置失败");
                    }
                }, childHandler);

    }

    private void showPreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(mSurfaceHolder.getSurface());

            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            CaptureRequest previewRequest = previewRequestBuilder.build();
            mCameraCaptureSession.setRepeatingRequest(previewRequest, null, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void initTakeSession() throws CameraAccessException {
        if (null == mCameraDevice) {
            return;
        }

        if (null != mCameraCaptureSession) {
            mCameraCaptureSession.stopRepeating();
            mCameraCaptureSession.abortCaptures();
            mCameraCaptureSession.close();
        }
        mImageReader = ImageReader.newInstance(720, 1280, ImageFormat.JPEG, 3);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader imageReader) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String filename = "IMG_" + sdf.format(new Date()) + ".jpg";
                String path = getContext().getFilesDir().getAbsolutePath();
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File picture = new File(path + File.separator + filename);

                Image image = imageReader.acquireNextImage();
                childHandler.post(new ImageSaveRunnable(image, picture));

                //显示图片
//                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                byte[] bytes = new byte[buffer.remaining()];
//                buffer.get(bytes);
//                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                if (null != bitmap) {
//                    mBinding.ivImage.setImageBitmap(bitmap);
//                }
            }
        }, childHandler);


        mCameraDevice.createCaptureSession(Arrays.asList(mImageReader.getSurface()),
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        if (null == mCameraDevice) {
                            return;
                        }
                        mCameraCaptureSession = cameraCaptureSession;

                        takePicture();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                        ToastUtils.showShort("配置失败");
                    }
                }, childHandler);

    }

    private void takePicture() {
        if (null == mCameraDevice) {
            return;
        }

        try {

            CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            captureRequestBuilder.addTarget(mImageReader.getSurface());

            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            //获取手机方向
//            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, 270);

            CaptureRequest captureRequest = captureRequestBuilder.build();
            mCameraCaptureSession.capture(captureRequest, null, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void initRecordSession() throws CameraAccessException {
        if (null == mCameraDevice) {
            return;
        }

        if (null != mCameraCaptureSession) {
            mCameraCaptureSession.stopRepeating();
            mCameraCaptureSession.abortCaptures();
            mCameraCaptureSession.close();
        }

        initMediaRecorder();

        List<Surface> surfaces = new ArrayList<>();

        //为相机预览设置曲面
        surfaces.add(mSurfaceHolder.getSurface());

        //设置MediaRecorder的表面
        Surface recorderSurface = mMediaRecorder.getSurface();
        surfaces.add(recorderSurface);
        // 启动捕获会话
        // 一旦会话开始，我们就可以更新UI并开始录制
        mCameraDevice.createCaptureSession(surfaces,
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        mCameraCaptureSession = cameraCaptureSession;
                        try {
                            initRecord();
                            //开启录像
                            mMediaRecorder.start();
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        ToastUtils.showShort("配置失败");
                    }
                }, childHandler);
    }

    private void initRecord() throws CameraAccessException {
        CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        //为相机预览设置曲面
        captureBuilder.addTarget(mSurfaceHolder.getSurface());
        //设置MediaRecorder的表面
        captureBuilder.addTarget(mMediaRecorder.getSurface());

        mCameraCaptureSession.setRepeatingRequest(captureBuilder.build(), null, childHandler);
    }

    private void initMediaRecorder() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String filename = "VIDEO_" + sdf.format(new Date()) + ".mp4";
        String path = getContext().getFilesDir().getAbsolutePath();
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File video = new File(path + File.separator + filename);
//        mMediaRecorder.setCamera(mcamera);
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

        mMediaRecorder.setOutputFile(video.getAbsolutePath());

//        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startRecord() {
        try {
            initRecordSession();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void stopRecord() {
        if (null != mCameraCaptureSession) {
            try {
                mCameraCaptureSession.stopRepeating();
                mCameraCaptureSession.abortCaptures();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder.stop();
        releaseMediaRecorder();

        if (null != mCameraCaptureSession) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }

    }

    private void releaseMediaRecorder() {
        if (null != mMediaRecorder) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
}