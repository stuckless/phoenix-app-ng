package com.androideasyapps.phoenix.services;

import com.androideasyapps.phoenix.dao.H2PersistenceManager;
import com.androideasyapps.phoenix.services.sagetv.SageTVRequestInterceptor;
import com.androideasyapps.phoenix.services.sagetv.SageTVService;
import com.androideasyapps.phoenix.services.sagetv.model.MediaFile;
import com.androideasyapps.phoenix.services.sagetv.model.MediaFileResults;
import com.androideasyapps.phoenix.sync.SageTVSync;
import com.androideasyapps.phoenix.sync.SyncOptions;
import com.androideasyapps.phoenix.util.OracleBase64Encoder;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit.RestAdapter;
import rx.Observable;
import rx.functions.Action1;

public class SageTVServiceTest {

    @Test
    public void testGetMediaFiles() throws Exception {
        SageTVService service = newService();

        Observable<MediaFileResults> results = service.getMediaFilesObservable("V", 0, 10);
        results.subscribe(new Action1<MediaFileResults>() {
            @Override
            public void call(MediaFileResults mediaFileResults) {
                List<MediaFile> files = mediaFileResults.getResult();
                for (MediaFile mf: files) {
                    System.out.println("MediaFile: " + mf.MediaFileID + "; Title: " + mf.MediaFileMetadataProperties.MediaTitle);
                }
            }
        });


        SageTVSync sync = new SageTVSync();
        sync.syncCollection(service, newPersistenceManager(), new SyncOptions()).subscribe(new Action1<com.androideasyapps.phoenix.dao.MediaFile>() {
            @Override
            public void call(com.androideasyapps.phoenix.dao.MediaFile mediaFile) {
                System.out.println(".");
            }
        });

        Thread.currentThread().sleep(100000);

    }

    SageTVService newService() {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("http://192.168.1.10:8080/sagex")
                .setLogLevel(RestAdapter.LogLevel.NONE)
                .setRequestInterceptor(new SageTVRequestInterceptor(new SageTVRequestInterceptor.Credentials("sage", "frey"), new OracleBase64Encoder()))
                .build();

        SageTVService service = restAdapter.create(SageTVService.class);
        return service;
    }

    Future<H2PersistenceManager> newPersistenceManager() {
        String jdbcUrl = "jdbc:h2:" + "/tmp/testsage.db" + ";TRACE_LEVEL_FILE=0;CREATE=true";
        final H2PersistenceManager daoManager = new H2PersistenceManager(jdbcUrl);
        try {
            daoManager.createTables();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Future<H2PersistenceManager>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public H2PersistenceManager get() throws InterruptedException, ExecutionException {
                return daoManager;
            }

            @Override
            public H2PersistenceManager get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return daoManager;
            }
        };

    }
}