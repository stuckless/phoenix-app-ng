package com.androideasyapps.phoenix.shared;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by seans on 23/12/14.
 */
public class AndroidBus extends Bus {
    private final Handler mainThread = new Handler(Looper.getMainLooper());

    public AndroidBus(ThreadEnforcer te) {
        super(te);
    }

    public void postOnUI(final Object event) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                post(event);
            }
        });
    }
}