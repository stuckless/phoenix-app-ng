package com.androideasyapps.phoenix.shared;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.androideasyapps.phoenix.dao.H2PersistenceManager;
import com.androideasyapps.phoenix.dao.MediaFile;
import com.androideasyapps.phoenix.dao.Server;
import com.androideasyapps.phoenix.services.phoenix.PhoenixService;
import com.androideasyapps.phoenix.services.sagetv.SageTVRequestInterceptor;
import com.androideasyapps.phoenix.services.sagetv.SageTVService;
import com.androideasyapps.phoenix.shared.prefs.PhoenixPreferences;
import com.androideasyapps.phoenix.shared.prefs.PreferenceManager;
import com.androideasyapps.phoenix.util.OnCompleteFuture;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.squareup.otto.ThreadEnforcer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import retrofit.RestAdapter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by seans on 21/12/14.
 */
public class AppInstance {
    private static final Logger log = LoggerFactory.getLogger(AppInstance.class);

    public static AppInstance getInstance(Context ctx) {
        if (instance == null) {
            instance = new AppInstance(ctx);
        }
        return instance;
    }

    private static AppInstance instance;

    private SageTVService sageTVService;
    private PhoenixService phoenixService;

    private AndroidBus uiBus = new AndroidBus(ThreadEnforcer.ANY);

    private Server server;

    private MediaFile selectedMediaFile;

    private PhoenixPreferences preferences;

    final ExecutorService executor;
    final Future<H2PersistenceManager> futureDAOManager;
    final OnCompleteFuture<Observable<MediaFile>> syncFuture = new OnCompleteFuture<>();
    private SageTVRequestInterceptor requestInterceptor;

    private final Context context;

    private Map<String, MediaSource> mediaSources = new LinkedHashMap<>();

    public SageTVRequestInterceptor getRequestInterceptor() {
        return requestInterceptor;
    }

    public AppInstance(Context ctx) {
        this.context = ctx;

        preferences = PreferenceManager.getPreferences(PhoenixPreferences.class, android.preference.PreferenceManager.getDefaultSharedPreferences(ctx));

        executor = Executors.newCachedThreadPool();
        futureDAOManager = executor.submit(new Callable<H2PersistenceManager>() {
            @Override
            public H2PersistenceManager call() throws Exception {
                final String dbname = "sagetv.db";
                File cache = new File(context.getFilesDir(), dbname);
                log.info("Starting Database: {}", cache);
                //if (cache.exists()) cache.delete();
                String jdbcUrl = "jdbc:h2:" + cache.getAbsolutePath() + ";TRACE_LEVEL_FILE=0;CREATE=true";
                H2PersistenceManager daoManager = new H2PersistenceManager(jdbcUrl);
                try {
                    daoManager.createTables();
                } catch (Exception e) {
                    log.error("failed -- will need to rebuild :(", e);
                    log.warn("Deleting: {}", cache);

                    cache.delete();
                    cache.getParentFile().listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            if (pathname.getName().startsWith(dbname)) {
                                log.warn("Deleting: {}", pathname);
                                pathname.delete();
                            }
                            return false;
                        }
                    });

                    daoManager = new H2PersistenceManager(jdbcUrl);
                    try {
                        daoManager.createTables();
                    } catch (Throwable t) {
                        log.error("Giving Up... We can't re-create database :(", t);
                    }
                }
                log.info("Database Online: " + cache);
                return daoManager;
            }
        });

        MediaSource recentMoviesUnwatched = new MediaSource("Recent Movies (Unwatched)",
                "watched != true and mediatype='movie' and hasmetadata = true order by mediafileid desc limit " + preferences().recent_limit(30));

        // MediaSource recentTVUnwatched = new MediaSource("Recent TV (Unwatched)", "watched != true and mediatype='tv' order by originalairdate desc");

        MediaSource recentTVUnwatchedGrouped = new MediaSource("Recent TV (Unwatched/Grouped)",
                "select distinct title, mediatype, max(originalairdate) as originalairdate, max(season), count(mediatype) as parts from mediafile where watched != true and mediatype='tv' group by title order by originalairdate desc",
                new SubGroupMediaSource("%s",
                        "watched != true and mediatype='tv' and title=? order by originalairdate desc",
                        new SubGroupMediaSource.SubGroupResolver() {
                            @Override
                            public Object getValue(MediaFile item) {
                                return item.getTitle();
                            }
                        }, null
                )
        );

        MediaSource recentNoMetadata = new MediaSource("Recent No Metadata",
                "hasmetadata != true and watched != true order by mediafileid desc limit " + preferences().recent_limit(30));


        mediaSources.put(recentMoviesUnwatched.title, recentMoviesUnwatched);
        // mediaSources.put(recentTVUnwatched.title, recentTVUnwatched);
        mediaSources.put(recentTVUnwatchedGrouped.title, recentTVUnwatchedGrouped);
        mediaSources.put(recentNoMetadata.title, recentNoMetadata);

        initImageLoader();

        // start the scheduler for syncing
        scheduleSyncUpdate(context, false);
        scheduleRecommendationUpdate(context, false);
    }

    public PhoenixService getPhoenixService() {
        if (phoenixService==null) {
            log.error("PhoenixService is not Yet Configured", new Throwable("PhoenixService is not Yet Configured"));
        }
        return phoenixService;
    }

    public SageTVService getSageTVService() {
        if (sageTVService==null) {
            log.error("SageTVService is not Yet Configured", new Throwable("SageTVService is not Yet Configured"));
        }
        return sageTVService;
    }

    public Future<H2PersistenceManager> getDAOManager() {
        return futureDAOManager;
    }

    public AndroidBus bus() {
        return uiBus;
    }

    protected void initializeServices(Server server) {
        requestInterceptor =new SageTVRequestInterceptor(new SageTVRequestInterceptor.Credentials(server.getUsername(), "frey"), new AndroidBase64Encoder());

        RestAdapter restAdapter = getRestAdapter(server);

        sageTVService = restAdapter.create(SageTVService.class);
        phoenixService = restAdapter.create(PhoenixService.class);
    }

    /**
     * Used to get the Persistence Manager as an observable, so that you can be notified when it's online.
     *
     * @return
     */
    public Observable<H2PersistenceManager> getDAOObservalbe() {
        return Observable.create(new Observable.OnSubscribe<H2PersistenceManager>() {
            @Override
            public void call(Subscriber<? super H2PersistenceManager> subscriber) {
                subscriber.onStart();
                try {
                    subscriber.onNext(getDAOManager().get());
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    log.error("get Observable for Persistence Manager failed :(", e);
                    subscriber.onError(e);
                }
            }
        });
    }

    Server getFirstConfiguredServer() throws Exception {
        // find the first server, may be null.
        Server server = getDAOManager().get().getServerDAO().queryFirst("isdefault = true");
        if (server == null) {
            server = getDAOManager().get().getServerDAO().queryFirst("serverid is not null");
        }
        return server;
    }

    public Observable<Server> getServerObservable() {
        return Observable.create(new Observable.OnSubscribe<Server>() {
            @Override
            public void call(Subscriber<? super Server> subscriber) {
                subscriber.onStart();
                try {
                    // find the first server, may be null.
                    Server server = getFirstConfiguredServer();
                    subscriber.onNext(server);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * Gets the curent Server... This could be null if there isn't a configured server.
     *
     * @return
     */
    public Server getServer() {
        if (server == null) {
            log.error("You current server is NOT current set.  This is a programming error.", new Throwable("SHOULD CONFIGURE SERVER FIRST!!!"));
            try {
                server = getFirstConfiguredServer();
            } catch (Exception e) {
                log.error("Failed to configure server", e);
            }
        }
        if (server==null) {
            throw new RuntimeException("SERVER is not configured and yet, we are trying to access it.");
        }
        return server;
    }

    RestAdapter getRestAdapter(Server server) {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("http://" + server.getHost() + ":" + server.getPort() + "/sagex")
                .setLogLevel(RestAdapter.LogLevel.NONE)
                .setRequestInterceptor(new SageTVRequestInterceptor(new SageTVRequestInterceptor.Credentials(server.getUsername(), server.getPassword()), new AndroidBase64Encoder()))
                .build();
        return restAdapter;
    }

    public SageTVService getSageTVService(Server server) {
        RestAdapter restAdapter = getRestAdapter(server);

        return restAdapter.create(SageTVService.class);
    }

    public void setServer(Server server) {
        this.server = server;
        initializeServices(server);
    }

    public MediaFile getSelectedMediaFile() {
        return selectedMediaFile;
    }

    public void setSelectedMediaFile(MediaFile selectedMediaFile) {
        this.selectedMediaFile = selectedMediaFile;
    }

    public void initImageLoader() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                //.showImageOnLoading(R.drawable.ic_stub) // resource or drawable
                //.showImageForEmptyUri(R.drawable.ic_empty) // resource or drawable
                //.showImageOnFail(R.drawable.ic_error) // resource or drawable
                //.resetViewBeforeLoading(false)  // default
                //.delayBeforeLoading(1000)
                .cacheInMemory(true) // default
                .cacheOnDisk(true) // default
                //.displayer(new FadeInBitmapDisplayer(300,true,true,true))

                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(550 * 1024 * 1024) // 550 Mb
                .memoryCacheSize(2 * 1024 * 1024)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                //.writeDebugLogs() // Remove for release app

                .defaultDisplayImageOptions(options)
                .build();


        // Initialize ImageLoader with configuration.
        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().destroy();
        }
        ImageLoader.getInstance().init(config);
    }


    public Map<String,MediaSource> getMediaSources() {
        return mediaSources;
    }

    public Observable<Collection<MediaFile>> getMediaItems(final Future<H2PersistenceManager> dao, final String query) {
        return Observable.create(new Observable.OnSubscribe<Collection<MediaFile>>() {
            @Override
            public void call(Subscriber<? super Collection<MediaFile>> subscriber) {
                subscriber.onStart();
                try {
                    subscriber.onNext(dao.get().getMediaFileDAO().query(query));
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Collection<MediaFile>> getMediaItems(final Future<H2PersistenceManager> dao, final MediaSource source, final MediaFile sourceFile) {
        return Observable.create(new Observable.OnSubscribe<Collection<MediaFile>>() {
            @Override
            public void call(Subscriber<? super Collection<MediaFile>> subscriber) {
                subscriber.onStart();
                try {
                    if (source instanceof SubGroupMediaSource) {
                        subscriber.onNext(dao.get().getMediaFileDAO().query(source.query, new Object[]{((SubGroupMediaSource)source).resolver.getValue(sourceFile)}));
                    } else {
                        subscriber.onNext(dao.get().getMediaFileDAO().query(source.query));
                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }


    public PhoenixPreferences preferences() {
        return preferences;
    }

    public boolean isReady() {
        try {
            // getServer() will thrown an exception if we can't get the server.
            Server server = getServer();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public void scheduleSyncUpdate(Context context, boolean update) {
        Calendar now = Calendar.getInstance();
        Calendar later = Calendar.getInstance();
        later.add(Calendar.DAY_OF_YEAR, 1);
        later.set(Calendar.HOUR_OF_DAY, 0);
        later.set(Calendar.MINUTE, 0);
        later.set(Calendar.SECOND, 0);
        later.set(Calendar.MILLISECOND, 0);

        log.info("SageTV Scheduling Sync update to happen every day starting at " + later.getTime());

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent syncIntent = new Intent(context, SageTVSyncService.class);

        if (!update && checkAlarmIfExists(alarmManager, context, syncIntent)) {
            log.info("Alarm already configured for {}", syncIntent);
            return;
        }

        cancelAlarmIfExists(alarmManager, context, syncIntent);

        PendingIntent alarmIntent = PendingIntent.getService(context, 0, syncIntent, 0);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                later.getTimeInMillis() - now.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                alarmIntent);
    }

    public void scheduleRecommendationUpdate(Context context, boolean update) {
        log.info("SageTV Scheduling recommendations update");

        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent recommendationIntent = new Intent(context, Class.forName("com.androideasyapps.phoenix.recommend.UpdateRecommendationsService"));

            if (!update && checkAlarmIfExists(alarmManager, context, recommendationIntent)) {
                log.info("Alarm already configured for {}", recommendationIntent);
                return;
            }

            cancelAlarmIfExists(alarmManager, context, recommendationIntent);

            PendingIntent alarmIntent = PendingIntent.getService(context, 0, recommendationIntent, 0);

            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    5000,
                    AlarmManager.INTERVAL_HALF_HOUR,
                    alarmIntent);
        } catch (Throwable t) {
            log.error("Failed to install recommendation alarm", t);
        }
    }

    void cancelAlarmIfExists(AlarmManager manager, Context mContext, Intent intent) {
        try {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            if (pendingIntent != null) {
                log.info("Cancelling Alarm for {}", intent);
                manager.cancel(pendingIntent);
            } else {
                log.info("No Pending Alarm for {}", intent);
            }
        } catch (Exception e) {
            log.warn("Error during cancelling Alarm for {}", intent);
        }
    }

    boolean checkAlarmIfExists(AlarmManager manager, Context mContext, Intent intent) {
        try {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            return pendingIntent != null;
        } catch (Exception e) {
            log.warn("Error during checking Alarm for {}", intent);
        }
        return false;
    }

}
