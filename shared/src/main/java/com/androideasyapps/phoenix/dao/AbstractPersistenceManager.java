package com.androideasyapps.phoenix.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractPersistenceManager {
	protected static AtomicLong connectionIdFactory = new AtomicLong();

	public static final Logger log = LoggerFactory
			.getLogger(AbstractPersistenceManager.class);

	protected String jdbcUrl;
    DBPool connectionPool;

	public AbstractPersistenceManager(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
        try {
            log.info("Persistence Manager created with jdbcUrl: {}", jdbcUrl);
            Class.forName("org.h2.Driver");
            this.connectionPool = DBPool.create(jdbcUrl, "sa", "sa");
        } catch (ClassNotFoundException e) {
            log.error("PerstenceManager does not have H2 drivers installed!  Everything will fail.");
        }
    }

	public abstract void createTables() throws Exception;
	
	// public abstract int upgradeTables();

	// public MediaFileDAO getMediaFileDOA();
	// public int getVersion("MEDIAFILE") // move to DAO
	// public boolean isUpgradeable("MEDIAFILE"); // move to DAO

	public class ConnectionWrapper {
		private Connection conn;
		private long connectionId;
		private Throwable error = null;

		public ConnectionWrapper(Connection conn, boolean autoCommit) {
			this.connectionId = connectionIdFactory.getAndIncrement();
			log.debug("New DB Connection {} with autocomit? ", connectionId,
					autoCommit);
			this.conn = conn;
			try {
				conn.setAutoCommit(autoCommit);
			} catch (SQLException e) {
				log.warn("Failed to set autocommit({}) for connection {}",
						autoCommit, connectionId, e);
			}
		}

		public Connection getConnection() {
			return conn;
		}

		// changed to make it work in android... ie, no autocloseables in android
		public void close() throws Exception {
			if (!conn.getAutoCommit()) {
				if (error == null) {
					log.debug("Committing Connection {}", connectionId);
					conn.commit();
				} else {
					log.error("Rolling back connection {} due to error",
							connectionId, error);
					conn.rollback();
				}
			}
			release(this);
			conn = null;
		}

		public Throwable getError() {
			return error;
		}

		public void setError(Throwable error) {
			this.error = error;
		}
	}

	public ConnectionWrapper getAutoComitConnection() {
		return new ConnectionWrapper(getConnection(), true);
	}

	protected Connection getConnection() {
        try {
            return connectionPool.getConnection();
        } catch (SQLException e) {
            log.error("Failed to get a connection from the pool.", e);
            return null;
        }
	}

	public ConnectionWrapper getTransactionalConnection() {
		return new ConnectionWrapper(getConnection(), false);
	}

	protected void release(ConnectionWrapper conn) {
		log.debug("Releasing Connection for {}", conn.connectionId);
        try {
            connectionPool.release(conn.getConnection());
        } catch (SQLException e) {
            log.error("Failed to release connection {}", conn.connectionId);
        }
    }

	public void destroy() {
        if (connectionPool != null) {
            log.info("Destroying PersistenceManager Connection Pool with {} activie connections and {} open free connections.", connectionPool.getActiveConnections(), connectionPool.getPoolSize());
            connectionPool.dispose();
        }
	}
}
