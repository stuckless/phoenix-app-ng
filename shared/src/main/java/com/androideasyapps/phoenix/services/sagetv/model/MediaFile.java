package com.androideasyapps.phoenix.services.sagetv.model;

/**
 * Created by seans on 20/12/14.
 */
public class MediaFile {
    public MediaFileAiring Airing;
    public MediaFileMetadata MediaFileMetadataProperties;

    public long MediaFileID;
    public String MediaFileRelativePath;
    public String[] SegmentFiles;
    public long FileDuration;
    public long FileStartTime;
    public long Size;
}
