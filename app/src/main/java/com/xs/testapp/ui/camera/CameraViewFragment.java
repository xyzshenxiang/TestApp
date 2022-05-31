package com.xs.testapp.ui.camera;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.FileCallback;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Mode;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;
import com.xs.testapp.databinding.FragmentCameraViewBinding;
import com.xs.testapp.ui.base.BaseFragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author xiang.shen
 * @create 2021/07/08
 * @Describe
 */
public class CameraViewFragment extends BaseFragment {

    private FragmentCameraViewBinding mBinding;

    private boolean mIsRecord = false;

    private CameraView mCameraView;

    private CameraListener mCameraListener = new CameraListener() {

        @Override
        public void onPictureTaken(@NonNull PictureResult result) {
            super.onPictureTaken(result);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssZ");
            String filename = "IMG_" + sdf.format(new Date()) + ".jpg";
            String path = getContext().getFilesDir().getAbsolutePath();
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File picture = new File(path + File.separator + filename);

            result.toFile(picture, new FileCallback() {
                @Override
                public void onFileReady(@Nullable File file) {

                }
            });
        }

        @Override
        public void onVideoTaken(@NonNull VideoResult result) {
            super.onVideoTaken(result);
        }

        @Override
        public void onCameraError(@NonNull CameraException exception) {
            super.onCameraError(exception);
            LogUtils.d("reason:" + exception.getReason(), exception.toString());
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentCameraViewBinding.inflate(inflater, container, false);
        LogUtils.d("onCreateView");
        mCameraView = mBinding.cameraView;
        mCameraView.addCameraListener(mCameraListener);

        mBinding.btnTake.setOnClickListener(view -> {

            if (mCameraView.getMode() != Mode.PICTURE) {
                mCameraView.setMode(Mode.PICTURE);
            }
            mCameraView.takePictureSnapshot();
        });

        mBinding.btnRecord.setOnClickListener(view -> {

            if (mIsRecord) {
                mIsRecord = false;
                mBinding.btnRecord.setText("start record");

                mCameraView.stopVideo();
            } else {
                mIsRecord = true;
                mBinding.btnRecord.setText("stop record");

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String filename = "VIDEO" + sdf.format(new Date()) + ".mp4";
                String path = getContext().getFilesDir().getAbsolutePath();
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File video = new File(path + File.separator + filename);

                if (mCameraView.getMode() != Mode.VIDEO) {
                    mCameraView.setMode(Mode.VIDEO);
                }

                mCameraView.takeVideoSnapshot(video);
            }
        });
        mCameraView.addFrameProcessor(new FrameProcessor() {
            @Override
            public void process(@NonNull Frame frame) {
                LogUtils.d("process");
            }
        });

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        mCameraView.open();
    }

    @Override
    public void onStop() {
        super.onStop();

        mCameraView.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCameraView.destroy();
    }
}