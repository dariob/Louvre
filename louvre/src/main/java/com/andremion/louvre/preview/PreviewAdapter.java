/*
 * Copyright (c) 2017. André Mion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andremion.louvre.preview;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import com.andremion.louvre.R;
import com.andremion.louvre.util.transition.MediaSharedElementCallback;
import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static android.view.View.NO_ID;

class PreviewAdapter extends PagerAdapter {

    interface Callbacks {

        void onCheckedUpdated(boolean checked);

        void onMaxSelectionReached();
    }

    private final FragmentActivity mActivity;
    private final LayoutInflater mInflater;
    private final CheckedTextView mCheckbox;
    private final MediaSharedElementCallback mSharedElementCallback;
    private final HashMap<Long, Uri> mSelection;
    @Nullable
    private PreviewAdapter.Callbacks mCallbacks;
    private int mMaxSelection;
    private int mInitialPosition;
    @Nullable
    private Cursor mData;
    private boolean mDontAnimate;
    private int mCurrentPosition = RecyclerView.NO_POSITION;

    PreviewAdapter(@NonNull FragmentActivity activity, @NonNull CheckedTextView checkbox, @NonNull MediaSharedElementCallback sharedElementCallback, @NonNull HashMap<Long, Uri> selection) {
        mActivity = activity;
        mInflater = LayoutInflater.from(activity);
        mCheckbox = checkbox;
        mSharedElementCallback = sharedElementCallback;
        mSelection = selection;
        mDontAnimate = true;
    }

    void setCallbacks(@Nullable PreviewAdapter.Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    void setMaxSelection(@IntRange(from = 0) int maxSelection) {
        mMaxSelection = maxSelection;
    }

    void setInitialPosition(int position) {
        mInitialPosition = position;
    }

    void swapData(Cursor data) {
        if (data != mData) {
            mData = data;
            notifyDataSetChanged();
        }
    }

    void setDontAnimate(boolean dontAnimate) {
        mDontAnimate = dontAnimate;
    }

    @Override
    public int getCount() {
        if (mData != null && !mData.isClosed()) {
            return mData.getCount();
        }
        return 0;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mInflater.inflate(R.layout.page_item_preview, container, false);
        ViewHolder holder = new ViewHolder(view);
        Uri data = getData(position);
        onViewBound(holder, position, data);
        container.addView(holder.itemView);
        return holder;
    }

    @Nullable
    Uri getData(int position) {
        if (mData != null && !mData.isClosed()) {
            mData.moveToPosition(position);
            return Uri.fromFile(new File(mData.getString(mData.getColumnIndex(MediaStore.Images.Media.DATA))));
        }
        return null;
    }

    private long getItemId(int position) {
        if (mData != null && !mData.isClosed()) {
            mData.moveToPosition(position);
            return mData.getLong(mData.getColumnIndex(MediaStore.Images.ImageColumns._ID));
        }
        return NO_ID;
    }

    private void onViewBound(ViewHolder holder, int position, Uri data) {
        String imageTransitionName = holder.imageView.getContext().getString(R.string.activity_gallery_image_transition, data.toString());
        ViewCompat.setTransitionName(holder.imageView, imageTransitionName);

        // Create global configuration and initialize ImageLoader with this config
 /*       ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mActivity)
            .build();
        ImageLoader.getInstance().init(config);

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.loadImage(data, new ImageLoadingCallback(position));/ */


     //Request request;
     //request = (Request) Glide.with(mActivity).load(data).into(holder.imageView,  ImageLoadingCallback(position));

    /*    Glide.with(mActivity)
                .load(data)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)

                .into(holder.imageView);
*/







    //    int pinto = Glide.with(mActivity).load(data).into(holder.imageView, new ImageLoadingCallback(position));

        RequestCreator request = Picasso.with(mActivity)
                .load(data)
                .config(Bitmap.Config.RGB_565);
          //      .resize(holder.imageView.getMaxWidth(), holder.imageView.getMaxHeight());
            //.memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE);
            //    .fit()
           //    .centerInside();

        if (mDontAnimate) {
            request.noFade();
        }
         request.into(holder.imageView, new ImageLoadingCallback(position));
    }

    private boolean isSelected(int position) {
        long itemId = getItemId(position);
        return mSelection.containsKey(itemId);
    }

    private void startPostponedEnterTransition(int position) {
        if (position == mInitialPosition) {
            mActivity.supportStartPostponedEnterTransition();
        }
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (object instanceof ViewHolder) {
            mCurrentPosition = position;
            mSharedElementCallback.setSharedElementViews(((ViewHolder) object).imageView, mCheckbox);
            if (mCallbacks != null) {
                mCallbacks.onCheckedUpdated(isSelected(position));
            }
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return object instanceof ViewHolder
                && view.equals(((ViewHolder) object).itemView);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = ((ViewHolder) object).itemView;
        container.removeView(view);
    }

    void selectCurrentItem() {
        boolean selectionChanged = handleChangeSelection(mCurrentPosition);
        if (selectionChanged) {
            notifyDataSetChanged();
        }
        if (mCallbacks != null) {
            if (selectionChanged) {
                mCallbacks.onCheckedUpdated(isSelected(mCurrentPosition));
            } else {
                mCallbacks.onMaxSelectionReached();
            }
        }
    }

    LinkedHashMap<Long, Uri> getRawSelection() {
        return new LinkedHashMap<>(mSelection);
    }

    private boolean handleChangeSelection(int position) {
        final long itemId = getItemId(position);
        if (!isSelected(position)) {
            if (mSelection.size() == mMaxSelection) {
                return false;
            }
            mSelection.put(itemId, getData(position));
        } else {
            mSelection.remove(itemId);
        }
        return true;
    }

    private static class ViewHolder {

        final View itemView;
        final ImageView imageView;

        ViewHolder(View view) {
            itemView = view;
            imageView = (ImageView) view.findViewById(R.id.image);
        }

    }

    private class ImageLoadingCallback implements Callback, ImageLoadingListener {

        final int mPosition;

        ImageLoadingCallback(int position) {
            mPosition = position;
        }

        @Override
        public void onSuccess() {
            startPostponedEnterTransition(mPosition);
        }

        @Override
        public void onError() {
            startPostponedEnterTransition(mPosition);
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {

        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    }

}
