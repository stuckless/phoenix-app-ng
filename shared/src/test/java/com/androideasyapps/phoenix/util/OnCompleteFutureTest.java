package com.androideasyapps.phoenix.util;

import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class OnCompleteFutureTest {
    @Test
    public void testFutureGet() throws ExecutionException, InterruptedException {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        final OnCompleteFuture<Long> future = new OnCompleteFuture<>();
        service.schedule(new Runnable() {
            @Override
            public void run() {
                future.setValue(1000l);
            }
        }, 1000, TimeUnit.MILLISECONDS);
        assertEquals(Long.valueOf(1000), future.get());
    }

    @Test
    public void testFutureGetTimeout() throws ExecutionException, InterruptedException {
        final OnCompleteFuture<Long> future = new OnCompleteFuture<>();
        try {
            future.get(1000, TimeUnit.MILLISECONDS);
            fail("Did Not Timeout");
        } catch (TimeoutException e) {
            System.out.println("TimedOut: Good.");
        }
    }

}