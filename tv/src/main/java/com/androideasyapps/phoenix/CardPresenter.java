/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.androideasyapps.phoenix;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androideasyapps.phoenix.dao.MediaFile;
import com.androideasyapps.phoenix.shared.AppInstance;
import com.androideasyapps.phoenix.util.MediaUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.net.URI;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
    private static Context mContext;
    private static int CARD_WIDTH = 150*2;
    private static int CARD_HEIGHT = 200*2;

    static class ViewHolder extends Presenter.ViewHolder {
        private MediaFile mMovie;
        private ImageCardView mCardView;
        private Drawable mDefaultCardImage;

        public ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
            mDefaultCardImage = mContext.getResources().getDrawable(R.drawable.movie);
        }

        public void setMovie(MediaFile m) {
            mMovie = m;
        }

        public MediaFile getMovie() {
            return mMovie;
        }

        public ImageCardView getCardView() {
            return mCardView;
        }

        protected void updateCardViewImageUIL(URI uri) {
            mCardView.getMainImageView().setImageBitmap(null);
                ImageLoader.getInstance().loadImage(uri.toString(), new ImageSize(Utils.convertDpToPixel(mContext, CARD_WIDTH),
                        Utils.convertDpToPixel(mContext, CARD_HEIGHT)), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap bitmap) {
                        Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
                        mCardView.setMainImage(bitmapDrawable);
                    }
                });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        mContext = parent.getContext();

        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.setBackgroundColor(mContext.getResources().getColor(R.color.fastlane_background));
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        MediaFile movie = (MediaFile) item;
        ((ViewHolder) viewHolder).setMovie(movie);

        if (movie.getMediaFileId()==0 && movie.getParts()>1) {
            ((ViewHolder) viewHolder).mCardView.setTitleText(movie.getTitle());
            ((ViewHolder) viewHolder).mCardView.setContentText(String.valueOf(movie.getParts() + " Items"));
        } else {
            ((ViewHolder) viewHolder).mCardView.setTitleText(movie.getTitle());
            if ("tv".equalsIgnoreCase(movie.getMediaType())) {
                ((ViewHolder) viewHolder).mCardView.setContentText(getTVContextText(movie));
            } else {
                ((ViewHolder) viewHolder).mCardView.setContentText(getMovieContentText(movie));
            }
        }
        ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
        ((ViewHolder) viewHolder).updateCardViewImageUIL(URI.create(MediaUtil.getPosterURL(AppInstance.getInstance(mContext).getServer(), movie, CARD_WIDTH)));
    }

    private CharSequence getTVContextText(MediaFile file) {
        if (!MediaUtil.isGroupedItem(file) && file.getMediaFileId()==0) {
            // scheduled recording
            if (file.getEpisode() > 0) {
                return String.format("%s %dx%d %s", MediaUtil.formatShortDateTimeForAiring(file), file.getSeason(), file.getEpisode(), file.getEpisodeTitle());
            } else {
                return String.format("%s %s", MediaUtil.formatShortDateTimeForAiring(file), file.getEpisodeTitle());
            }
        } else {
            if (file.getEpisode() > 0) {
                return String.format("%dx%d %s", file.getSeason(), file.getEpisode(), file.getEpisodeTitle());
            } else {
                return file.getEpisodeTitle();
            }
        }
    }

    private CharSequence getMovieContentText(MediaFile movie) {
        long minutes = movie.getDuration() / (60 * 1000);
        String str = String.format("%d mins", minutes);
        return str + ((movie.getGenre()!=null)?(" " + movie.getGenre()):"");
    }

    // not used
    private Drawable getBadge(MediaFile movie) {
        if (true) {
            return mContext.getResources().getDrawable(R.drawable.ic_action_a);
        }

        TextView tv = new TextView(mContext);
        tv.setDrawingCacheEnabled(true);
        tv.setText(String.valueOf(movie.getParts()));
        tv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
        tv.buildDrawingCache(true);



        Drawable drawable = new BitmapDrawable(tv.getDrawingCache());
        return drawable;
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
    }

    @Override
    public void onViewAttachedToWindow(Presenter.ViewHolder viewHolder) {
        // TO DO
    }
}
