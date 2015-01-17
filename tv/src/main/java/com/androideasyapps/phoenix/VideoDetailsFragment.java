package com.androideasyapps.phoenix;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.androideasyapps.phoenix.dao.MediaFile;
import com.androideasyapps.phoenix.dao.Server;
import com.androideasyapps.phoenix.shared.AppInstance;
import com.androideasyapps.phoenix.shared.MediaSource;
import com.androideasyapps.phoenix.util.MediaUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.URI;
import java.util.Collection;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.android.observables.AndroidObservable;
import rx.functions.Action1;

public class VideoDetailsFragment extends DetailsFragment {
    private static final String TAG = "VideoDetailsFragment";

    private static final int ACTION_WATCH = 1;
    private static final long ACTION_WATCH_EXT = 2;
    private static final long ACTION_SET_WATCHED = 3;

    private static final int DETAIL_THUMB_WIDTH = 150;
    private static final int DETAIL_THUMB_HEIGHT = 200;

    private MediaFile mSelectedMovie;

    private Drawable mDefaultBackground;
    private Target mBackgroundTarget;
    private DisplayMetrics mMetrics;
    private DetailsOverviewRowPresenter mDorPresenter;
    private DetailRowBuilderTask mDetailRowBuilderTask;

    private Server server;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);

        server = AppInstance.getInstance(this.getActivity()).getServer();

        mDorPresenter =
                new DetailsOverviewRowPresenter(new DetailsDescriptionPresenter());

        BackgroundManager backgroundManager = BackgroundManager.getInstance(getActivity());
        backgroundManager.attach(getActivity().getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget(backgroundManager);

        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);

        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);

        mSelectedMovie = AppInstance.getInstance(this.getActivity()).getSelectedMediaFile();
        mDetailRowBuilderTask = (DetailRowBuilderTask) new DetailRowBuilderTask().execute(mSelectedMovie);
        mDorPresenter.setSharedElementEnterTransition(getActivity(),
                DetailsActivity.SHARED_ELEMENT_NAME);


        updateBackground(URI.create(MediaUtil.getBackgroundURL(AppInstance.getInstance(this.getActivity()).getServer(), mSelectedMovie)));
        setOnItemViewClickedListener(new ItemViewClickedListener());

        Log.i(TAG, "onCreate DetailsFragment Complete");
    }

    @Override
    public void onStop() {
        mDetailRowBuilderTask.cancel(true);
        super.onStop();
    }

    Action watchedAction = new Action(ACTION_SET_WATCHED, "");

    void updateWatchedUI(boolean refresh) {
        if (mSelectedMovie.getWatched()) {
            watchedAction.setLabel1(getResources().getString(R.string.clear_watched));
        } else {
            watchedAction.setLabel1(getResources().getString(R.string.set_watched));
        }
        if (refresh && adapter != null) {
            adapter.notifyArrayItemRangeChanged(0, adapter.size());
        }
    }

    ArrayObjectAdapter adapter = null;

    class DetailRowBuilderTask extends AsyncTask<MediaFile, Integer, DetailsOverviewRow> {
        @Override
        protected DetailsOverviewRow doInBackground(MediaFile... movies) {
            mSelectedMovie = movies[0];

            final DetailsOverviewRow row = new DetailsOverviewRow(mSelectedMovie);
            try {
                ImageLoader.getInstance().loadImage(MediaUtil.getPosterURL(server, mSelectedMovie),
                        new ImageSize(Utils.convertDpToPixel(getActivity(), DETAIL_THUMB_WIDTH),
                                Utils.convertDpToPixel(getActivity(), DETAIL_THUMB_HEIGHT)),
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap bitmap) {
                                System.out.println("Poster is loaded..");
                                row.setImageBitmap(getActivity(), bitmap);
                            }
                        });

//                Bitmap poster = Picasso.with(getActivity())
//                        .load(MediaUtil.getPosterURL(server, mSelectedMovie))
//                                .resize(Utils.convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_WIDTH),
//                                        Utils.convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_HEIGHT))
//                                .centerCrop()
//                                .get();
//                row.setImageBitmap(getActivity(), poster);
            } catch (Exception e) {
            }

            if (mSelectedMovie.getMediaFileId() > 0) {
                row.addAction(new Action(ACTION_WATCH, getResources().getString(
                        R.string.watch)));
                row.addAction(new Action(ACTION_WATCH_EXT, getResources().getString(R.string.watch_ext)));
                updateWatchedUI(false);
                row.addAction(watchedAction);
            }

            return row;
        }

        private Callback<Void> VOIDCallback = new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                System.out.println("callback complete ok");
            }

            @Override
            public void failure(RetrofitError error) {
                System.out.println("Callback Failed, " + error);
            }
        };

        @Override
        protected void onPostExecute(DetailsOverviewRow detailRow) {
            ClassPresenterSelector ps = new ClassPresenterSelector();
            // set detail background and style
            mDorPresenter.setBackgroundColor(getResources().getColor(R.color.detail_background));
            mDorPresenter.setStyleLarge(true);
            mDorPresenter.setOnActionClickedListener(new OnActionClickedListener() {
                @Override
                public void onActionClicked(Action action) {
                    if (action.getId() == ACTION_WATCH) {
                        Intent intent = new Intent(getActivity(), PlaybackOverlayActivity.class);
                        intent.putExtra(getResources().getString(R.string.movie), mSelectedMovie);
                        intent.putExtra(getResources().getString(R.string.should_start), true);
                        startActivity(intent);
                    } else if (action.getId() == ACTION_WATCH_EXT) {
                        try {
                            Uri uri = Uri.parse(MediaUtil.getVideoURLWithEmbeddedAuth(AppInstance.getInstance(getActivity()).getServer(), mSelectedMovie));
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            intent.setDataAndType(uri, "video/*");
                            startActivity(intent);
                        } catch (Throwable t) {
                            t.printStackTrace();
                            Toast.makeText(getActivity(), "No External Players", Toast.LENGTH_SHORT).show();
                        }
                    } else if (action.getId() == ACTION_SET_WATCHED) {
                        mSelectedMovie.setWatched(!mSelectedMovie.getWatched());
                        updateWatchedUI(true);
                        try {
                            if (mSelectedMovie.getWatched()) {
                                AppInstance.getInstance(getActivity()).getSageTVService().setWatched(mSelectedMovie.getMediaFileId(), VOIDCallback);
                            } else {
                                AppInstance.getInstance(getActivity()).getSageTVService().clearWatched(mSelectedMovie.getMediaFileId(), VOIDCallback);
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                        try {
                            AppInstance.getInstance(getActivity()).getDAOManager().get().getMediaFileDAO().save(mSelectedMovie);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            ps.addClassPresenter(DetailsOverviewRow.class, mDorPresenter);
            ps.addClassPresenter(ListRow.class,
                    new ListRowPresenter());

            adapter = new ArrayObjectAdapter(ps);
            adapter.add(detailRow);

            if (!TextUtils.isEmpty(mSelectedMovie.getUserdata())) {
                MediaSource ms = AppInstance.getInstance(getActivity()).getMediaSources().get(mSelectedMovie.getUserdata());
                if (ms != null) {
                    ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
                    populateSource(listRowAdapter, ms.subGroupMediaSource);
                    HeaderItem header = new HeaderItem(0, "Episodes", null);
                    adapter.add(new ListRow(header, listRowAdapter));
                }
            }

            if (MediaUtil.isMovie(mSelectedMovie)) {
                populateRelatedGenres(adapter, mSelectedMovie);
            }

            setAdapter(adapter);
        }

    }

    private void populateRelatedGenres(ArrayObjectAdapter adapter, MediaFile file) {
        String genre = file.getGenre();
        if (genre != null) {
            String parts[] = genre.split("\\s*/\\s*");
            addAndPopulateGenreView(adapter, file, parts[0]);
            if (parts.length > 1) {
                addAndPopulateGenreView(adapter, file, parts[1]);
            }
        }
    }

    private void addAndPopulateGenreView(ArrayObjectAdapter adapter, MediaFile file, String genre) {
        long st = System.currentTimeMillis();
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        MediaSource ms = new MediaSource("Other Movies with " + genre + " Genre", "mediatype='movie' and genre like '%" + genre + "%' order by year desc, mediafileid desc limit 10");
        populateSource(listRowAdapter, ms);
        HeaderItem header = new HeaderItem(0, ms.title, null);
        adapter.add(new ListRow(header, listRowAdapter));
        long et = System.currentTimeMillis();
        System.out.println("Created Genres in " + (et - st) + "ms");
    }

    private void populateSource(ArrayObjectAdapter listRowAdapter, MediaSource source) {
        if (source.subGroupMediaSource == null) {
            populateListRowAdapter(listRowAdapter, source);
        }
    }

    private void populateListRowAdapter(final ArrayObjectAdapter listRowAdapter, MediaSource source) {
        AndroidObservable.bindFragment(this, AppInstance.getInstance(getActivity()).getMediaItems(AppInstance.getInstance(this.getActivity()).getDAOManager(), source, mSelectedMovie))
                .subscribe(new Action1<Collection<MediaFile>>() {
                    @Override
                    public void call(Collection<MediaFile> mediaFiles) {
                        if (mediaFiles != null) {
                            for (MediaFile mf : mediaFiles) {
                                listRowAdapter.add(mf);
                            }
                        }
                    }
                });
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof MediaFile) {
                MediaFile movie = (MediaFile) item;
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                //intent.putExtra(DetailsActivity.MOVIE, movie);
                AppInstance.getInstance(VideoDetailsFragment.this.getActivity()).setSelectedMediaFile(movie);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);
            }
        }
    }

    protected void updateBackground(URI uri) {
        Picasso.with(getActivity())
                .load(uri.toString())
                .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                .error(mDefaultBackground)
                .into(mBackgroundTarget);
    }

}
