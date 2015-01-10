package com.androideasyapps.phoenix.shared;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.androideasyapps.phoenix.dao.H2PersistenceManager;
import com.androideasyapps.phoenix.dao.Server;
import com.squareup.otto.Subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * Created by seans on 22/12/14.
 */
public class StartupActivity extends Activity implements EditServerFragment.OnConfigured {
    public Logger log = LoggerFactory.getLogger(StartupActivity.class);

    TextView message;

    public StartupActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.startup_activity);

        message = ButterKnife.findById(this, R.id.message);

        AppInstance.getInstance(this.getApplication()).initImageLoader();

        AppInstance.getInstance(this.getApplication()).bus().register(this);

        AppInstance.getInstance(this.getApplication()).getDAOObservalbe().subscribe(new Action1<H2PersistenceManager>() {
            @Override
            public void call(H2PersistenceManager h2PersistenceManager) {
                // db is online...
                dbPostInit(h2PersistenceManager);
            }
        });
    }

    protected  void dbPostInit(H2PersistenceManager persistenceManager) {
        log.info("Database is online, Checking if we are configured.");
        message.setText(R.string.msg_checking_servers);
        AppInstance.getInstance(this).getServerObservable().subscribe(new Action1<Server>() {
            @Override
            public void call(Server server) {
                if (server==null) {
                    showServerConfiguration();
                } else {
                    AppInstance.getInstance(StartupActivity.this).setServer(server);
                    doPostServerConfiguration();
                }
            }
        });
    }

    private void showServerConfiguration() {
        log.info("Showing Server Configuration");
        showDialog();
    }

    private void doPostServerConfiguration() {
        message.setText(R.string.msg_checking_media);
        // called when the server is configured, so that we can start the database sync
        log.info("Doing post server configuration...");
        Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                subscriber.onStart();
                try {
                    long val = AppInstance.getInstance(StartupActivity.this).getDAOManager().get().getMediaFileDAO().count();
                    subscriber.onNext(val);
                } catch (Exception e) {
                    subscriber.onNext(0l); // force 0 into the stream so we don't need to check for errors
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                if (aLong>0) {
                    // we have media items, so let's go the media browser
                    navigateToMediaBrowser();
                } else {
                    // start sync process
                    beginMediaSync();
                }
            }
        });

    }

    private void navigateToMediaBrowser() {
        Intent i = null;
        try {
            i = new Intent(this, Class.forName(getResources().getString(R.string.class_MediaBrowser)));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } catch (ClassNotFoundException e) {
            log.error("Developer needs to set RESOURCE: class_MediaBrowser to valid className");
        }
    }

    private void beginMediaSync() {
        message.setText(R.string.msg_syncing_mediafiles);
        SageTVSyncService.startSageTVSync(this, null);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void showDialog() {

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = new EditServerFragment();
        newFragment.setCancelable(false);
        newFragment.show(ft, "dialog");
    }

    @Override
    public void onConfigured(Server server) {
        try {
            dbPostInit(AppInstance.getInstance(StartupActivity.this).getDAOManager().get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        AppInstance.getInstance(this.getApplication()).bus().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onSyncComplete(SyncComplete sync) {
        navigateToMediaBrowser();
    }

    @Subscribe
    public void onSyncFailed(SyncFailed sync) {
        Toast.makeText(this, R.string.msg_sync_failed, Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onSyncItem(SyncItem item) {
        if ("tv".equalsIgnoreCase(item.file.getMediaType())) {
            message.setText(item.file.getTitle() + " - " + item.file.getEpisodeTitle());
        } else {
            message.setText(item.file.getTitle());
        }
    }
}
