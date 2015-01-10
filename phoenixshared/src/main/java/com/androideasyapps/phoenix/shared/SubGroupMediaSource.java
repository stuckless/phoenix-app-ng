package com.androideasyapps.phoenix.shared;

import com.androideasyapps.phoenix.dao.MediaFile;

/**
 * Created by seans on 24/12/14.
 */
public class SubGroupMediaSource extends MediaSource {
    public interface SubGroupResolver {
        public Object getValue(MediaFile item);
    }

    public final SubGroupResolver resolver;

    public SubGroupMediaSource(String title, String query, SubGroupResolver resolver, SubGroupMediaSource subGroupMediaSource) {
        super(title, query, subGroupMediaSource);
        this.resolver=resolver;
    }
}
