package com.fan.fangallery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * time: 2015/9/28
 * description:
 *
 * @author fandong
 */
public class CropMask extends ImageView {
    public static final int TYPE_1_1 = 1;
    public static final int TYPE_4_3 = 2;
    public static final int TYPE_3_4 = 3;
    private int mImageType = TYPE_1_1;
    private float mWidth;
    private float mHeight;
    private int mLeft;
    private int mTop;
    private int mBottom;
    private int mAspectX = 1;
    private int mAspectY = 1;
    //高亮区域的高度
    private float mRecHeight;
    private float mRecWidth;
    //上下黑边的高度
    private int mGrayHeight;
    private Paint mMaskPaint;
    private Paint mBorderPaint;

    private RectF mCropRectF;

    public CropMask(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMaskPaint = new Paint();
        mMaskPaint.setColor(0xff17181a);
        mBorderPaint = new Paint();
        mBorderPaint.setColor(Color.WHITE);
        mWidth = context.getResources().getDisplayMetrics().widthPixels;
        mHeight = context.getResources().getDimension(R.dimen.crop_image_height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mLeft = getLeft();
        mTop = getTop();
        mBottom = getBottom();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //1.第一次进来的时候需要计算区域
        mCropRectF = getCropRectF();
        canvas.save();
        //2.绘制左右两个灰块
        float grayWidth = (mWidth - mRecWidth) / 2.f;
        if (mCropRectF.left > mLeft) {
            canvas.drawRect(mLeft, mTop, grayWidth, mHeight, mMaskPaint);
            canvas.drawRect(mLeft + grayWidth + mRecWidth, mTop, mWidth, mHeight, mMaskPaint);
        }
        canvas.drawRect(mLeft + grayWidth, mTop, mRecWidth + mLeft + grayWidth, mGrayHeight, mMaskPaint);
        canvas.drawRect(mLeft + grayWidth, mTop + mGrayHeight + mRecHeight, mLeft + grayWidth + mRecWidth, mHeight, mMaskPaint);
        canvas.restore();
    }

    public RectF getCropRectF() {
        if (mCropRectF == null) {
            mCropRectF = getRectF();
        }
        return mCropRectF;
    }

    /**
     * 得到中间区域的矩阵
     *
     * @return 中间区域的矩阵
     */
    public RectF getRectF() {
        // 高亮区域的高度
        mRecWidth = mWidth;
        mRecHeight = Math.round((mAspectY / (float) mAspectX) * mRecWidth);
        if (mRecHeight > mHeight) {
            mRecHeight = mHeight;
            mRecWidth = Math.round((mAspectX / (float) mAspectY) * mRecHeight);
        }
        // 上下灰边的高度
        mGrayHeight = Math.round((mHeight - mRecHeight) / 2.f);
        return new RectF(mLeft + (mWidth - mRecWidth) / 2.f,
                mTop + mGrayHeight,
                mLeft + mRecWidth + (mWidth - mRecWidth) / 2.f,
                mTop + mGrayHeight + mRecHeight);
    }

    /**
     * 设置CropMask的宽高比
     *
     * @param aspectX 横向比例
     * @param aspectY 纵向比例
     */
    private void setAspect(int aspectX, int aspectY) {
        boolean needValidate = aspectX != mAspectX || aspectY != mAspectY;
        mAspectX = aspectX;
        mAspectY = aspectY;
        if (needValidate) {
            mCropRectF = getRectF();
            invalidate();
        }
    }

    /**
     * 得到蒙层的比例值
     *
     * @return 蒙层的比例值
     */
    public float getAspect() {
        return mAspectX / (float) mAspectY;
    }

    /**
     * 设置图片的尺寸类型
     *
     * @param type 尺寸类型
     */
    public void setImageType(int type) {
        //1.设置图片类型
        mImageType = type;
        //2.设置宽高比
        switch (mImageType) {
            case TYPE_1_1:
                setAspect(1, 1);
                break;
            case TYPE_3_4:
                setAspect(3, 4);
                break;
            case TYPE_4_3:
                setAspect(4, 3);
                break;
            default:
                break;
        }
    }

    /**
     * 转换尺寸的类型
     *
     * @return 代表当前需要转换的类型，用来确定前面界面显示的资源图片
     */
    public int switchAspect() {
        int aspectX = 1;
        switch (mImageType) {
            case TYPE_1_1:
                if (mAspectX == 4) {
                    setAspect(3, 4);
                    aspectX = 3;
                } else if (mAspectX == 1) {
                    setAspect(4, 3);
                    aspectX = 4;
                } else {
                    setAspect(1, 1);
                    aspectX = 1;
                }
                break;
            case TYPE_3_4:
                if (mAspectX == 3) {
                    setAspect(1, 1);
                    aspectX = 1;
                } else if (mAspectX == 1) {
                    setAspect(3, 4);
                    aspectX = 3;
                }
                break;
            case TYPE_4_3:
                if (mAspectX == 4) {
                    setAspect(1, 1);
                    aspectX = 1;
                } else if (mAspectX == 1) {
                    setAspect(4, 3);
                    aspectX = 4;
                }
                break;
            default:
                break;
        }
        return aspectX;
    }

}

