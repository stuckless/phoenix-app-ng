package com.androideasyapps.phoenix.dao;

// Generated Class DO NOT EDIT

import com.androideasyapps.phoenix.dao.AbstractPersistenceManager;

public class H2PersistenceManager extends AbstractPersistenceManager {
  public H2PersistenceManager(String jdbcUrl) {
    super(jdbcUrl);
  }
  MediaFileH2DAO mMediaFileDAO;
  public MediaFileH2DAO getMediaFileDAO() {
    if (mMediaFileDAO==null) {
      this.mMediaFileDAO= new MediaFileH2DAO(this);
    }
    return mMediaFileDAO;
  }
  ServerH2DAO mServerDAO;
  public ServerH2DAO getServerDAO() {
    if (mServerDAO==null) {
      this.mServerDAO= new ServerH2DAO(this);
    }
    return mServerDAO;
  }
  public void createTables()
      throws Exception {
    getMediaFileDAO().create();
    getServerDAO().create();
  }
}
