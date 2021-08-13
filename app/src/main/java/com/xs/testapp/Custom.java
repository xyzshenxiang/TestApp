package com.xs.testapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import androidx.annotation.Nullable;

/**
 * @author xiang.shen
 * @create 2021/07/14
 * @Describe
 */
public class Custom extends View {


    public Custom(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
    }

    public Custom(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    Scroller mScroller;

    private int lastx = 0;
    private int lasty = 0;
    int lastmx,lastmy;
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:{
                lastx = x;
                lasty = y;
                lastmx = x;
                lastmy = y;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int offsetx = x - lastx;
                int offsety = y - lasty;
                if (x-lastmx>50||y-lastmy>50) {
                    lastmx = x;
                    lastmy = y;
                }
//                ((View)getParent()).scrollBy(-offsetx,-offsety);
//                layout(getLeft() + offsetx, getTop() + offsety, getRight() + offsetx, getBottom() + offsety);
                break;
            }
        }
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {

            ((View)getParent()).scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            invalidate();
        }
    }

    public void smoothScrollTo(int destX, int destY) {

        int scrollX = getScrollX();
        int delta = destX-scrollX;
        mScroller.startScroll(scrollX,0,delta,0,2000);
        invalidate();
    }
}