package com.xs.testapp.ui.customview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xs.testapp.R;
import com.xs.testapp.databinding.ViewChildModeArrowBinding;

import java.lang.ref.WeakReference;

/**
 * @author xiang.shen
 * @create 2021/07/14
 * @Describe
 */
public class ChildModeArrowView extends LinearLayout {

    private ViewChildModeArrowBinding mBinding;
    private int mIndex = 0;

    private RefreshHandler mHandler = new RefreshHandler(this);

    public ChildModeArrowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ChildModeArrowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context ctx) {
        mBinding = ViewChildModeArrowBinding.inflate(LayoutInflater.from(ctx));
    }

    private void start() {
        mHandler.sendEmptyMessageDelayed(0, 500);
    }

    private void stop() {
        mHandler.removeMessages(0);
    }

    public void next() {

//        mBinding.ivArrow1.setImageResource(R.mipmap.ic_child_mode_arrow_1);
//        mBinding.ivArrow2.setImageResource(R.mipmap.ic_child_mode_arrow_1);
//        mBinding.ivArrow3.setImageResource(R.mipmap.ic_child_mode_arrow_1);
//        if (0 == mIndex) {
//            mBinding.ivArrow1.setImageResource(R.mipmap.ic_child_mode_arrow_2);
//        } else if (1 == mIndex) {
//            mBinding.ivArrow2.setImageResource(R.mipmap.ic_child_mode_arrow_2);
//        } else if (2 == mIndex) {
//            mBinding.ivArrow3.setImageResource(R.mipmap.ic_child_mode_arrow_2);
//        }

        mHandler.sendEmptyMessageDelayed(0, 500);
    }

    static class RefreshHandler extends Handler {

        private WeakReference<ChildModeArrowView> mWeakReference;
        public RefreshHandler(ChildModeArrowView view) {
            super();
            mWeakReference = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            ChildModeArrowView view = mWeakReference.get();
            if (null != view) {
                view.next();
            }
        }
    }
}