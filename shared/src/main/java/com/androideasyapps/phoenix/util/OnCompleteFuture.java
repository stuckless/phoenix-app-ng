package com.androideasyapps.phoenix.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by seans on 23/12/14.
 */
public class OnCompleteFuture<V> implements Future<V> {
    public interface OnComplete<V> {
        public void onComplete(V value);
    }

    V value = null;

    public void setOnComplete(OnComplete<V> onComplete) {
        this.onComplete = onComplete;
        if (isDone()) {
            this.onComplete.onComplete(this.value);
        }
    }

    OnComplete<V> onComplete;

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
        return value!=null;
    }

    public void setValue(V value) {
        this.value=value;
        if (onComplete!=null) {
            onComplete.onComplete(value);
        }
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        try {
            return get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return null;
        }
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!isDone()) {
            long lastTime=System.currentTimeMillis();
            timeout = unit.toMillis(timeout);

            while (!isDone()) {
                if ((System.currentTimeMillis()-lastTime)>timeout) break;
                Thread.sleep(50);
            }
            if (!isDone()) {
                throw new TimeoutException();
            }
        }
        return value;
    }

    public void reset() {
        onComplete=null;
        value=null;
    }
}
