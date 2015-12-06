package com.fan.fangallery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.lang.reflect.Field;

/**
 * time: 2015/9/30
 * description:
 *
 * @author fandong
 */
public class ScrollRecyclerView extends RecyclerView {
    private int mScrollDx;
    private int mScrollDy;
    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            mScrollDx += dx;
            mScrollDy += dy;
        }
    };

    public ScrollRecyclerView(Context context) {
        super(context);
        initView();
    }

    public ScrollRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ScrollRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        addOnScrollListener(mOnScrollListener);
    }

    public int getScrollDx() {
        return mScrollDx;
    }

    public int getScrollDy() {
        return mScrollDy;
    }

    public int getLastTouchY() {
        try {
            Field field = this.getClass().getSuperclass().getDeclaredField("mLastTouchY");
            field.setAccessible(true);
            return field.getInt(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
