package com.fan.fangallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.fans.loader.core.listener.RecyclerPauseOnScrollListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * time: 2015/9/23
 * description:照片选择界面
 *
 * @author fandong
 */
public class GalleryActivity extends AppCompatActivity {
    @InjectView(R.id.recycler_view)
    public ScrollRecyclerView mRecyclerView;
    @InjectView(R.id.image_crop_view)
    public ImageCropView mImageCropView;
    @InjectView(R.id.scroll_linear_layout)
    public ScrollLinearLayout mScrollLinearLayout;
    @InjectView(R.id.image_crop_title)
    public ImageCropTitleBar mImageCropTitleBar;
    @InjectView(R.id.root_view)
    public View mRootView;
    private PopupWindow mPopupWindow;
    private GalleryAdapter mGalleryAdapter;
    private GridLayoutManager mLayoutManager;
    //如果ScrollLinearLayout处于顶部的状态,那么点击scrollLinearLayout会回退到底部状态
    private ImageCropView.OnClickInterceptListener mOnClickInterceptListener;
    //当recyclerView滑动停止时候，ScrollLinearLayout需要重置状态
    private RecyclerView.OnScrollListener mRecyclerOnScrollListener;
    //recyclerView对应的点击事件
    private GalleryAdapter.OnRecyclerItemClickListener mOnRecyclerItemClickListener;
    //处理重复点击
    private int mLastClickPosition = -1;
    private long mLastClickTime;


    {
        //1.初始化mOnClickInterceptListener
        mOnClickInterceptListener = () -> {
            if (mScrollLinearLayout.isTopState()) {
                mScrollLinearLayout.scrollToBottom();
                return true;
            }
            return false;
        };
        //2.初始化mRecyclerOnScrollListener
        mRecyclerOnScrollListener = new RecyclerView.OnScrollListener() {
            float limitY = ResHelper.getDimen(R.dimen.crop_image_operation_height);

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    /*当recyclerview的第一个空白视图的bottom大雨标题的bottom，那么scrollLinearlayout应该滑动到下面*/
                    boolean scrollToBottom = false;
                    View targetView = mLayoutManager.getChildAt(0);
                    if (0 == (int) targetView.getTag()) {
                        if (limitY < targetView.getBottom()) {
                            scrollToBottom = true;
                        }
                    }
                    mScrollLinearLayout.clipToBound(mRecyclerView::smoothScrollBy, scrollToBottom);
                    System.gc();
                } else {
                    if (mPopupWindow != null && mPopupWindow.isShowing()) {
                        mPopupWindow.dismiss();
                        mPopupWindow = null;
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                View view = mLayoutManager.getChildAt(0);
                if (dy > 0) {
                    mScrollLinearLayout.scrollBy((float) mRecyclerView.getLastTouchY(), dx, dy);
                } else {
                    if ((Integer) view.getTag() == 1) {
                        if (view.getTop() >= 0) {
                            mScrollLinearLayout.scrollBy(0.f, dx, dy);
                        }
                    }
                    if ((Integer) view.getTag() == 0) {
                        if (view.getBottom() > limitY) {
                            mScrollLinearLayout.scrollBy(0.f, dx, dy);
                        }
                    }
                }
            }
        };
        //3.初始化recyclerView的点击事件
        mOnRecyclerItemClickListener = (position, url, clickView) -> {
            if (1 != position && position == mLastClickPosition) {
                return;
            }
            mLastClickPosition = position;
            //3.0 如果是第一个方框，则需要启动拍照的界面
            if (1 == position && System.currentTimeMillis() - mLastClickTime > 3000) {
                mLastClickTime = System.currentTimeMillis();
                Toast.makeText(GalleryActivity.this, "点击拍照按钮", Toast.LENGTH_SHORT).show();
                return;
            }
            int old = mGalleryAdapter.getSelectedPosition();
            mGalleryAdapter.setSelectedPosition(position);
            mGalleryAdapter.notifyItemChanged(old);
            mGalleryAdapter.notifyItemChanged(position);
            mImageCropView.setImageURI(url);
            //3.1.如果linearLayout是悬浮在上面的，就下滑至原来位置
            mScrollLinearLayout.scrollToBottom();
            //3.2.滑动item到指定位置
            mRecyclerView.smoothScrollBy(0, (int) (clickView.getTop() - mScrollLinearLayout.getFullViewHeight() + 0.5f));
        };
    }

    public static void launch(Context context) {
        Intent intent = new Intent(context, GalleryActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_gallery);
        ButterKnife.inject(this);
        initView();
    }

    private void initView() {
        //2.如果ScrollLinearLayout处于顶部的状态,那么点击scrollLinearLayout会回退到底部状态
        mImageCropView.setOnClickInterceptListener(mOnClickInterceptListener);
        //3.设置适配器
        mGalleryAdapter = new GalleryAdapter(this);
        mImageCropView.setImageURI(mGalleryAdapter.getUrl(0));
        mRecyclerView.addItemDecoration(new GridRecyclerDecoration((int) PixelUtil.dp2px(2.f), 0x242424));
        //4.得到LayoutManager
        mLayoutManager = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0) {
                    return 4;
                }
                return 1;
            }
        });
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mGalleryAdapter);
        mRecyclerView.setItemViewCacheSize(0);
        mRecyclerView.setDrawingCacheEnabled(false);
        //5. 当recyclerView滑动停止时候，ScrollLinearLayout需要重置状态
        /*如果为idle，那么ImageLoader应该停止加载图片和保存图片*/
        mRecyclerView.addOnScrollListener(new RecyclerPauseOnScrollListener(true, true));
        mRecyclerView.addOnScrollListener(mRecyclerOnScrollListener);
        //6.添加点击事件
        mGalleryAdapter.setOnRecyclerItemClickListener(mOnRecyclerItemClickListener);
        //7.给title添加点击事件
        mImageCropTitleBar.setOnBackClickListener(__ -> finish())
                .setOnAheadClickListener(__ -> onConfirmClick())
                .setOnGalleryClickListener(v -> showGalleryPopupWindow());
    }


    /**
     * 当点击Menu键，或者点击标题栏，都会触发此方法，会呼起相册选择界面
     */
    public void showGalleryPopupWindow() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        } else {
            //1.获取到数据
            List<Gallery> galleryList = LocalPhotoManager.getInstance().getLocalGallery();
            //2.
            RecyclerView recyclerView = (RecyclerView) View.inflate(this, R.layout.vw_gallery_pop, null);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(layoutManager);
            GalleryNameAdapter adapter = new GalleryNameAdapter(this, galleryList);
            recyclerView.setAdapter(adapter);
            adapter.setOnRecyclerItemClickListener((position, __) -> {
                //1.修改标题栏的相册名称
                String galleryName = galleryList.get(position).getGalleryName();
                mImageCropTitleBar.setTitle(galleryName);
                //2.将popwindow消失
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                }
                //3.相册内容改变
                mGalleryAdapter.refresh(galleryList.get(position).getPictures());
                mGalleryAdapter.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(0);
                //4.改变裁剪图片的内容
                mImageCropView.setImageURI(mGalleryAdapter.getUrl(0));
                if (mScrollLinearLayout.isTopState()) {
                    mScrollLinearLayout.scrollToBottom();
                }
            });

            int width = (int) (this.getResources().getDisplayMetrics().widthPixels * 0.5f);
            //确定显示的高度
            int height = (int) (ResHelper.getDimen(R.dimen.gallery_pop_item_height) * galleryList.size());
            int limitHeight = (int) (this.getResources().getDisplayMetrics().heightPixels * 0.6f);
            if (height > limitHeight) {
                height = limitHeight;
            }
            mPopupWindow = new PopupWindow(recyclerView, width, height);
            Drawable drawable = new ColorDrawable(0xf1131313);
            mPopupWindow.setBackgroundDrawable(drawable);
            mPopupWindow.setOutsideTouchable(true);
            boolean isTopState = mScrollLinearLayout.isTopState();
            if (isTopState) {
                mPopupWindow.showAtLocation(mRootView, Gravity.LEFT | Gravity.TOP, 0, ResHelper.getStatusBarHeight());
            } else {
                mPopupWindow.showAsDropDown(mImageCropTitleBar, 0, 0);
            }


        }
    }

    /**
     * 标题栏，勾号点击对应的事件
     */
    private void onConfirmClick() {
        //1.执行裁剪
        Bitmap bitmap = mImageCropView.doCrop();
        Toast.makeText(this, "裁剪成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            if (LocalPhotoManager.getInstance().isGalleryValidate()) {
                showGalleryPopupWindow();
            }
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
                mPopupWindow = null;
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 当从拍照界面拍完照片之后，会调用该方法更新相册界面
     */
    public void refresh() {
        //1.修改标题栏的相册名称
        mImageCropTitleBar.setTitle("图库");
        //2.将popwindow消失
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
        //3.相册内容改变
        mGalleryAdapter.refresh();
        mGalleryAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);
        //4.改变裁剪图片的内容
        mImageCropView.setImageURI(mGalleryAdapter.getUrl(0));
        if (mScrollLinearLayout.isTopState()) {
            mScrollLinearLayout.scrollToBottom();
        }
    }


}
