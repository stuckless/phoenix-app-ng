package com.androideasyapps.phoenix.dao;

// Generated Class DO NOT EDIT

import com.androideasyapps.phoenix.dao.BaseDAO;
import com.androideasyapps.phoenix.dao.ChangesBean;
import com.androideasyapps.phoenix.dao.ChangesCollection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MediaFileH2DAO extends BaseDAO<H2PersistenceManager,MediaFile> {
  private static final Map<String,String> SQL_FIELD_MAP = new HashMap<String,String>();
  static {
    SQL_FIELD_MAP.put("_id","_id");
    SQL_FIELD_MAP.put("mediafileid","mediaFileId");
    SQL_FIELD_MAP.put("title","title");
    SQL_FIELD_MAP.put("year","year");
    SQL_FIELD_MAP.put("mediatype","mediaType");
    SQL_FIELD_MAP.put("season","season");
    SQL_FIELD_MAP.put("episode","episode");
    SQL_FIELD_MAP.put("episodetitle","episodeTitle");
    SQL_FIELD_MAP.put("duration","duration");
    SQL_FIELD_MAP.put("originalairdate","originalAirDate");
    SQL_FIELD_MAP.put("filedate","fileDate");
    SQL_FIELD_MAP.put("watched","watched");
    SQL_FIELD_MAP.put("description","description");
    SQL_FIELD_MAP.put("userrating","userRating");
    SQL_FIELD_MAP.put("trailerurl","trailerURL");
    SQL_FIELD_MAP.put("genre","genre");
    SQL_FIELD_MAP.put("rating","rating");
    SQL_FIELD_MAP.put("userdata","userdata");
    SQL_FIELD_MAP.put("parts","parts");
    SQL_FIELD_MAP.put("hasmetadata","hasmetadata");
  }
  public static final String COLUMN__ID = "_id";
  public static final String COLUMN_MEDIAFILEID = "mediaFileId";
  public static final String COLUMN_TITLE = "title";
  public static final String COLUMN_YEAR = "year";
  public static final String COLUMN_MEDIATYPE = "mediaType";
  public static final String COLUMN_SEASON = "season";
  public static final String COLUMN_EPISODE = "episode";
  public static final String COLUMN_EPISODETITLE = "episodeTitle";
  public static final String COLUMN_DURATION = "duration";
  public static final String COLUMN_ORIGINALAIRDATE = "originalAirDate";
  public static final String COLUMN_FILEDATE = "fileDate";
  public static final String COLUMN_WATCHED = "watched";
  public static final String COLUMN_DESCRIPTION = "description";
  public static final String COLUMN_USERRATING = "userRating";
  public static final String COLUMN_TRAILERURL = "trailerURL";
  public static final String COLUMN_GENRE = "genre";
  public static final String COLUMN_RATING = "rating";
  public static final String COLUMN_USERDATA = "userdata";
  public static final String COLUMN_PARTS = "parts";
  public static final String COLUMN_HASMETADATA = "hasmetadata";
  public static final int VERSION = 4;
  public MediaFileH2DAO(H2PersistenceManager ctx) {
    super(ctx);
  }
  public String getTableName() {
    return "MEDIAFILE";
  }
  private static final String[] FIELDS = new String[] {"_id","mediafileid","title","year","mediatype","season","episode","episodetitle","duration","originalairdate","filedate","watched","description","userrating","trailerurl","genre","rating","userdata","parts","hasmetadata"};
  public String[] getColumnNames() {
    return FIELDS;
  }
  protected String[] getCREATESQL() {
    return new String[] {"create table if not exists MEDIAFILE (_id identity, mediafileid bigint, title varchar(256) not null, year int, mediatype varchar(20), season int, episode int, episodetitle varchar, duration bigint, originalairdate bigint, filedate bigint, watched boolean, description clob, userrating int, trailerurl varchar, genre varchar, rating varchar, userdata varchar, parts int, hasmetadata boolean)","create index if not exists NDX_MEDIAFILEID on MEDIAFILE(mediafileid)","create index if not exists NDX_HASMETADATA on MEDIAFILE(hasmetadata)","create index if not exists NDX_YEAR on MEDIAFILE(year)","create index if not exists NDX_MEDIATYPE on MEDIAFILE(mediatype)","create index if not exists NDX_RATING on MEDIAFILE(rating)","create index if not exists NDX_EPISODE on MEDIAFILE(episode)","create index if not exists NDX_WATCHED on MEDIAFILE(watched)","create index if not exists NDX_TITLE on MEDIAFILE(title)","create index if not exists NDX_GENRE on MEDIAFILE(genre)","create index if not exists NDX__ID on MEDIAFILE(_id)","create index if not exists NDX_SEASON on MEDIAFILE(season)"};
  }
  protected String[] getUPDATESQL_VERSION_4() {
    String sqls[] = new String[1];
    sqls[0]="alter table MEDIAFILE add column if not exists hasmetadata boolean";
    return sqls;
  }
  protected String[] getUPDATESQL_VERSION_3() {
    String sqls[] = new String[2];
    sqls[0]="alter table MEDIAFILE add column if not exists userdata varchar";
    sqls[1]="alter table MEDIAFILE add column if not exists parts int";
    return sqls;
  }
  protected String[] getUPDATESQL_VERSION_2() {
    String sqls[] = new String[1];
    sqls[0]="alter table MEDIAFILE add column if not exists rating varchar";
    return sqls;
  }
  protected void doSchemaUpdates(Connection conn)
      throws Exception {
    log.info("Begin Upgrading Schema to Version 2");
    for (String sql: getUPDATESQL_VERSION_2()) {
      conn.createStatement().execute(sql);
    }
    log.info("End Upgrading Schema to Version 2");
    log.info("Begin Upgrading Schema to Version 3");
    for (String sql: getUPDATESQL_VERSION_3()) {
      conn.createStatement().execute(sql);
    }
    log.info("End Upgrading Schema to Version 3");
    log.info("Begin Upgrading Schema to Version 4");
    for (String sql: getUPDATESQL_VERSION_4()) {
      conn.createStatement().execute(sql);
    }
    log.info("End Upgrading Schema to Version 4");
  }
  protected PreparedStatement prepareInsert(Connection conn, MediaFile item)
      throws SQLException {
    PreparedStatement ps = conn.prepareStatement("insert into MEDIAFILE (mediafileid,title,year,mediatype,season,episode,episodetitle,duration,originalairdate,filedate,watched,description,userrating,trailerurl,genre,rating,userdata,parts,hasmetadata) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
    ps.setLong(1, item.getMediaFileId());
    ps.setString(2, item.getTitle());
    ps.setInt(3, item.getYear());
    ps.setString(4, item.getMediaType());
    ps.setInt(5, item.getSeason());
    ps.setInt(6, item.getEpisode());
    ps.setString(7, item.getEpisodeTitle());
    ps.setLong(8, item.getDuration());
    ps.setLong(9, item.getOriginalAirDate());
    ps.setLong(10, item.getFileDate());
    ps.setBoolean(11, item.getWatched());
    ps.setString(12, item.getDescription());
    ps.setInt(13, item.getUserRating());
    ps.setString(14, item.getTrailerURL());
    ps.setString(15, item.getGenre());
    ps.setString(16, item.getRating());
    ps.setString(17, item.getUserdata());
    ps.setInt(18, item.getParts());
    ps.setBoolean(19, item.getHasmetadata());
    return ps;
  }
  protected MediaFile newItem() {
    return new MediaFile();
  }
  protected MediaFile copyResultSetToItem(ResultSet rs, MediaFile item, Connection conn)
      throws Exception {
    item._id=rs.getLong("_id");
    item.mediaFileId=rs.getLong("mediafileid");
    item.title=rs.getString("title");
    item.year=rs.getInt("year");
    item.mediaType=rs.getString("mediatype");
    item.season=rs.getInt("season");
    item.episode=rs.getInt("episode");
    item.episodeTitle=rs.getString("episodetitle");
    item.duration=rs.getLong("duration");
    item.originalAirDate=rs.getLong("originalairdate");
    item.fileDate=rs.getLong("filedate");
    item.watched=rs.getBoolean("watched");
    item.description=rs.getString("description");
    item.userRating=rs.getInt("userrating");
    item.trailerURL=rs.getString("trailerurl");
    item.genre=rs.getString("genre");
    item.rating=rs.getString("rating");
    item.userdata=rs.getString("userdata");
    item.parts=rs.getInt("parts");
    item.hasmetadata=rs.getBoolean("hasmetadata");
    return item;
  }
  protected void updateForeignCollections(MediaFile item, Connection conn)
      throws Exception {
  }
}
