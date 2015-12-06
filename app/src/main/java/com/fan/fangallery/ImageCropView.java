package com.fan.fangallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fans.loader.FanImageLoader;
import com.fans.loader.internal.core.assist.FailReason;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * time: 2015/9/25
 * description:相册选择界面，相册，的头部裁剪的部分
 *
 * @author fandong
 */
public class ImageCropView extends RelativeLayout {
    @InjectView(R.id.crop_touch_image_view)
    public TouchImageView mTouchImageView;
    @InjectView(R.id.crop_mask)
    public CropMask mCropMask;
    @InjectView(R.id.image_crop_attach)
    public ImageView mAttach;
    @InjectView(R.id.image_crop_ratio)
    public ImageView mRatio;
    @InjectView(R.id.image_crop_rotate)
    public ImageView mRotate;
    /*默认是吸附状态*/
    private boolean mIsAttach = true;
    /*点击事件拦截的监听*/
    private OnClickInterceptListener mOnClickInterceptListener;
    private String mUri;
    private int mTargetSize;

    public ImageCropView(Context context) {
        super(context);
        initView(context);
    }

    public ImageCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ImageCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mTargetSize = context.getResources().getDisplayMetrics().widthPixels;
        inflate(context, R.layout.vw_gallery_crop_header, this);
        ButterKnife.inject(this);
    }

    public void setOnClickInterceptListener(OnClickInterceptListener listener) {
        this.mOnClickInterceptListener = listener;
    }


    public void setImageURI(String uri) {
        if (TextUtils.isEmpty(uri) || uri.equals(mUri)) {
            return;
        }
        mUri = uri;
        FanImageLoader.create(mUri)
                .setShowSize(mTargetSize, mTargetSize)
                .setImageLoadinglistener(new FanImageLoader.DefaultLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        onLoadBitmapComplete(loadedImage);
                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {
                        super.onLoadingFailed(s, view, failReason);
                    }

                    @Override
                    public void onLoadingStarted(String s, View view) {
                    }
                })
                .load();
    }

    @OnClick({R.id.image_crop_attach, R.id.image_crop_ratio, R.id.image_crop_rotate})
    public void onClick(View view) {
        //1.确定是否拦截点击事件
        if (mOnClickInterceptListener != null) {
            if (mOnClickInterceptListener.clickIntercept()) {
                return;
            }
        }
        //2.点击事件
        switch (view.getId()) {
            case R.id.image_crop_rotate:
                mTouchImageView.rotate();
                break;
            case R.id.image_crop_attach:
                //1.重置吸附状态
                mIsAttach = !mIsAttach;
                //2.改变按钮形状
                if (mIsAttach) {
                    mAttach.setImageResource(R.drawable.crop_attach_full);
                } else {
                    mAttach.setImageResource(R.drawable.crop_attach_part);
                }
                //3.设置蒙层的Rect给TouchImageView,进行适配显示
                mTouchImageView.setAttach(mCropMask.getCropRectF(), mIsAttach);
                break;
            case R.id.image_crop_ratio:
                //1.蒙层设置比例
                int type = mCropMask.switchAspect();
                //2.比例按钮
                mRatio.setImageResource(type == 1 ? R.drawable.crop_1_1 :
                        3 == type ? R.drawable.crop_3_4 : R.drawable.crop_4_3);
                //3.吸附的处理
                mTouchImageView.setAttach(mCropMask.getCropRectF(), mIsAttach);
                break;
            default:
                break;
        }
    }

    /**
     * 当加载图片完成时候调用
     *
     * @param bitmap
     */
    private void onLoadBitmapComplete(Bitmap bitmap) {
        //1.得到宽高信息并处理蒙层比例
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float wh = width / (float) height;
        if (wh > 1.33f) {
            mCropMask.setImageType(CropMask.TYPE_4_3);
            mRatio.setImageResource(R.drawable.crop_4_3);
        } else if (wh < 0.75f) {
            mCropMask.setImageType(CropMask.TYPE_3_4);
            mRatio.setImageResource(R.drawable.crop_3_4);
        } else {
            mCropMask.setImageType(CropMask.TYPE_1_1);
            mRatio.setImageResource(R.drawable.crop_1_1);
        }
        //2.设置蒙层的矩阵
        mTouchImageView.setCropRectF(mCropMask.getCropRectF());
        //3.进行显示
        mTouchImageView.setImageBitmap(bitmap);
    }


    public Bitmap doCrop() {
        RectF cropRectF = mCropMask.getCropRectF();
        float width = cropRectF.right - cropRectF.left;
        float height = cropRectF.bottom - cropRectF.top;
        Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(255, 255, 255, 255);
        canvas.translate(-cropRectF.left, -cropRectF.top);
        mTouchImageView.draw(canvas);
        return bitmap;
    }

    public interface OnClickInterceptListener {
        boolean clickIntercept();
    }


}
