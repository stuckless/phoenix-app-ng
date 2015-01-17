package com.androideasyapps.phoenix.shared;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.androideasyapps.phoenix.dao.MediaFile;
import com.androideasyapps.phoenix.sync.SageTVSync;
import com.androideasyapps.phoenix.sync.SyncOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SageTVSyncService extends IntentService {
    static final Logger log  = LoggerFactory.getLogger(SageTVSyncService.class);

    private static final String ACTION_SYNC_SAGETV = "com.androideasyapps.phoenix.shared.action.SYNC_SAGETV";
    private static final String PARAM_OPTIONS = "options";

    Observable<MediaFile> mediaSyncObserver = null;
    SageTVSync sync = null;
    private Subscription mediaSubscription = null;

    public static void startSageTVSync(Context context, SyncOptions options) {
        Intent intent = new Intent(context, SageTVSyncService.class);
        intent.setAction(ACTION_SYNC_SAGETV);
        if (options!=null) {
            intent.putExtra(PARAM_OPTIONS, options);
        }
        context.startService(intent);
    }

    public SageTVSyncService() {
        super("SageTVSyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!AppInstance.getInstance(this).isReady()) {
            log.warn("SageTV Sync Aborted, since SageTV is not configured.");
            return;
        }

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SYNC_SAGETV.equals(action)) {

                handleActionSyncSageTV((SyncOptions)intent.getSerializableExtra(PARAM_OPTIONS));
            }
        }
    }

    private void handleActionSyncSageTV(SyncOptions options) {
        if (sync==null) {
            sync = new SageTVSync();
        }

        if (mediaSyncObserver==null) {
            final SyncItem item = new SyncItem();
            mediaSyncObserver = sync.syncCollection(AppInstance.getInstance(this).getSageTVService(), AppInstance.getInstance(this).getDAOManager(), options);
            AppInstance.getInstance(this).bus().postOnUI(new MediaSyncEvent(mediaSyncObserver));
            mediaSubscription = mediaSyncObserver.subscribe(new Action1<MediaFile>() {
                @Override
                public void call(MediaFile mediaFile) {
                    item.file=mediaFile;
                    AppInstance.getInstance(SageTVSyncService.this).bus().postOnUI(item);
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                   log.error("Sync Failed", throwable);
                    AppInstance.getInstance(SageTVSyncService.this).bus().postOnUI(new SyncFailed(throwable));
                }
            }, new Action0() {
                @Override
                public void call() {
                    mediaSubscription.unsubscribe();
                    mediaSyncObserver=null;
                    mediaSubscription=null;
                    AppInstance.getInstance(SageTVSyncService.this).bus().postOnUI(new SyncComplete());

                }
            });
        } else {
            log.warn("SageTV Sync is already running.  Exiting.");
        }
    }
}
