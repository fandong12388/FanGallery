package com.fan.fangallery;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;


/**
 * time: 2015/9/30
 * description:
 *
 * @author fandong
 */
public class ScrollLinearLayout extends LinearLayout {
    private static final int MAX_SCROLL_DURATION = 2000;
    /*向上滑动的总体距离*/
    private int mTotalScrollY;
    /*向上滑动的限制距离*/
    private float mScrollLimit;
    /*顶部保留的高度*/
    private float mTopLimit;
    /*ScrollLinearLayout的整体高度*/
    private float mViewHeight;
    /*阴影的高度*/
    private float mShadowHeight;


    private static final Interpolator sQuinticInterpolator = t -> {
        t -= 1.0f;
        return t * t * t * t * t + 1.0f;
    };

    public ScrollLinearLayout(Context context) {
        super(context);
        mTopLimit = ResHelper.getDimen(R.dimen.crop_image_operation_height);
        mViewHeight = ResHelper.getDimen(R.dimen.crop_image_header_height);
        mShadowHeight = ResHelper.getDimen(R.dimen.crop_image_shadow_height);
        mScrollLimit = mViewHeight - mTopLimit - mShadowHeight;
    }

    public ScrollLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTopLimit = ResHelper.getDimen(R.dimen.crop_image_operation_height);
        mViewHeight = ResHelper.getDimen(R.dimen.crop_image_header_height);
        mShadowHeight = ResHelper.getDimen(R.dimen.crop_image_shadow_height);
        mScrollLimit = mViewHeight - mTopLimit - mShadowHeight;
    }

    public ScrollLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTopLimit = ResHelper.getDimen(R.dimen.crop_image_operation_height);
        mViewHeight = ResHelper.getDimen(R.dimen.crop_image_header_height);
        mShadowHeight = ResHelper.getDimen(R.dimen.crop_image_shadow_height);
        mScrollLimit = mViewHeight - mTopLimit - mShadowHeight;
    }

    /**
     * 根据点击点的y坐标确定是否跟随RecyclerView一起滑动
     *
     * @param touchY 点击的Y坐标
     * @param dx     x轴滑动距离
     * @param dy     y轴滑动距离
     */
    public void scrollBy(float touchY, int dx, int dy) {
        //1.上滑的时候需要确定热点区域，另外还有上滑的时候，必须不能超过限制
        if (touchY <= mViewHeight && touchY >= mTopLimit) {
            if (dy + mTotalScrollY > mScrollLimit) {
                dy = (int) (mScrollLimit - mTotalScrollY + 0.5f);
            }
            super.scrollBy(dx, dy);
            mTotalScrollY += dy;
        }
        //2.下滑的时候，只有当recycler第一个视图的下滑抵拢到固定的item的时候同时下滑
        else {
            if (dy < 0) {
                if (dy + mTotalScrollY < 0) {
                    dy = (int) (-mTotalScrollY - 0.5f);
                }
                super.scrollBy(dx, dy);
                mTotalScrollY += dy;
            }
        }
    }

    /**
     * 当手指从屏幕离开，RecyclerView的状态是Idle的时候的回调
     *
     * @param listener 监听
     */
    public void clipToBound(OnScrollListener listener, boolean scrollToBottom) {
        if (scrollToBottom && mTotalScrollY > 0) {
            smoothScrollBy(0, -mTotalScrollY);
            mTotalScrollY = 0;
            return;
        }
        if (mTotalScrollY > 0) {
            int dy;
            if (mTotalScrollY >= mScrollLimit / 2.f) {
                //超过一半，就滑到顶部去
                dy = (int) (mScrollLimit - mTotalScrollY + 0.5f);
            } else {
                //没有超过一半，就滑到底部
                dy = (int) (-mTotalScrollY - 0.5f);
            }
            listener.onScrolled(0, dy);
            mTotalScrollY += dy;
            smoothScrollBy(0, dy);
        }
    }

    /**
     * 判断是否是顶部挂起状态
     *
     * @return
     */
    public boolean isTopState() {
        return mTotalScrollY >= mScrollLimit - 1;
    }

    /**
     * 返回整个视图的高度
     *
     * @return
     */
    public float getViewHeight() {
        return mViewHeight;
    }

    public float getFullViewHeight() {
        return mViewHeight - mShadowHeight + PixelUtil.dp2px(2.f);
    }

    /**
     * 滑动到底部
     */
    public void scrollToBottom() {
        if (isTopState()) {
            smoothScrollBy(0, -mTotalScrollY);
            mTotalScrollY = 0;
        }
    }

    /**
     * 带动画地滑到目的距离
     *
     * @param dx x轴滑动距离
     * @param dy y轴滑动距离
     */
    public void smoothScrollBy(int dx, int dy) {
        int duration = computeScrollDuration(dx, dy, 0, 0);
        ValueAnimator animator = ValueAnimator.ofInt(0, dy);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private int lastValue;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                scrollBy(0, value - lastValue);
                lastValue = value;
            }
        });
        animator.setDuration(duration);
        animator.setInterpolator(sQuinticInterpolator);
        animator.start();
    }


    private int computeScrollDuration(int dx, int dy, int vx, int vy) {
        final int absDx = Math.abs(dx);
        final int absDy = Math.abs(dy);
        final boolean horizontal = absDx > absDy;
        final int velocity = (int) Math.sqrt(vx * vx + vy * vy);
        final int delta = (int) Math.sqrt(dx * dx + dy * dy);
        final int containerSize = horizontal ? getWidth() : getHeight();
        final int halfContainerSize = containerSize / 2;
        final float distanceRatio = Math.min(1.f, 1.f * delta / containerSize);
        final float distance = halfContainerSize + halfContainerSize *
                distanceInfluenceForSnapDuration(distanceRatio);

        final int duration;
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            float absDelta = (float) (horizontal ? absDx : absDy);
            duration = (int) (((absDelta / containerSize) + 1) * 300);
        }
        return Math.min(duration, MAX_SCROLL_DURATION);
    }


    private float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f;
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    public interface OnScrollListener {
        void onScrolled(int dx, int dy);
    }

}
