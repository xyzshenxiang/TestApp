package com.xs.testapp.ui.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author shenxiang
 * @date 2022-04-14
 * <p>
 * 摄像头控制类
 */
public class Camera2Helper {

    public interface TakePictureListener {
        /**
         * 拍照回调接口
         */
        void callbackPicture(String image);
    }

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
    private String mCameraId = null;

    private CameraDevice mCamera;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private CameraCaptureSession mCameraCaptureSession;
    private List<Surface> mOutputs = new ArrayList<>();
    private ImageReader mImageReader;
    private MediaRecorder mMediaRecorder;

    private String mTakePicturePath;
    private String mRecordVideoPath;

    private Surface mPreviewSurface;

    private TextureView mTextureView;

    private Size mPreviewSize;
    /**
     * 当前相机状态
     */
    private int mState = STATE_PREVIEW;

    private TakePictureListener mTakePictureListener = null;

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCamera = cameraDevice;
            mBackgroundHandler.sendEmptyMessage(NOTICE_CAMERA_OPEN);
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

    /**
     * 打开摄像头
     */
    @SuppressLint("MissingPermission")
    public void openCamera() {

        stopBackgroundThread();
        startBackgroundThread();

        getCameraId();

        try {
            mCameraManager.openCamera(mCameraId, stateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();

        }
    }

    /**
     * 关闭摄像头
     */
    public void closeCamera() {
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
                mCameraCaptureSession = null;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }
    }

    public void takePicture() {
        mState = STATE_PICTURE_TAKEN;
        capturePicture();
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
                mCameraCaptureSession = null;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder.stop();
        releaseMediaRecorder();
    }
    /**
     * 启动预览
     */
    private void startPreview() {
        mState = STATE_PREVIEW;
        try {
            bindPreviewSession();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 绑定预览和拍照的Session
     * @throws CameraAccessException
     */
    private void bindPreviewSession() throws CameraAccessException {
        if (null == mCamera) {
            return;
        }

        setupPreviewSize(mTextureView.getWidth(), mTextureView.getHeight());

        mOutputs.clear();

        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        mPreviewSurface = new Surface(surfaceTexture);
        //加入预览的surface
        mOutputs.add(mPreviewSurface);

        setupImageReader();
        //加入拍照的surface
        mOutputs.add(mImageReader.getSurface());

        mCamera.createCaptureSession(mOutputs,
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        mCameraCaptureSession = cameraCaptureSession;
                        repeatPreview();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        ToastUtils.showShort("createCaptureSession:失败");
                    }
                }, mBackgroundHandler);

    }

    /**
     * 绑定录像的session
     * @throws CameraAccessException
     */
    private void bindRecordSession() throws CameraAccessException {
        if (null == mCamera) {
            return;
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

    /**
     * 设置照片保存
     */
    private void setupImageReader() {
        try {
            Size captureSize = null;
            if (null != mCameraId) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);

                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                //照片大小
                captureSize = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new Comparator<Size>() {
                    @Override
                    public int compare(Size lhs, Size rhs) {
                        return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getWidth());
                    }
                });
            } else {
                captureSize = new Size(1280,720);
            }

            mImageReader = ImageReader.newInstance(captureSize.getWidth(), captureSize.getHeight(), ImageFormat.JPEG, 3);
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    File file = new File(mTakePicturePath);
                    Image image = imageReader.acquireNextImage();
                    mBackgroundHandler.post(new ImageSaveRunnable(image, file,mTakePictureListener));
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }
    /**
     * 发送连续捕获预览
     */
    private void repeatPreview() {
        if (null == mCamera) {
            return;
        }

        try {
            CaptureRequest.Builder captureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            captureRequestBuilder.addTarget(mPreviewSurface);

            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
           //停止之前的捕获
            mCameraCaptureSession.abortCaptures();

            CaptureRequest previewRequest = captureRequestBuilder.build();
            mCameraCaptureSession.setRepeatingRequest(previewRequest, null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    /**
     * 设置单次捕获请求
     */
    private void capturePicture() {
        if (null == mCamera) {
            return;
        }

        try {
            //首先我们创建请求拍照的CaptureRequest
            CaptureRequest.Builder captureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            captureRequestBuilder.addTarget(mPreviewSurface);
            captureRequestBuilder.addTarget(mImageReader.getSurface());
            // 自动对焦
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 自动曝光
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            //获取手机方向

            //设置拍照方向
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, 270);

            //停止预览
//            mCameraCaptureSession.stopRepeating();

            //开始拍照，然后回调上面的接口重启预览，因为mCaptureBuilder设置ImageReader作为target，所以会自动回调ImageReader的onImageAvailable()方法保存图片
            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    repeatPreview();
                }
            };
            mCameraCaptureSession.capture(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * 设置连续捕获图像请求
     */
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

            mCameraCaptureSession.abortCaptures();

            mCameraCaptureSession.setRepeatingRequest(captureBuilder.build(), null, mBackgroundHandler);

            mMediaRecorder.start();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * 初始化视频录制
     */
    private void initMediaRecorder() {
        File video = new File(mRecordVideoPath);

        mMediaRecorder = new MediaRecorder();
        //设置声音来源
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //使用camera2时，视频源必须设置为SURFACE
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        //设置当前相机能支持的最高配置
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        //旋转配置
        mMediaRecorder.setOrientationHint(90);
        mMediaRecorder.setOutputFile(video.getAbsolutePath());

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 释放视频录制
     */
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
                if (null != facing && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    mCameraId = cmid;
                    return;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setupPreviewSize(int width, int height) {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);

            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            //预览尺寸
            Size size = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
            mPreviewSize = new Size(size.getHeight(),size.getWidth());
            configureTransform(mTextureView, width,height);

        }  catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * 启动子线程，摄像头相关的回调在子线程执行
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case NOTICE_CAMERA_OPEN:{
                        switch (mState) {
                            case STATE_PREVIEW: {
                                startPreview();
                                break;
                            }
                            case STATE_PICTURE_TAKEN: {
                                break;
                            }
                            case STATE_VIDEO_RECORD: {
                                break;
                            }
                        }

                        break;
                    }
                }

            }
        };
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

    public void setTakePicturePath(String mTakePicturePath) {
        this.mTakePicturePath = mTakePicturePath;
    }

    public void setRecordVideoPath(String mRecordVideoPath) {
        this.mRecordVideoPath = mRecordVideoPath;
    }

    public void setPreviewSurface(Surface mPreviewSurface) {
        this.mPreviewSurface = mPreviewSurface;
    }

    public void setTextureView(TextureView mTextureView) {
        this.mTextureView = mTextureView;
    }

    public void setTakePictureListener(TakePictureListener listener) {
        mTakePictureListener = listener;
    }

    public void addSurface(@NonNull Surface surface) {
        mOutputs.add(surface);
    }

    public void clearSurface() {
        mOutputs.clear();
    }

    //选择sizeMap中大于并且最接近width和height的size
    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size option : sizeMap) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    sizeList.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 0) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return sizeMap[0];
    }


    /**
     * 将相机输出的预览尺寸和TextureView尺寸对应上，等比拉伸
     * @param textureView
     * @param viewWidth
     * @param viewHeight
     */
    private void configureTransform(TextureView textureView,int viewWidth, int viewHeight) {
        if (null == textureView || null == mPreviewSize) {
            return;
        }
        //屏幕方向
        //int rotation = windowManager.getDefaultDisplay().getRotation();
        final Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getWidth(), mPreviewSize.getHeight());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        //相机宽度小于界面高度时，使用矩阵缩放适配
            bufferRect.offset(centerX-bufferRect.centerX() ,  centerY-bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);

            float widthScale = (float) viewWidth / mPreviewSize.getWidth();
            float heightScale = (float) viewHeight / mPreviewSize.getHeight();
            //选择最小的居中显示
            float scale = Math.min(widthScale, heightScale);
            //设置缩放
            matrix.postScale(scale, scale, centerX, centerY);
            //设置旋转角度
//            matrix.postRotate(180, centerX, centerY);

        textureView.post(new Runnable() {
            @Override
            public void run() {
                textureView.setTransform(matrix);
            }
        });
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

        private TakePictureListener mTakePictureListener = null;

        public ImageSaveRunnable(Image mImage, File mFile,TakePictureListener listener) {
            this.mImage = mImage;
            this.mFile = mFile;
            mTakePictureListener = listener;
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

            if (null != mTakePictureListener) {
                mTakePictureListener.callbackPicture(mFile.getAbsolutePath());
            }

        }
    }
}
