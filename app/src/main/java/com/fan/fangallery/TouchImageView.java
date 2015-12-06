package com.fan.fangallery;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

/**
 * time: 2015/9/28
 * description:
 *
 * @author fandong
 */
public class TouchImageView extends ImageView {
    private static final float[] MATRIX_ARRAY = {1.f, 0.f, 0.f, 0.f, 1.f, 0.f, 0.f, 0.f, 1.f};
    private Matrix mMatrix = new Matrix();
    private float[] mMatrixArray = new float[]{1.f, 0.f, 0.f, 0.f, 1.f, 0.f, 0.f, 0.f, 1.f};

    public static final int TOUCH_MODEL_SINGLE = 1;
    public static final int TOUCH_MODEL_MULTI = TOUCH_MODEL_SINGLE << 1;
    private int mTouchMode = TOUCH_MODEL_SINGLE;
    //点击开始的点
    private PointF mStartPoint = new PointF();
    //多点触控开始的距离
    private double mStartDistance;
    //图片的显示矩阵
    private RectF mRectF;
    //蒙层的剪切矩阵
    private RectF mCropRectF = new RectF();
    //bitmap的宽和高
    private int mBitmapHeight;
    private int mBitmapWidth;
    //是否是吸附状态，默认吸附
    private boolean mIsAttach = true;

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            // 表示用户开始触摸
            case MotionEvent.ACTION_DOWN:
                mTouchMode = TOUCH_MODEL_SINGLE;
                mStartPoint.set(event.getRawX(), event.getRawY());
                break;
            // 当屏幕上已经有触点，又有一个手指按下屏幕
            case MotionEvent.ACTION_POINTER_DOWN:
                mTouchMode = TOUCH_MODEL_MULTI;
                mStartDistance = getDistance(event);
                break;
            // 手指在屏幕上移动
            case MotionEvent.ACTION_MOVE:
                if (mTouchMode == TOUCH_MODEL_SINGLE) {
                    float currentX = event.getRawX();
                    float currentY = event.getRawY();
                    float x = currentX - mStartPoint.x;
                    float y = currentY - mStartPoint.y;
                    translate(x, y);
                    mStartPoint.set(currentX, currentY);
                } else if (mTouchMode == TOUCH_MODEL_MULTI) {
                    double endDistance = getDistance(event);
                    if (endDistance > 10f) {
                        double scale = endDistance / mStartDistance;
                        mStartDistance = endDistance;
                        scale((float) scale);
                    }
                }
                break;
            // 手指离开屏幕
            case MotionEvent.ACTION_UP:
                clipToBounds();
                mTouchMode = TOUCH_MODEL_SINGLE;
                break;
            // 当一个手指离开屏幕，但在屏幕上还有触点。
            case MotionEvent.ACTION_POINTER_UP:
                break;

            default:
                break;
        }
        return true;
    }

    /**
     * 当手指从屏幕上面离开，需要调用此方法，不让图片滑出视图区域
     *
     * @return
     */
    private boolean clipToBounds() {
        if (mRectF == null) return false;
        boolean isModified = false;
        float drawableWidth = mRectF.right - mRectF.left;
        float drawableHeight = mRectF.bottom - mRectF.top;
        float rectWidth = mCropRectF.right - mCropRectF.left;
        float rectHeight = mCropRectF.bottom - mCropRectF.top;
        float drawableRatio = drawableHeight / drawableWidth;
        float cropRatio = rectHeight / rectWidth;
        //1.如果图片在蒙层裁剪区域内就居中显示图片
        if (drawableHeight < rectHeight && drawableWidth < rectWidth) {
            float scale;
            float marginLeft;
            float marginTop;
            if (drawableRatio > cropRatio) {//高占满cop
                if (!mIsAttach) {
                    scale = rectHeight / drawableHeight;
                    marginTop = 0.f + mCropRectF.top;
                } else {
                    scale = rectWidth / drawableWidth;
                    marginTop = (rectHeight - drawableHeight * scale) / 2.f + mCropRectF.top;
                }
                marginLeft = (rectWidth - drawableWidth * scale) / 2.f + mCropRectF.left;
            } else {//宽占满crop
                if (!mIsAttach) {
                    scale = rectWidth / drawableWidth;
                    marginLeft = 0.f + mCropRectF.left;
                } else {
                    scale = rectHeight / drawableHeight;
                    marginLeft = (rectWidth - drawableWidth * scale) / 2.f + mCropRectF.left;
                }
                marginTop = (rectHeight - drawableHeight * scale) / 2.f + mCropRectF.top;
            }
            scale(scale);
            translate(marginLeft - mRectF.left, marginTop - mRectF.top);
            isModified = true;
        } else {
            float transX = 0.f, transY = 0.f;
            if (!mIsAttach) {
                //如果上下超过了一半，就回弹到一半的位置
                float cropHalfX = mCropRectF.left + rectWidth / 2.f;
                float cropHalfY = mCropRectF.top + rectHeight / 2.f;
                if (mRectF.right < cropHalfX) {
                    transX = cropHalfX - mRectF.right;
                } else if (mRectF.left > cropHalfX) {
                    transX = cropHalfX - mRectF.left;
                }
                if (mRectF.bottom < cropHalfY) {
                    transY = cropHalfY - mRectF.bottom;
                } else if (mRectF.top > cropHalfY) {
                    transY = cropHalfY - mRectF.top;
                }
                translate(transX, transY);
                isModified = true;
            } else {
                if (drawableHeight < rectHeight || drawableWidth < rectWidth) {
                    float scale;
                    float marginLeft;
                    float marginTop;
                    if (drawableRatio > cropRatio) {//高占满cop
                        scale = rectWidth / drawableWidth;
                        marginTop = (rectHeight - drawableHeight * scale) / 2.f + mCropRectF.top;
                        marginLeft = (rectWidth - drawableWidth * scale) / 2.f + mCropRectF.left;
                    } else {//宽占满crop
                        scale = rectHeight / drawableHeight;
                        marginLeft = (rectWidth - drawableWidth * scale) / 2.f + mCropRectF.left;
                        marginTop = (rectHeight - drawableHeight * scale) / 2.f + mCropRectF.top;
                    }
                    scale(scale);
                    translate(marginLeft - mRectF.left, marginTop - mRectF.top);
                    isModified = true;
                } else {
                    if (mRectF.left > mCropRectF.left) {
                        transX = mCropRectF.left - mRectF.left;
                    }
                    if (mRectF.right < mCropRectF.right) {
                        transX = mCropRectF.right - mRectF.right;
                    }
                    if (mRectF.top > mCropRectF.top) {
                        transY = mCropRectF.top - mRectF.top;
                    }

                    if (mRectF.bottom < mCropRectF.bottom) {
                        transY = mCropRectF.bottom - mRectF.bottom;
                    }
                    translate(transX, transY);
                    isModified = true;
                }
            }
        }
        return isModified;
    }


    @Override
    public void setImageBitmap(Bitmap bm) {
        //1.设置位图
        super.setImageBitmap(bm);
//        AlphaAnimation fadeImage = new AlphaAnimation(0, 1);
//        fadeImage.setDuration(800);
//        fadeImage.setInterpolator(new DecelerateInterpolator());
//        startAnimation(fadeImage);
        //2.设置位图的宽高
        mBitmapHeight = bm.getHeight();
        mBitmapWidth = bm.getWidth();
        //3.设置吸附模式
        setAttach(mCropRectF, mIsAttach);
    }



    /**
     * 得到两个手指之间的距离
     *
     * @param event 按下的动作
     * @return 距离
     */
    private double getDistance(MotionEvent event) {
        try {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            return Math.sqrt(dx * dx + dy * dy);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 设置吸附状态，需要重置位置
     *
     * @param cropRect
     * @param attach
     */
    public void setAttach(RectF cropRect, boolean attach) {
        //1.重置蒙层区域
        mCropRectF.set(cropRect);
        //2.设置吸附状态
        mIsAttach = attach;
        //3.重置一系列状态
        mMatrix.setValues(MATRIX_ARRAY);
        //高度是否填满整个视图
        boolean isHeightFull = resetPictureArea();
        if (mIsAttach) {
            float scale = 1.f;
            if (isHeightFull) {
                scale = (mRectF.bottom - mRectF.top) / mBitmapHeight;
            } else {
                scale = (mRectF.right - mRectF.left) / mBitmapWidth;
            }
            mMatrixArray[2] = mRectF.left;
            mMatrixArray[5] = mRectF.top;
            mMatrixArray[0] = mMatrixArray[4] = scale;
            //重置
            mMatrixArray[1]
                    = mMatrixArray[3]
                    = mMatrixArray[6]
                    = mMatrixArray[7]
                    = 0.f;
            mMatrixArray[8] = 1.f;
            mMatrix.setValues(mMatrixArray);
        } else {
            float targetWidth = mRectF.right - mRectF.left;
            float scale = targetWidth / mBitmapWidth;
            //缩放
            mMatrixArray[0] = mMatrixArray[4] = scale;
            //平移
            mMatrixArray[2] = mRectF.left;
            mMatrixArray[5] = mRectF.top;
            //重置
            mMatrixArray[1]
                    = mMatrixArray[3]
                    = mMatrixArray[6]
                    = mMatrixArray[7]
                    = 0.f;
            mMatrixArray[8] = 1.f;
            mMatrix.setValues(mMatrixArray);
        }
        setImageMatrix(mMatrix);
    }

    /**
     * 当第一次进入裁剪界面的时候，加载bitmap成功，会调用这个方法
     *
     * @param rectF 蒙层的rectF
     */
    public void setCropRectF(RectF rectF) {
        mCropRectF.set(rectF);
    }

    /**
     * 根据吸附状态、蒙层比例重置整个视图
     *
     * @return 高度是否填满视图
     */
    private boolean resetPictureArea() {
        boolean isHeightFull;
        //1.no attach模式下面,宽度满屏，高度上对齐
        float width;
        float height;
        if (mIsAttach) {
            float cropWidth = mCropRectF.right - mCropRectF.left;
            float cropHeight = mCropRectF.bottom - mCropRectF.top;
            float cropRatio = cropHeight / cropWidth;
            float bitmapRatio = mBitmapHeight / (float) mBitmapWidth;
            if (bitmapRatio > cropRatio) {
                //宽度满屏，高度显示不全
                float showWidth = cropWidth;
                float showHeight = showWidth * bitmapRatio;
                float py = (showHeight - cropHeight) / 2.f - mCropRectF.top;
                mRectF = new RectF(mCropRectF.left, -py, showWidth + mCropRectF.left, showHeight - py);
                isHeightFull = false;
            } else {
                //高度满屏，宽度显示不全
                float showHeight = cropHeight;
                float showWidth = showHeight / bitmapRatio;
                float px = (showWidth - cropWidth) / 2.f - mCropRectF.left;
                mRectF = new RectF(-px, mCropRectF.top, showWidth - px, showHeight + mCropRectF.top);
                isHeightFull = true;
            }
        } else {
            /**2.非attach模式下面
             *   2.1 图片宽/高 > crop宽/高  则宽全部显示
             *   2.2 图片宽/高 < crop宽/高  则高全部显示
             */
            float bitmapScale = mBitmapWidth / (float) mBitmapHeight;
            float cropScale = (mCropRectF.right - mCropRectF.left) / (mCropRectF.bottom - mCropRectF.top);
            if (bitmapScale > cropScale) {
                width = mCropRectF.right - mCropRectF.left;
                height = width * mBitmapHeight / (float) mBitmapWidth;
                float top = ((mCropRectF.bottom - mCropRectF.top) - height) / 2.f + mCropRectF.top;
                float left = mCropRectF.left;
                float right = left + width;
                float bottom = top + height;
                mRectF = new RectF(left, top, right, bottom);
                isHeightFull = false;
            } else {
                height = mCropRectF.bottom - mCropRectF.top;
                width = height * mBitmapWidth / (float) mBitmapHeight;
                float cropWidth = mCropRectF.right - mCropRectF.left;
                float left = (cropWidth - width) / 2.f + mCropRectF.left;
                float top = mCropRectF.top;
                mRectF = new RectF(left, top, width + left, height + top);
                isHeightFull = true;
            }
        }
        return isHeightFull;
    }


    /**
     * 得到当前图片的中心位置
     *
     * @return 封装了x, y坐标的点
     */
    private PointF getCenterPoint() {
        float x = mRectF.left + (mRectF.right - mRectF.left) / 2.f;
        float y = mRectF.top + (mRectF.bottom - mRectF.top) / 2.f;
        return new PointF(x, y);
    }

    /**
     * 得到旋转90度的matrix
     *
     * @return matrix
     */
    public void rotate() {
        PointF center = getCenterPoint();
        final float x = center.x;
        final float y = center.y;
        ValueAnimator animator = ValueAnimator.ofFloat(0.f, 90.f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private float lastValue = 0.f;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mMatrix.postRotate(value - lastValue, x, y);
                mMatrix.getValues(mMatrixArray);
                setImageMatrix(mMatrix);
                lastValue = value;
                if (lastValue >= 90.f) {
                    float height = mRectF.bottom - mRectF.top;
                    float width = mRectF.right - mRectF.left;
                    mRectF.left = mRectF.left + (width - height) / 2.f;
                    mRectF.right = mRectF.left + height;
                    mRectF.top = mRectF.top + (height - width) / 2.f;
                    mRectF.bottom = mRectF.top + width;
                }
            }
        });
        animator.setDuration(200);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }


    /**
     * 按中心点缩放
     *
     * @param scale
     */
    private void scale(float scale) {
        //1.执行缩放的计算
        mMatrix.setValues(mMatrixArray);
        PointF center = getCenterPoint();
        mMatrix.postScale(scale, scale, center.x, center.y);
        mMatrix.getValues(mMatrixArray);
        setImageMatrix(mMatrix);
        //2.重置mRectF
        float width = mRectF.right - mRectF.left;
        float height = mRectF.bottom - mRectF.top;
        float diffWidth = (scale * width - width) / 2.f;
        float diffHeight = (scale * height - height) / 2.f;
        mRectF.left -= diffWidth;
        mRectF.right += diffWidth;
        mRectF.top -= diffHeight;
        mRectF.bottom += diffHeight;
    }

    /**
     * 平移
     *
     * @param x 水平方向平移的距离
     * @param y 数值方向平移的距离
     */
    private void translate(float x, float y) {
        mMatrixArray[2] += x;
        mMatrixArray[5] += y;
        mRectF.left += x;
        mRectF.top += y;
        mRectF.right += x;
        mRectF.bottom += y;
        mMatrix.setValues(mMatrixArray);
        setImageMatrix(mMatrix);
    }

}

