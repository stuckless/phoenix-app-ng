package com.androideasyapps.phoenix.shared;

/**
 * Created by seans on 24/12/14.
 */
public class MediaSource {
    public MediaSource(String title, String query) {
        this(title,query,null);
    }

    public MediaSource(String title, String query, SubGroupMediaSource subGroupMediaSource) {
        this.title=title;
        this.query=query;
        this.subGroupMediaSource=subGroupMediaSource;
    }

    public String title;
    public String query;
    public SubGroupMediaSource subGroupMediaSource;
}
