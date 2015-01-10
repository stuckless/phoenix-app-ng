package com.androideasyapps.phoenix.services.phoenix;

import com.androideasyapps.phoenix.dao.MediaFile;
import com.androideasyapps.phoenix.services.phoenix.model.ViewItem;

/**
 * Created by seans on 23/12/14.
 */
public class ViewItemMediaFileAdapter extends MediaFile {
    public ViewItemMediaFileAdapter(ViewItem item) {
        if (item.mediatype!=null) setMediaType(item.mediatype.toLowerCase());
        setTitle(item.title);
        setDescription(item.description);
        setWatched(item.watched);
        setOriginalAirDate(item.airingTime);
        setEpisodeTitle(item.episodeName);
        setSeason(item.season);
        setEpisode(item.episode);
        setDuration(item.runtime);
    }
}
