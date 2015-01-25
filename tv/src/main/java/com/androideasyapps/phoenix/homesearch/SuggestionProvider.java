package com.androideasyapps.phoenix.homesearch;
        import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.androideasyapps.phoenix.dao.MediaFile;
import com.androideasyapps.phoenix.shared.AppInstance;
import com.androideasyapps.phoenix.util.MediaUtil;

import java.util.Collection;

/**
 * Provides access to the video database.
 */
public class SuggestionProvider extends ContentProvider {
    private static String TAG = "SageTVContentProvider";
    public static String AUTHORITY = "com.androideasyapps.phoenix";

    // MIME types used for searching words or looking up a single definition
    public static final String WORDS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd.com.androideasyapps.phoenix.SageTVContentProvider";
    public static final String DEFINITION_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.com.androideasyapps.phoenix.SageTVContentProvider";
    // UriMatcher stuff
    private static final int SEARCH_WORDS = 0;
    private static final int SEARCH_SUGGEST = 2;
    private static final int REFRESH_SHORTCUT = 3;
    private static final UriMatcher URI_MATCHER = buildUriMatcher();


    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get suggestions...
        Log.d(TAG, "suggest_uri_path_query: " + SearchManager.SUGGEST_URI_PATH_QUERY);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");
        return true;
    }

    /**
     * Handles all the video searches and suggestion queries from the Search Manager.
     * When requesting a specific word, the uri alone is required.
     * When searching all of the video for matches, the selectionArgs argument must carry
     * the search query as the first element.
     * All other arguments are ignored.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        switch (URI_MATCHER.match(uri)) {
            case SEARCH_SUGGEST:
                Log.d(TAG, "search suggest: " + selectionArgs[0] + " URI: " + uri);
                if (selectionArgs == null) {
                    throw new IllegalArgumentException(
                            "selectionArgs must be provided for the Uri: " + uri);
                }
                return getSuggestions(selectionArgs[0]);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    private Cursor getSuggestions(String query) {
        query = "%" + query.toLowerCase() + "%";

        MatrixCursor cursor = new MatrixCursor(new String[] {BaseColumns._ID, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_CONTENT_TYPE, SearchManager.SUGGEST_COLUMN_PRODUCTION_YEAR, SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE});
        Collection<MediaFile> mediafiles = null;
        try {
            mediafiles = AppInstance.getInstance(this.getContext()).getDAOManager().get().getMediaFileDAO().query("lower(title) like ? order by title, season, episode limit 100", query);
            if (mediafiles!=null) {
                for (MediaFile mf: mediafiles) {
                    cursor.addRow(new Object[] {
                            mf.getMediaFileId(),
                            mf.getMediaFileId(),
                            mf.getMediaFileId(),
                            mf.getTitle(),
                            MediaUtil.getMediaDescription(mf),
                            "video/*",
                            mf.getYear(),
                            Uri.parse(MediaUtil.getPosterURL(AppInstance.getInstance(this.getContext()).getServer(), mf, MediaUtil.DEFAULT_POSTER_WIDTH))
                    });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Search Failed for " + query, e);
        }
        return cursor;
    }

    /**
     * This method is required in order to query the supported types.
     * It's also useful in our own query() method to determine the type of Uri received.
     */
    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case SEARCH_WORDS:
                return WORDS_MIME_TYPE;
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            case REFRESH_SHORTCUT:
                return SearchManager.SHORTCUT_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    // Other required implementations...

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}