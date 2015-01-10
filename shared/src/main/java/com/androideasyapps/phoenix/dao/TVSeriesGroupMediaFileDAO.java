package com.androideasyapps.phoenix.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by seans on 23/12/14.
 */
public class TVSeriesGroupMediaFileDAO extends BaseDAO<H2PersistenceManager, MediaFile> {
    public TVSeriesGroupMediaFileDAO(H2PersistenceManager h2PersistenceManager) {
        super(h2PersistenceManager);
    }

    @Override
    public String getTableName() {
        return "mediafile";
    }

    @Override
    public String[] getColumnNames() {
        return new String[] {"title","mediatype","originalairdate","parts"};
    }

    @Override
    protected String[] getCREATESQL() {
        return null;
    }

    @Override
    protected PreparedStatement prepareInsert(Connection conn, MediaFile item) throws SQLException {
        return null;
    }

    @Override
    protected MediaFile newItem() {
        return new MediaFile();
    }

    @Override
    protected MediaFile copyResultSetToItem(ResultSet rs, MediaFile item, Connection conn) throws Exception {
        item.setTitle(rs.getString("title"));
        item.setMediaType(rs.getString("mediatype"));
        item.setOriginalAirDate(rs.getLong("originalairdate"));
        item.setParts(rs.getInt("parts"));
//        int columns = rs.getMetaData().getColumnCount();
//        for (int i=1;i<=columns;i++) {
//            System.out.println("COLUMN: " + i + "; " + rs.getMetaData().getColumnName(i));
//        }
        return item;
    }

    @Override
    protected void updateForeignCollections(MediaFile item, Connection conn) throws Exception {

    }

    @Override
    protected void doSchemaUpdates(Connection conn) throws Exception {
    }
}
