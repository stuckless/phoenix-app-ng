package com.androideasyapps.phoenix.recommend;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.androideasyapps.phoenix.DetailsActivity;
import com.androideasyapps.phoenix.R;
import com.androideasyapps.phoenix.dao.MediaFile;
import com.androideasyapps.phoenix.shared.AppInstance;
import com.androideasyapps.phoenix.util.MediaUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/*
 * This class builds up to MAX_RECOMMMENDATIONS of recommendations and defines what happens
 * when they're clicked from Recommendations section on Home screen
 */
public class UpdateRecommendationsService extends IntentService {
    private static final Logger log = LoggerFactory.getLogger(UpdateRecommendationsService.class);

    public UpdateRecommendationsService() {
        super("RecommendationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final AppInstance app = AppInstance.getInstance(this);
        if (!app.isReady()) {
            log.info("App not ready");
            return;
        }
        log.info("Updating recommendation cards for SAGETV");
        String query = "watched != true order by hasmetadata desc, mediafileid desc limit " + app.preferences().recommendation_limit(5);
        // change the observeOn to be the background thread, but defauilt, it is the main thread
        Observable<Collection<MediaFile>> files = app.getMediaItems(app.getDAOManager(), query).observeOn(Schedulers.io());
        files.subscribe(new Action1<Collection<MediaFile>>() {
            @Override
            public void call(Collection<MediaFile> mediaFiles) {

                if (mediaFiles == null || mediaFiles.size() == 0) {
                    log.warn("No Recommendations");
                    return;
                }
                log.info("Processing Recommendations: " + mediaFiles.size());
                try {
                    RecommendationBuilder builder = new RecommendationBuilder()
                            .setContext(getApplicationContext())
                            .setSmallIcon(R.drawable.sageicon64);

                    int size = mediaFiles.size();
                    int i = 0;
                    for (MediaFile mf : mediaFiles) {
                        log.info("SageTV Recommendation - " + mf.getTitle());

                        String title = mf.getTitle();
                        if (MediaUtil.isTV(mf)) {
                            title = mf.getTitle() + " - " + mf.getEpisodeTitle();
                        }

                        builder.setBackground(MediaUtil.getBackgroundURL(app.getServer(), mf))
                                .setId(i)
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setTitle(mf.getTitle())
                                .setDescription(MediaUtil.getMediaDescription(mf).toString())
                                .setImage(MediaUtil.getPosterURL(app.getServer(), mf))
                                .setIntent(buildPendingIntent(mf))
                                .build();

                        i++;

                    }
                } catch (Throwable e) {
                    log.error("Unable to update recommendation", e);
                }
            }
        });
    }

    private PendingIntent buildPendingIntent(MediaFile movie) {
        Intent detailsIntent = new Intent(this, DetailsActivity.class);
        detailsIntent.putExtra(DetailsActivity.MOVIE, movie);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(DetailsActivity.class);
        stackBuilder.addNextIntent(detailsIntent);
        // Ensure a unique PendingIntents, otherwise all recommendations end up with the same
        // PendingIntent
        detailsIntent.setAction(Long.toString(movie.getId()));

        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        return intent;
    }
}