package com.xs.testapp.ui.camera;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shenxiang
 * @date 2022-04-14
 * <p>
 * 摄像头控制类
 */
public class Camera2Helper {

    private static class Holder {
        static Camera2Helper mInstance = new Camera2Helper();
    }

    public static Camera2Helper getInstance() {
        return Camera2Helper.Holder.mInstance;
    }

    public static final int NOTICE_CAMERA_OPEN = 0x01;

    private static final int STATE_PREVIEW = 0;//相机状态：显示相机预览。
    private static final int STATE_PICTURE_TAKEN = 1;//相机状态：拍照。
    private static final int STATE_VIDEO_RECORD = 2;//相机状态：录像。

    /**
     * camera管理，控制摄像头开启和关闭
     */
    private CameraManager mCameraManager;
    /**
     * 摄像头id
     */
    private String mCameraId;

    private CameraDevice mCamera;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private CameraCaptureSession mCameraCaptureSession;
    private List<Surface> mOutputs = new ArrayList<>();
    private CaptureRequest.Builder mRepeatingRequestBuilder;
    private ImageReader mImageReader;
    private MediaRecorder mMediaRecorder;

    private String mTakePicturePath;
    private String mRecordVideoPath;

    private Surface mPreviewSurface;
    /**
     * 当前相机状态
     */
    private int mState = STATE_PREVIEW;

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCamera = cameraDevice;

            try {
                createRepeatingRequestBuilder(CameraDevice.TEMPLATE_PREVIEW);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {

            if (null != mCamera) {
                mCamera.close();
                mCamera = null;
            }
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {

            ToastUtils.showShort("摄像头开启失败");
        }
    };

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        if (null == context) {
            return;
        }

        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    /**
     * 释放资源
     */
    public void release() {
        closeSession();
        closeCamera();
    }

    public void open() {
        openCamera();
    }

    public void close() {
        closeCamera();
    }

    /**
     * 打开摄像头
     */
    @SuppressLint("MissingPermission")
    private void openCamera() {

        stopBackgroundThread();
        startBackgroundThread();

        getCameraId();

        try {
            mCameraManager.openCamera(mCameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCamera = camera;
                    mBackgroundHandler.sendEmptyMessage(NOTICE_CAMERA_OPEN);
                    switch (mState) {
                        case STATE_PREVIEW: {
                            break;
                        }
                        case STATE_PICTURE_TAKEN: {
                            break;
                        }
                        case STATE_VIDEO_RECORD: {
                            break;
                        }
                    }
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {

                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {

                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭摄像头
     */
    private void closeCamera() {
        if (null != mCamera) {
            mCamera.close();
            mCamera = null;
        }
    }

    private void closeSession() {
        if (null != mCameraCaptureSession) {
            try {
                mCameraCaptureSession.stopRepeating();
                mCameraCaptureSession.abortCaptures();
                mCameraCaptureSession.close();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }
    }

    public void startPreview() {
        mState = STATE_PREVIEW;
        try {
            bindPreviewSession();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void takePicture() {

        mState = STATE_PICTURE_TAKEN;
        try {
            bindTakeSession();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void takeVideo() {

        mState = STATE_VIDEO_RECORD;
        try {
            bindRecordSession();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void stopVideo() {
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
    }

    private void bindPreviewSession() throws CameraAccessException {
        if (null == mCamera) {
            return;
        }

        if (null != mCameraCaptureSession) {
            mCameraCaptureSession.stopRepeating();
            mCameraCaptureSession.abortCaptures();
            mCameraCaptureSession.close();
        }

        mOutputs.clear();
        if (null != mPreviewSurface) {
            mOutputs.add(mPreviewSurface);
        }
        mCamera.createCaptureSession(mOutputs,
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        mCameraCaptureSession = cameraCaptureSession;
                        showPreview();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        ToastUtils.showShort("createCaptureSession:失败");
                    }
                }, mBackgroundHandler);

    }

    public void showPreview() {
        if (null == mCamera) {
            return;
        }
        try {
            CaptureRequest.Builder captureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            captureRequestBuilder.addTarget(mPreviewSurface);

            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            CaptureRequest previewRequest = captureRequestBuilder.build();
            mCameraCaptureSession.setRepeatingRequest(previewRequest, null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void bindTakeSession() throws CameraAccessException {
        if (null == mCamera) {
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
                File file = new File(mTakePicturePath);
                Image image = imageReader.acquireNextImage();
                mBackgroundHandler.post(new ImageSaveRunnable(image, file));
            }
        }, mBackgroundHandler);
        mOutputs.clear();
//        mOutputs.add(mPreviewSurface);
        mOutputs.add(mImageReader.getSurface());

        mCamera.createCaptureSession(mOutputs,
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        mCameraCaptureSession = cameraCaptureSession;

                        capturePicture();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                        ToastUtils.showShort("配置失败");
                    }
                }, mBackgroundHandler);

    }

    private void capturePicture() {
        if (null == mCamera) {
            return;
        }

        try {
            CaptureRequest.Builder captureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            captureRequestBuilder.addTarget(mImageReader.getSurface());

            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            //获取手机方向
//            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, 270);

            CaptureRequest captureRequest = captureRequestBuilder.build();
            mCameraCaptureSession.capture(captureRequest, null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void bindRecordSession() throws CameraAccessException {
        if (null == mCamera) {
            return;
        }

        if (null != mCameraCaptureSession) {
            mCameraCaptureSession.stopRepeating();
            mCameraCaptureSession.abortCaptures();
            mCameraCaptureSession.close();
        }

        initMediaRecorder();

        mOutputs.clear();

        if (null != mPreviewSurface) {
            mOutputs.add(mPreviewSurface);
        }

        //设置MediaRecorder的表面
        mOutputs.add(mMediaRecorder.getSurface());
        // 启动捕获会话
        // 一旦会话开始，我们就可以更新UI并开始录制
        mCamera.createCaptureSession(mOutputs,
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        mCameraCaptureSession = cameraCaptureSession;
                        captureVideo();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        ToastUtils.showShort("配置失败");
                    }
                }, mBackgroundHandler);
    }

    private void captureVideo() {
        if (null == mCamera) {
            return;
        }
        try {
            CaptureRequest.Builder captureBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            //为相机预览设置曲面
            captureBuilder.addTarget(mPreviewSurface);
            //设置MediaRecorder的表面
            captureBuilder.addTarget(mMediaRecorder.getSurface());

            mCameraCaptureSession.setRepeatingRequest(captureBuilder.build(), null, mBackgroundHandler);

            mMediaRecorder.start();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void initMediaRecorder() {
        File video = new File(mRecordVideoPath);

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

        mMediaRecorder.setOutputFile(video.getAbsolutePath());

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
        }
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

    /**
     * 启动子线程，摄像头相关的回调在子线程执行
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * 停止子线程
     */
    private void stopBackgroundThread() {
        if (null != mBackgroundThread) {
            mBackgroundThread.quitSafely();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }
    }

    private CaptureRequest.Builder createRepeatingRequestBuilder(int template)
            throws CameraAccessException {
        mRepeatingRequestBuilder = mCamera.createCaptureRequest(template);
        mRepeatingRequestBuilder.setTag(template);
        return mRepeatingRequestBuilder;
    }

    private void addRepeatingRequestBuilderSurfaces(@NonNull Surface... extraSurfaces) {
        for (Surface extraSurface : extraSurfaces) {
            if (extraSurface == null) {
                throw new IllegalArgumentException("Should not add a null surface.");
            }
            mRepeatingRequestBuilder.addTarget(extraSurface);
        }
    }

    public void setTakePicturePath(String mTakePicturePath) {
        this.mTakePicturePath = mTakePicturePath;
    }

    public void setRecordVideoPath(String mRecordVideoPath) {
        this.mRecordVideoPath = mRecordVideoPath;
    }

    public void setPreviewSurface(Surface mPreviewSurface) {
        this.mPreviewSurface = mPreviewSurface;
    }

    public void addSurface(@NonNull Surface surface) {
        mOutputs.add(surface);
    }

    public void clearSurface() {
        mOutputs.clear();
    }

    static class BackgroundHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

        }
    }

    static class ImageSaveRunnable implements Runnable {
        private final Image mImage;

        private final File mFile;

        public ImageSaveRunnable(Image mImage, File mFile) {
            this.mImage = mImage;
            this.mFile = mFile;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mFile);
                fos.write(bytes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != fos) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
}
