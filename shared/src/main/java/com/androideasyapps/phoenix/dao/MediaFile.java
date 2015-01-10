package com.androideasyapps.phoenix.dao;

// Generated Class DO NOT EDIT

import com.androideasyapps.phoenix.dao.ChangesBean;
import com.androideasyapps.phoenix.dao.ChangesCollection;
import com.androideasyapps.phoenix.dao.HasID;
import java.io.Serializable;

public class MediaFile extends ChangesBean
    implements IMediaFile, Serializable, HasID {
  public MediaFile() {
    super();
  }
  protected long _id;
  protected long mediaFileId;
  protected String title;
  protected int year;
  protected String mediaType;
  protected int season;
  protected int episode;
  protected String episodeTitle;
  protected long duration;
  protected long originalAirDate;
  protected long fileDate;
  protected boolean watched;
  protected String description;
  protected int userRating;
  protected String trailerURL;
  protected String genre;
  protected String rating;
  protected String userdata;
  protected int parts;
  protected boolean hasmetadata;
  public long getId() {
    return this._id;
  }
  public void setId(long _id) {
    long old = this._id;
    this._id=_id;
    onChanged("_id", old, this._id);
  }
  public long getMediaFileId() {
    return this.mediaFileId;
  }
  public void setMediaFileId(long mediaFileId) {
    long old = this.mediaFileId;
    this.mediaFileId=mediaFileId;
    onChanged("mediaFileId", old, this.mediaFileId);
  }
  public String getTitle() {
    return this.title;
  }
  public void setTitle(String title) {
    String old = this.title;
    this.title=title;
    onChanged("title", old, this.title);
  }
  public int getYear() {
    return this.year;
  }
  public void setYear(int year) {
    int old = this.year;
    this.year=year;
    onChanged("year", old, this.year);
  }
  public String getMediaType() {
    return this.mediaType;
  }
  public void setMediaType(String mediaType) {
    String old = this.mediaType;
    this.mediaType=mediaType;
    onChanged("mediaType", old, this.mediaType);
  }
  public int getSeason() {
    return this.season;
  }
  public void setSeason(int season) {
    int old = this.season;
    this.season=season;
    onChanged("season", old, this.season);
  }
  public int getEpisode() {
    return this.episode;
  }
  public void setEpisode(int episode) {
    int old = this.episode;
    this.episode=episode;
    onChanged("episode", old, this.episode);
  }
  public String getEpisodeTitle() {
    return this.episodeTitle;
  }
  public void setEpisodeTitle(String episodeTitle) {
    String old = this.episodeTitle;
    this.episodeTitle=episodeTitle;
    onChanged("episodeTitle", old, this.episodeTitle);
  }
  public long getDuration() {
    return this.duration;
  }
  public void setDuration(long duration) {
    long old = this.duration;
    this.duration=duration;
    onChanged("duration", old, this.duration);
  }
  public long getOriginalAirDate() {
    return this.originalAirDate;
  }
  public void setOriginalAirDate(long originalAirDate) {
    long old = this.originalAirDate;
    this.originalAirDate=originalAirDate;
    onChanged("originalAirDate", old, this.originalAirDate);
  }
  public long getFileDate() {
    return this.fileDate;
  }
  public void setFileDate(long fileDate) {
    long old = this.fileDate;
    this.fileDate=fileDate;
    onChanged("fileDate", old, this.fileDate);
  }
  public boolean getWatched() {
    return this.watched;
  }
  public void setWatched(boolean watched) {
    boolean old = this.watched;
    this.watched=watched;
    onChanged("watched", old, this.watched);
  }
  public String getDescription() {
    return this.description;
  }
  public void setDescription(String description) {
    String old = this.description;
    this.description=description;
    onChanged("description", old, this.description);
  }
  public int getUserRating() {
    return this.userRating;
  }
  public void setUserRating(int userRating) {
    int old = this.userRating;
    this.userRating=userRating;
    onChanged("userRating", old, this.userRating);
  }
  public String getTrailerURL() {
    return this.trailerURL;
  }
  public void setTrailerURL(String trailerURL) {
    String old = this.trailerURL;
    this.trailerURL=trailerURL;
    onChanged("trailerURL", old, this.trailerURL);
  }
  public String getGenre() {
    return this.genre;
  }
  public void setGenre(String genre) {
    String old = this.genre;
    this.genre=genre;
    onChanged("genre", old, this.genre);
  }
  public String getRating() {
    return this.rating;
  }
  public void setRating(String rating) {
    String old = this.rating;
    this.rating=rating;
    onChanged("rating", old, this.rating);
  }
  public String getUserdata() {
    return this.userdata;
  }
  public void setUserdata(String userdata) {
    String old = this.userdata;
    this.userdata=userdata;
    onChanged("userdata", old, this.userdata);
  }
  public int getParts() {
    return this.parts;
  }
  public void setParts(int parts) {
    int old = this.parts;
    this.parts=parts;
    onChanged("parts", old, this.parts);
  }
  public boolean getHasmetadata() {
    return this.hasmetadata;
  }
  public void setHasmetadata(boolean hasmetadata) {
    boolean old = this.hasmetadata;
    this.hasmetadata=hasmetadata;
    onChanged("hasmetadata", old, this.hasmetadata);
  }
}
