package com.androideasyapps.phoenix.sync;

import java.io.Serializable;

/**
 * Created by seans on 24/12/14.
 */
public class SyncOptions implements Serializable {
    private boolean eraseCollection = false;
    public SyncOptions() {

    }

    public boolean isEraseCollection() {
        return eraseCollection;
    }

    public void setEraseCollection(boolean eraseCollection) {
        this.eraseCollection = eraseCollection;
    }


}
