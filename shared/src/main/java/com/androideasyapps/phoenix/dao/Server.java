package com.androideasyapps.phoenix.dao;

// Generated Class DO NOT EDIT

import com.androideasyapps.phoenix.dao.ChangesBean;
import com.androideasyapps.phoenix.dao.ChangesCollection;
import com.androideasyapps.phoenix.dao.HasID;
import java.io.Serializable;

public class Server extends ChangesBean
    implements IServer, Serializable, HasID {
  public Server() {
    super();
  }
  protected long _id;
  protected String serverId;
  protected String title;
  protected String host;
  protected int port;
  protected String username;
  protected String password;
  protected boolean isDefault;
  public long getId() {
    return this._id;
  }
  public void setId(long _id) {
    long old = this._id;
    this._id=_id;
    onChanged("_id", old, this._id);
  }
  public String getServerId() {
    return this.serverId;
  }
  public void setServerId(String serverId) {
    String old = this.serverId;
    this.serverId=serverId;
    onChanged("serverId", old, this.serverId);
  }
  public String getTitle() {
    return this.title;
  }
  public void setTitle(String title) {
    String old = this.title;
    this.title=title;
    onChanged("title", old, this.title);
  }
  public String getHost() {
    return this.host;
  }
  public void setHost(String host) {
    String old = this.host;
    this.host=host;
    onChanged("host", old, this.host);
  }
  public int getPort() {
    return this.port;
  }
  public void setPort(int port) {
    int old = this.port;
    this.port=port;
    onChanged("port", old, this.port);
  }
  public String getUsername() {
    return this.username;
  }
  public void setUsername(String username) {
    String old = this.username;
    this.username=username;
    onChanged("username", old, this.username);
  }
  public String getPassword() {
    return this.password;
  }
  public void setPassword(String password) {
    String old = this.password;
    this.password=password;
    onChanged("password", old, this.password);
  }
  public boolean getIsDefault() {
    return this.isDefault;
  }
  public void setIsDefault(boolean isDefault) {
    boolean old = this.isDefault;
    this.isDefault=isDefault;
    onChanged("isDefault", old, this.isDefault);
  }
}
