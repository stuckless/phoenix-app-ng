package com.androideasyapps.phoenix.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPersistenceManager {
	protected static AtomicLong connectionIdFactory = new AtomicLong();

	public static final Logger log = LoggerFactory
			.getLogger(AbstractPersistenceManager.class);

	protected String jdbcUrl;

	public AbstractPersistenceManager(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
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

	Connection conn = null;

	protected Connection getConnection() {
		if (conn == null) {
			try {
				Class.forName("org.h2.Driver");
				conn = DriverManager.getConnection(jdbcUrl);
			} catch (Throwable t) {
				throw new RuntimeException("Failed to get Connection for "
						+ jdbcUrl, t);
			}
		}
		return conn;
	}

	public ConnectionWrapper getTransactionalConnection() {
		return new ConnectionWrapper(getConnection(), false);
	}

	protected void release(ConnectionWrapper conn) {
		log.debug("Releasing Connection for {}", conn.connectionId);
	}

	public void destroy() {
		try {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			log.warn("SQL Errors during Connection.close()", e);
		}
	}
}
