package com.androideasyapps.phoenix;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.SearchFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemClickedListener;
import android.support.v17.leanback.widget.Row;
import android.text.TextUtils;

import com.androideasyapps.phoenix.dao.MediaFile;
import com.androideasyapps.phoenix.shared.AppInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by seans on 18/12/14.
 */
public class SageTVSearchFragment extends SearchFragment
        implements SearchFragment.SearchResultProvider {

    static final Logger log = LoggerFactory.getLogger(SageTVSearchFragment.class);

    private static final int SEARCH_DELAY_MS = 300;
    private ArrayObjectAdapter mRowsAdapter;
    private Handler mHandler = new Handler();
    private SearchRunnable mDelayedLoad;

    public class SearchRunnable implements Runnable {
        private String searchQuery;

        @Override
        public void run() {
            loadRows(searchQuery);
        }

        public void setSearchQuery(String searchQuery) {
            this.searchQuery = searchQuery;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setSearchResultProvider(this);
        setOnItemClickedListener(getDefaultItemClickedListener());
        mDelayedLoad = new SearchRunnable();
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        return mRowsAdapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        mRowsAdapter.clear();
        if (!TextUtils.isEmpty(newQuery)) {
            mDelayedLoad.setSearchQuery(newQuery);
            mHandler.removeCallbacks(mDelayedLoad);
            mHandler.postDelayed(mDelayedLoad, SEARCH_DELAY_MS);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mRowsAdapter.clear();
        if (!TextUtils.isEmpty(query)) {
            mDelayedLoad.setSearchQuery(query);
            mHandler.removeCallbacks(mDelayedLoad);
            mHandler.postDelayed(mDelayedLoad, SEARCH_DELAY_MS);
        }
        return true;
    }

    private void loadRows(String searchQuery) {
        log.info("Searching {}", searchQuery);
        if (searchQuery != null && searchQuery.trim().length() > 1) {
            String query = "%" + searchQuery + "%";
            Collection<MediaFile> mediafiles = null;
            try {
                mediafiles = AppInstance.getInstance(SageTVSearchFragment.this.getActivity()).getDAOManager().get().getMediaFileDAO().query("lower(title) like ? order by title, season, episode limit 100", query);
                if (mediafiles!=null) {
                    ArrayObjectAdapter moviesRowAdapter = new ArrayObjectAdapter(new CardPresenter());
                    HeaderItem header = new HeaderItem(0, getResources().getString(R.string.search_results_movies), null);
                    mRowsAdapter.add(new ListRow(header, moviesRowAdapter));

                    ArrayObjectAdapter tvRowAdapter = new ArrayObjectAdapter(new CardPresenter());
                    HeaderItem tvHeader = new HeaderItem(1, getResources().getString(R.string.search_results_tv), null);
                    mRowsAdapter.add(new ListRow(tvHeader, tvRowAdapter));

                    for (MediaFile mf: mediafiles) {
                        if ("tv".equalsIgnoreCase(mf.getMediaType())) {
                            tvRowAdapter.add(mf);
                        } else if ("movie".equalsIgnoreCase(mf.getMediaType())) {
                            moviesRowAdapter.add(mf);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Search Failed for " + searchQuery, e);
            }
        }
    }

    protected OnItemClickedListener getDefaultItemClickedListener() {
        return new OnItemClickedListener() {
            @Override
            public void onItemClicked(Object item, Row row) {
                if (item instanceof MediaFile) {
                    MediaFile mf = (MediaFile) item;
                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
                    AppInstance.getInstance(getActivity()).setSelectedMediaFile(mf);
                    startActivity(intent);
                }
            }
        };
    }
}