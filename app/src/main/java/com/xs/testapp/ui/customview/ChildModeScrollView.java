package com.xs.testapp.ui.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;

import androidx.annotation.Nullable;

/**
 * @author xiang.shen
 * @create 2021/07/14
 * @Describe
 */
public class ChildModeScrollView extends View {

    /**
     * 解锁监听事件
     */
    public interface OnUnlockListener{
        /**
         * 解锁
         */
        void onUnlock();
    }

    private OnUnlockListener mOnUnlockListener;


    public ChildModeScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChildModeScrollView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnUnlockListener(OnUnlockListener listener) {
        this.mOnUnlockListener = listener;
    }

    private int lastY = 0;

    private boolean mUnlock = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int curY = (int) event.getY();
        Log.d("",String.format("event X:%f,Y:%f",event.getX(),event.getY()));
        Log.d("",String.format("event RawX:%f,RawY:%f",event.getRawX(),event.getRawY()));
        Log.d("",String.format("view left:%d,top:%d,right:%d,bottom:%d",getLeft(),getTop(),getRight(),getBottom()));
        Log.d("",String.format("view X:%f,Y:%f",getX(),getY()));
        Log.d("",String.format("view ScrollX:%d,getScrollY:%d",getScrollX(),getScrollY()));
        Log.d("",String.format("view TranslationX:%f,TranslationY:%f",getTranslationX(),getTranslationY()));
        View pare = (View) getParent();
        Log.d("",String.format("pareX:%f,pareY:%f",pare.getX(),pare.getY()));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                lastY = curY;
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mUnlock) {
                    setVisibility(INVISIBLE);
                    if (null != mOnUnlockListener) {
                        mOnUnlockListener.onUnlock();
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int offsetY = curY - lastY;
                int top = getTop() + offsetY;
                if (top < 0) {
                    top = 0;
                }
                int bottom = top + getHeight();

                View parent = (View) getParent();
                if (bottom > parent.getHeight()) {
                    bottom = parent.getHeight();
                    top = bottom - getHeight();
                }

                int maxBottom = parent.getHeight() - getHeight() + 20;
                if (bottom >= maxBottom) {
                    mUnlock = true;
                    setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                } else {
                    mUnlock = false;
                    setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                }


                Log.d("",String.format("offsetY:%d,curY:%d,lastY:%d",offsetY,curY,lastY));
                Log.d("",String.format("layout Top:%d,bottom:%d",top,bottom));
                layout(getLeft(), top, getRight(), bottom);
                break;
            }
            default:
                break;

        }
        return true;
    }
}