package com.fan.fangallery;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * time: 2015/10/8
 * description:照片裁剪界面的布局
 *
 * @author fandong
 */
public class ImageCropTitleBar extends RelativeLayout {

    @InjectView(R.id.image_crop_title_txt)
    public TextView mGalleryName;

    private OnClickListener mOnBackClickListener;
    private OnClickListener mOnAheadClickListener;
    private OnClickListener mOnGalleryClickListener;

    public ImageCropTitleBar(Context context) {
        super(context);
        initView();
    }

    public ImageCropTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ImageCropTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.vw_gallery_image_crop_title, this);
        ButterKnife.inject(this, this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent __) {
        return true;
    }

    @OnClick({R.id.image_back, R.id.image_crop_title_txt, R.id.text_go_ahead})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.image_back:
                if (null != mOnBackClickListener) {
                    mOnBackClickListener.onClick(this);
                }
                break;
            case R.id.image_crop_title_txt:
                if (null != mOnBackClickListener) {
                    mOnGalleryClickListener.onClick(mGalleryName);
                }
                break;
            case R.id.text_go_ahead:
                if (null != mOnAheadClickListener) {
                    mOnAheadClickListener.onClick(this);
                }
                break;
            default:
                break;
        }
    }

    public void setTitle(String title) {
        mGalleryName.setText(title);
    }

    public ImageCropTitleBar setOnBackClickListener(OnClickListener onBackClickListener) {
        this.mOnBackClickListener = onBackClickListener;
        return this;
    }

    public ImageCropTitleBar setOnAheadClickListener(OnClickListener onAheadClickListener) {
        this.mOnAheadClickListener = onAheadClickListener;
        return this;
    }

    public ImageCropTitleBar setOnGalleryClickListener(OnClickListener onGalleryClickListener) {
        this.mOnGalleryClickListener = onGalleryClickListener;
        return this;
    }
}
