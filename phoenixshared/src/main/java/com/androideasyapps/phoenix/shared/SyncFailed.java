package com.androideasyapps.phoenix.shared;

/**
 * Created by seans on 01/01/15.
 */
public class SyncFailed {
    private final Throwable error;

    public SyncFailed(Throwable throwable) {
        this.error=throwable;
    }
}
