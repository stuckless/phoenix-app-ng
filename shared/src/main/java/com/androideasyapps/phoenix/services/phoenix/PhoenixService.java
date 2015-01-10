package com.androideasyapps.phoenix.services.phoenix;

import com.androideasyapps.phoenix.services.phoenix.model.ViewReply;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by seans on 20/12/14.
 */
public interface PhoenixService {
    @GET("/phoenix?c=phoenix.umb.CreateView&1=phoenix.view.primary.scheduledrecordings")
    public Observable<ViewReply> getScheduledRecordings();
}
