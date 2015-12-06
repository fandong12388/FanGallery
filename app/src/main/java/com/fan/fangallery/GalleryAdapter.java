package com.fan.fangallery;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fans.loader.FanImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * time: 2015/9/23
 * description:
 *
 * @author fandong
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
    private Context mContext;
    private LayoutInflater mInflater;
    private Map<Integer, String> mMapPhotos = new HashMap<>();
    private List<Integer> mPhotos = new ArrayList<>();
    /*注意：mItemSize的值不能随意修改，在LocalPhotoManager当中会用到*/
    private int mItemSize;
    private int mSelectedPosition = 2;

    private OnRecyclerItemClickListener mOnRecyclerItemClickListener;

    public GalleryAdapter(Context context) {
        this.mContext = context;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mItemSize = (int) (mContext.getResources().getDisplayMetrics().widthPixels / 4.f + 0.5f - 3.f);
        initAdapter();
    }

    private void initAdapter() {
        this.mMapPhotos.putAll(LocalPhotoManager.getInstance().getLocalPhotos());
        this.mPhotos.clear();
        for (Map.Entry<Integer, String> entry : mMapPhotos.entrySet()) {
            mPhotos.add(entry.getKey());
        }
    }

    public void refresh() {
        this.mMapPhotos = LocalPhotoManager.getInstance().getLocalPhotos();
        this.mItemSize = (int) (mContext.getResources().getDisplayMetrics().widthPixels / 4.f + 0.5f - 3.f);
        initAdapter();
    }

    public void refresh(TreeMap<Integer, String> photos) {
        //1.重置map
        mMapPhotos.clear();
        mMapPhotos.putAll(photos);
        //2.重置photos
        this.mPhotos.clear();
        for (Map.Entry<Integer, String> entry : mMapPhotos.entrySet()) {
            mPhotos.add(entry.getKey());
        }
        //3.设置选中
        mSelectedPosition = 2;
    }

    public void setSelectedPosition(int position) {
        this.mSelectedPosition = position;
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener listener) {
        this.mOnRecyclerItemClickListener = listener;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }


    public String getUrl(int position) {
        Integer resourceId = mPhotos.get(position);
        String path = mMapPhotos.get(resourceId);
        String url;
        if (!TextUtils.isEmpty(path)) {
            url = "file://" + path;
        } else {
            url = "content://media/external/images/media/" + resourceId;
        }
        return url;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (0 == viewType) {
            view = mInflater.inflate(R.layout.vw_gallery_header, parent, false);
        } else if (1 == viewType) {
            view = mInflater.inflate(R.layout.vw_gallery_camera_item, parent, false);
        } else {
            view = mInflater.inflate(R.layout.vw_gallery_item, parent, false);
        }
        view.setTag(viewType);
        return new GalleryViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, int position) {
        if (1 < position) {
            //1.设置宽高
            GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(mItemSize, mItemSize);
            holder.view.setLayoutParams(params);
            //2.获取相应的资源进行显示
            String url = getUrl(position - 2);
            FanImageLoader.create(url)
                    .setDisplayType(FanImageLoader.DISPLAY_FADE_IN)
                    .setFadeInTime(800)
                    .setShowSize(mItemSize, mItemSize)
                    .setDefaultDrawable(new ColorDrawable(0x00000000))
                    .into(holder.mImageView);
            holder.mImageView.setOnClickListener(v -> {
                if (mOnRecyclerItemClickListener != null) {
                    Integer resourceId = mPhotos.get(position - 2);
                    String uri = "content://media/external/images/media/" + resourceId;
                    mOnRecyclerItemClickListener.onItemClick(position, uri, holder.view);
                }
            });
            holder.mImageView.setIsSelected(mSelectedPosition == position);
        } else if (1 == position) {
            holder.view.setOnClickListener(v -> {
                if (mOnRecyclerItemClickListener != null) {
                    mOnRecyclerItemClickListener.onItemClick(position, null, holder.view);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mPhotos.size() + 2;
    }


    public interface OnRecyclerItemClickListener {
        void onItemClick(int position, String url, View view);
    }

    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.iv)
        public OnPressImageView mImageView;

        public View view;

        public GalleryViewHolder(View itemView, int viewType) {
            super(itemView);
            view = itemView;
            if (viewType > 1) {
                ButterKnife.inject(this, itemView);
            }
        }
    }
}
