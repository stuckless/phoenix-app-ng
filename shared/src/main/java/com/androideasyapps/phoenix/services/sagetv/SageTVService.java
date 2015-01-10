package com.androideasyapps.phoenix.services.sagetv;

import com.androideasyapps.phoenix.services.sagetv.model.MediaFileResults;
import com.androideasyapps.phoenix.services.sagetv.model.Result;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by seans on 20/12/14.
 */
public interface SageTVService {
    @GET("/api?c=GetMediaFiles&encoder=json")
    public Observable<MediaFileResults> getMediaFilesObservable(@Query("1") String mediaFileTypes, @Query("start") int start, @Query("size") int pageSize);

    @GET("/api?c=GetMediaFiles&encoder=json")
    public MediaFileResults getMediaFiles(@Query("1") String mediaFileTypes, @Query("start") int start, @Query("size") int pageSize);

    @GET("/api?c=GetDatabaseLastModifiedTime&encoder=json")
    public Observable<Result<Number>> getDatabaseLastModified(@Query("1") String mediaFileTypes);

    @GET("/api?c=SetWatched&encoder=json")
    public void setWatched(@Query("1") long mediaFileId, Callback<Void> callback);

    @GET("/api?c=ClearWatched&encoder=json")
    public void clearWatched(@Query("1") long mediaFileId, Callback<Void> callback);
}
