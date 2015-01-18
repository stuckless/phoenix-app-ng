package com.androideasyapps.phoenix.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * Android has some issues with H2 pool, so this is a very simple pool.
 *
 * @author seans
 */
public class DBPool {
    private String url;

    private LinkedList<Connection> freeConnections = new LinkedList<Connection>();
    private int active = 0;

    public DBPool() {
    }

    DBPool(String url) {
        this.url = url;
    }

    public static DBPool create(String jdbcUrl, String string, String string2) {
        return new DBPool(jdbcUrl);
    }

    Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public Connection getConnection() throws SQLException {
        synchronized (freeConnections) {
            active++;
            Connection c = freeConnections.poll();
            if (c == null) {
                c = createConnection();
            }
            return c;
        }
    }

    public void release(Connection c) throws SQLException {
        active--;
        synchronized (freeConnections) {
            if (freeConnections.size() > 10) {
                c.close();
            } else {
                freeConnections.add(c);
            }
        }
    }

    public int getActiveConnections() {
        return active;
    }

    public int getPoolSize() {
        return freeConnections.size();
    }

    public void dispose() {
        synchronized (freeConnections) {
            for (Connection c : freeConnections) {
                try {
                    c.close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            freeConnections.clear();
        }
    }
}
