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

public class ServerH2DAO extends BaseDAO<H2PersistenceManager,Server> {
  private static final Map<String,String> SQL_FIELD_MAP = new HashMap<String,String>();
  static {
    SQL_FIELD_MAP.put("_id","_id");
    SQL_FIELD_MAP.put("serverid","serverId");
    SQL_FIELD_MAP.put("title","title");
    SQL_FIELD_MAP.put("host","host");
    SQL_FIELD_MAP.put("port","port");
    SQL_FIELD_MAP.put("username","username");
    SQL_FIELD_MAP.put("password","password");
    SQL_FIELD_MAP.put("isdefault","isDefault");
  }
  public static final String COLUMN__ID = "_id";
  public static final String COLUMN_SERVERID = "serverId";
  public static final String COLUMN_TITLE = "title";
  public static final String COLUMN_HOST = "host";
  public static final String COLUMN_PORT = "port";
  public static final String COLUMN_USERNAME = "username";
  public static final String COLUMN_PASSWORD = "password";
  public static final String COLUMN_ISDEFAULT = "isDefault";
  public static final int VERSION = 1;
  public ServerH2DAO(H2PersistenceManager ctx) {
    super(ctx);
  }
  public String getTableName() {
    return "SERVER";
  }
  private static final String[] FIELDS = new String[] {"_id","serverid","title","host","port","username","password","isdefault"};
  public String[] getColumnNames() {
    return FIELDS;
  }
  protected String[] getCREATESQL() {
    return new String[] {"create table if not exists SERVER (_id identity, serverid varchar, title varchar not null, host varchar, port int, username varchar, password varchar, isdefault boolean)","create index if not exists NDX_SERVERID on SERVER(serverid)","create index if not exists NDX_TITLE on SERVER(title)","create index if not exists NDX__ID on SERVER(_id)"};
  }
  protected void doSchemaUpdates(Connection conn)
      throws Exception {
  }
  protected PreparedStatement prepareInsert(Connection conn, Server item)
      throws SQLException {
    PreparedStatement ps = conn.prepareStatement("insert into SERVER (serverid,title,host,port,username,password,isdefault) values (?,?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
    ps.setString(1, item.getServerId());
    ps.setString(2, item.getTitle());
    ps.setString(3, item.getHost());
    ps.setInt(4, item.getPort());
    ps.setString(5, item.getUsername());
    ps.setString(6, item.getPassword());
    ps.setBoolean(7, item.getIsDefault());
    return ps;
  }
  protected Server newItem() {
    return new Server();
  }
  protected Server copyResultSetToItem(ResultSet rs, Server item, Connection conn)
      throws Exception {
    item._id=rs.getLong("_id");
    item.serverId=rs.getString("serverid");
    item.title=rs.getString("title");
    item.host=rs.getString("host");
    item.port=rs.getInt("port");
    item.username=rs.getString("username");
    item.password=rs.getString("password");
    item.isDefault=rs.getBoolean("isdefault");
    return item;
  }
  protected void updateForeignCollections(Server item, Connection conn)
      throws Exception {
  }
}
