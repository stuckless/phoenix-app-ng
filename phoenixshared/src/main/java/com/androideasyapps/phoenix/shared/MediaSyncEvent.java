package com.androideasyapps.phoenix.shared;

import com.androideasyapps.phoenix.dao.MediaFile;

import rx.Observable;

/**
 * Created by seans on 23/12/14.
 */
public class MediaSyncEvent {
    public Observable<MediaFile> getObservable() {
        return observable;
    }

    private final Observable<MediaFile> observable;

    public MediaSyncEvent(Observable<MediaFile> observable) {
        this.observable = observable;
    }
}
