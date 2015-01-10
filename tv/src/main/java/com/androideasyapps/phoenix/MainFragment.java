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

import java.net.URI;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androideasyapps.phoenix.dao.H2PersistenceManager;
import com.androideasyapps.phoenix.dao.MediaFile;
import com.androideasyapps.phoenix.dao.TVSeriesGroupMediaFileDAO;
import com.androideasyapps.phoenix.recommend.UpdateRecommendationsService;
import com.androideasyapps.phoenix.services.phoenix.ViewItemMediaFileAdapter;
import com.androideasyapps.phoenix.services.phoenix.model.ViewItem;
import com.androideasyapps.phoenix.services.phoenix.model.ViewReply;
import com.androideasyapps.phoenix.settings.SettingsActivity;
import com.androideasyapps.phoenix.settings.SettingsFragment;
import com.androideasyapps.phoenix.shared.AppInstance;
import com.androideasyapps.phoenix.shared.MediaSource;
import com.androideasyapps.phoenix.shared.MediaSyncEvent;
import com.androideasyapps.phoenix.shared.SageTVSyncService;
import com.androideasyapps.phoenix.util.MediaUtil;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Subscriber;
import rx.android.observables.AndroidObservable;
import rx.functions.Action0;
import rx.functions.Action1;


public class MainFragment extends BrowseFragment {
    private static final Logger log = LoggerFactory.getLogger(MainFragment.class);
    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;

    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private Target mBackgroundTarget;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private final Handler mHandler = new Handler();
    private URI mBackgroundURI;
    CardPresenter mCardPresenter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();

        setupUIElements();

        loadRows();

        setupEventListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
        AppInstance.getInstance(getActivity()).bus().unregister(this);
    }

    private void loadRows() {


        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        mCardPresenter = new CardPresenter();

        int i=0;

        mRowsAdapter.clear();

        for (MediaSource source: AppInstance.getInstance(getActivity()).getMediaSources().values()) {
            // build recently added movies, unwatched
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(mCardPresenter);
            HeaderItem header = new HeaderItem(i++, source.title, null);
            mRowsAdapter.add(new ListRow(header, listRowAdapter));
            populateSource(listRowAdapter, source);
        }

        // build Scheduled Recordings...
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(mCardPresenter);
        HeaderItem header = new HeaderItem(i++, "Scheduled Recordings", null);
        mRowsAdapter.add(new ListRow(header, listRowAdapter));
        populateScheduledRecordings(listRowAdapter);

        // TV Gaps
//        ArrayObjectAdapter gapsRowAdapter = new ArrayObjectAdapter(mCardPresenter);
//        HeaderItem gapsHeader = new HeaderItem(i++, "TV Gaps", null);
//        mRowsAdapter.add(new ListRow(gapsHeader, gapsRowAdapter));
//        populateTVGaps(gapsRowAdapter);


        // build Prefs
        HeaderItem gridHeader = new HeaderItem(i, "PREFERENCES", null);

        SettingsPresenter mGridPresenter = new SettingsPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        gridRowAdapter.add(getResources().getString(R.string.refresh));
        gridRowAdapter.add(getResources().getString(R.string.start_recommendations));
//        gridRowAdapter.add(getString(R.string.error_fragment));
        gridRowAdapter.add(getResources().getString(R.string.personal_settings));
        mRowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        setAdapter(mRowsAdapter);
    }

    private void populateTVGaps(final ArrayObjectAdapter gapsRowAdapter) {
        TVGapsUtil.getTVGapFilesObserable(AppInstance.getInstance(getActivity()).getDAOManager()).subscribe(new Action1<Collection<MediaFile>>() {
            @Override
            public void call(Collection<MediaFile> mediaFiles) {
                if (mediaFiles!=null && mediaFiles.size()>0) {
                    gapsRowAdapter.addAll(0, mediaFiles);
                }
            }
        });
    }

    private void populateSource(ArrayObjectAdapter listRowAdapter, MediaSource source) {
        if (source.subGroupMediaSource==null) {
            populateListRowAdapter(listRowAdapter, source.query);
        } else {
            populateListRowAdapterForGroup(listRowAdapter, source);
        }
    }

    private void populateScheduledRecordings(final ArrayObjectAdapter listRowAdapter) {
        AppInstance.getInstance(getActivity()).getPhoenixService().getScheduledRecordings().subscribe(new Action1<ViewReply>() {
            @Override
            public void call(ViewReply viewReply) {
                if (viewReply.reply!=null && viewReply.reply.children!=null) {
                    for (ViewItem vi: viewReply.reply.children) {
                        listRowAdapter.add(new ViewItemMediaFileAdapter(vi));
                    }
                } else {
                    log.warn("No Items for getScheduledRecordings()");
                }
            }
        });
    }

    private void populateListRowAdapterForGroup(final ArrayObjectAdapter listRowAdapter, final MediaSource source) {
        getMediaItemsUsingGroupedQuery(AppInstance.getInstance(this.getActivity()).getDAOManager(), source.query).subscribe(new Action1<Collection<MediaFile>>() {
            @Override
            public void call(Collection<MediaFile> mediaFiles) {
                if (mediaFiles != null) {
                    for (MediaFile mf : mediaFiles) {
                        listRowAdapter.add(mf);
                        mf.setUserdata(source.title);
                    }
                }
            }
        });

    }

    private void populateListRowAdapter(final ArrayObjectAdapter listRowAdapter, final String query) {
        AppInstance.getInstance(getActivity()).getMediaItems(AppInstance.getInstance(this.getActivity()).getDAOManager(), query).subscribe(new Action1<Collection<MediaFile>>() {
            @Override
            public void call(Collection<MediaFile> mediaFiles) {
                if (mediaFiles != null && mediaFiles.size() > 0) {
                    for (MediaFile mf : mediaFiles) {
                        listRowAdapter.add(mf);
                    }
                } else {
                    log.warn("No Media FOR: " + query);
                }
            }
        });
    }

    Observable<Collection<MediaFile>> getMediaItemsUsingGroupedQuery(final Future<H2PersistenceManager> dao, final String query) {
        return Observable.create(new Observable.OnSubscribe<Collection<MediaFile>>() {
            @Override
            public void call(Subscriber<? super Collection<MediaFile>> subscriber) {
                subscriber.onStart();
                try {
                    TVSeriesGroupMediaFileDAO tvDAO = new TVSeriesGroupMediaFileDAO(dao.get());
                    subscriber.onNext(tvDAO.rawQuery(query));
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }



    private void prepareBackgroundManager() {

        BackgroundManager backgroundManager = BackgroundManager.getInstance(getActivity());
        backgroundManager.attach(getActivity().getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget(backgroundManager);

        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);

        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        // setBadgeDrawable(getActivity().getResources().getDrawable(
        // R.drawable.videos_by_google_banner));
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.fastlane_background));
        // set search icon color
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SageTVSearchActivity.class);
                startActivity(intent);
             }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());

        AppInstance.getInstance(getActivity()).bus().register(this);
    }

    @Subscribe
    public void onMediaSync(MediaSyncEvent event) {
        log.info("Subscribing to Media Sync items");
        final AtomicInteger counter = new AtomicInteger();
        AndroidObservable.bindFragment(this, event.getObservable()).subscribe(new Action1<MediaFile>() {
            @Override
            public void call(MediaFile mediaFile) {
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                log.error("Error During Sync", throwable);
           }
        }, new Action0() {
            @Override
            public void call() {
                log.info("Reloading Rows");
                loadRows();
                Toast.makeText(getActivity(), R.string.msg_syncing_mediafiles_complete, Toast.LENGTH_LONG).show();
            }
        });
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof MediaFile) {
                MediaFile movie = (MediaFile) item;
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                //intent.putExtra(DetailsActivity.MOVIE, movie);
                AppInstance.getInstance(MainFragment.this.getActivity()).setSelectedMediaFile(movie);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);
            } else if (item instanceof String) {
                if (((String) item).contains(getString(R.string.refresh))) {
                    Toast.makeText(getActivity(), R.string.msg_syncing_mediafiles, Toast.LENGTH_LONG).show();
                    SageTVSyncService.startSageTVSync(getActivity(), null);
                } else if (((String) item).contains(getString(R.string.start_recommendations))) {
                    Intent intent = new Intent(getActivity(), UpdateRecommendationsService.class);
                    getActivity().startService(intent);
                    Toast.makeText(getActivity(), "Recommendations will soon show on the Home screen", Toast.LENGTH_SHORT).show();
                } else if (((String) item).contains(getString(R.string.error_fragment))) {
                    Intent intent = new Intent(getActivity(), BrowseErrorActivity.class);
                    startActivity(intent);
                } else if (((String) item).contains(getString(R.string.personal_settings))) {
                    Intent i = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }
    }


    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof MediaFile) {
                mBackgroundURI = URI.create(MediaUtil.getBackgroundURL(AppInstance.getInstance(MainFragment.this.getActivity()).getServer(), (MediaFile) item));
                startBackgroundTimer();
            }

        }
    }

    protected void setDefaultBackground(Drawable background) {
        mDefaultBackground = background;
    }

    protected void setDefaultBackground(int resourceId) {
        mDefaultBackground = getResources().getDrawable(resourceId);
    }

    protected void updateBackground(URI uri) {
        Picasso.with(getActivity())
                .load(uri.toString())
                .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                .centerCrop()
                .error(mDefaultBackground)
                .into(mBackgroundTarget);
    }

    protected void updateBackground(Drawable drawable) {
        BackgroundManager.getInstance(getActivity()).setDrawable(drawable);
    }

    protected void clearBackground() {
        BackgroundManager.getInstance(getActivity()).setDrawable(mDefaultBackground);
    }

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBackgroundURI != null) {
                        updateBackground(mBackgroundURI);
                    }
                }
            });

        }
    }

    private class SettingsPresenter extends Presenter {
        private static final int GRID_ITEM_WIDTH = 300;
        private static final int GRID_ITEM_HEIGHT = 300;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(getResources().getColor(R.color.default_background));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }

}
