package com.androideasyapps.phoenix.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.androideasyapps.phoenix.dao.AbstractPersistenceManager.ConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: we might need to create an IModel interface for all model objects
// TODO: create a central PersistenceManager that could be a singleton that is the 
// central point for all DAO objects.
public abstract class BaseDAO<PM extends AbstractPersistenceManager, T extends ChangesBean> {
	protected static final Logger log = LoggerFactory.getLogger("com.androideasyapps.phoenix.dao.db");

	protected PM persistenceManager;

	public BaseDAO(PM pm) {
		this.persistenceManager = pm;
	}

	public abstract String getTableName();

	public abstract String[] getColumnNames();

	protected abstract String[] getCREATESQL();

	protected abstract PreparedStatement prepareInsert(Connection conn, T item)
			throws SQLException;

	protected abstract T newItem();

	protected abstract T copyResultSetToItem(ResultSet rs, T item,
			Connection conn) throws Exception;

	protected abstract void updateForeignCollections(T item, Connection conn)
			throws Exception;

	// public abstract int createIndexes();
	// public abstract int upgrade(); // see upgrade note in NOTES.txt

	// public abstract int update(T item);
	//
	// // partial object
	// public abstract Collection<T> query(String[] columns, String where,
	// Object... args);
	//
	// full query
	public Collection<T> query(String where, Object... args) throws Exception {
		ConnectionWrapper c = persistenceManager.getTransactionalConnection();
		try {
			Connection conn = c.getConnection();

			StringBuilder sql = new StringBuilder("select * from "
					+ getTableName());
			if (where != null)
				sql.append(" where ").append(where);

			PreparedStatement p = conn.prepareStatement(sql.toString());
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					p.setObject(i + 1, args[i]);
				}
			}
			ResultSet rs = p.executeQuery();
			List<T> rows = new ArrayList<T>();
			while (rs.next()) {
				rows.add(copyResultSetToItem(rs, newItem(), conn));
			}
			return rows;
		} finally {
			release(c);
		}
	}
	
	public Collection<T> queryAll() throws Exception {
		return query(null);
	}

	private void release(ConnectionWrapper c) {
		// work around so that we can use it in android, which doesn't support
		// try with resources :(
		if (c!=null) {
			try {
				c.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Collection<T> rawQuery(String sql, Object... args) throws Exception {
		ConnectionWrapper c = persistenceManager.getTransactionalConnection();
		try {
			return rawQuery(c.getConnection(), sql, args);
		} finally {
			release(c);
		}
	}

	/**
	 * Executes the query and return the first field, in the first result. (useful for things
	 * like, select max(field) from table
	 * @param sql
	 * @param args
	 * @return value as object, or null.
	 * @throws Exception
	 */
	public <T2> T2 rawQueryFirstField(String sql, Object... args) throws Exception {
		ConnectionWrapper c = persistenceManager.getTransactionalConnection();
		try {
			PreparedStatement p = c.getConnection().prepareStatement(sql.toString());
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					p.setObject(i + 1, args[i]);
				}
			}
			ResultSet rs = p.executeQuery();
			if (rs.next()) {
				return (T2) rs.getObject(1);
			}
		} finally {
			release(c);
		}
		
		return null;
	}
	
	public long max(String field) throws Exception {
		Number val = rawQueryFirstField("select max("+field+") from " + getTableName(), null);
		if (val==null) return 0;
		return val.longValue();
	}

	public Collection<T> rawQuery(Connection conn, String sql, Object... args)
			throws Exception {
		PreparedStatement p = conn.prepareStatement(sql.toString());
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				p.setObject(i + 1, args[i]);
			}
		}
		ResultSet rs = p.executeQuery();
		List<T> rows = new ArrayList<T>();
		while (rs.next()) {
			rows.add(copyResultSetToItem(rs, newItem(), conn));
		}
		return rows;
	}

	public void save(T item) throws Exception {
		if (((HasID) item).getId() == 0) {
			insert(item);
		} else {
			updateChanges(item);
		}
	}

	public void save(T item, Connection conn) throws Exception {
		if (((HasID) item).getId() == 0) {
			insert(item, conn);
		} else {
			updateChanges(item, conn);
		}
	}

	public T refresh(T item) throws Exception {
		return findById(((HasID) item).getId(), item);
	}

	public void create() throws Exception {
		ConnectionWrapper c = persistenceManager.getAutoComitConnection();
		try {
			Connection conn = c.getConnection();
			for (String sql : getCREATESQL()) {
				conn.createStatement().execute(sql);
			}
			doSchemaUpdates(conn);
		} finally {
			release(c);;
		}
	}

	protected abstract void doSchemaUpdates(Connection conn) throws Exception;

	public int insert(T item) throws Exception {
		ConnectionWrapper c = persistenceManager
				.getTransactionalConnection();
		try {
			return insert(item, c.getConnection());
		} finally {
			release(c);
		}
	}

	public int insert(T item, Connection conn) throws Exception {
		// TODO: we need to insert foriegn objects first, and get it's result
		// value
		// and set in the foriegn reference of this object, and then save this
		// object

		PreparedStatement ps = prepareInsert(conn, item);
		try {
			int i = ps.executeUpdate();
			if (i > 0) {
				setId(item, ps);
			}

			updateForeignCollections(item, conn);

			// once we save, we need to reset the changed status
			item.setChanged(false);
			return i;
		} finally {
			ps.close();
		}
	}

	protected void updatexxxCastMemberForeignCollection(T item, Connection conn) {
		// ChangesCollection<CastMember> coll = item.getCastMembers();
		// if (!coll.isChanged()) return; // do nothing, since nothing has
		// changed.

		// process additions
		// CastMemberH2DAO relatedDao = persistenceManager.getCastMemberDAO();
		// for (CastMember cb: coll.additions) {
		// // save the item, if necessary
		// if (cb.isChanged()) relatedDao.save(cb, conn);
		//
		// // insert the relationship
		// try {
		// PreparedStatement ps =
		// conn.prepareStatement("insert into MEDIAFILE_CASTMEMBER (MEDIFILE_ID, CASTMEMBER_ID) values (?, ?)");
		// ps.setLong(1, item.getId());
		// ps.setLong(2, cb.getId());
		// ps.executeUpdate();
		// } catch (Exception e) {
		// // row exists...
		// }
		// }

		// // process updates
		// for (CastMember cb: coll.additions) {
		// // save the item, if necessary
		// if (cb.isChanged()) relatedDao.save(conn, cb);
		//
		// // insert the relationship (is this necessary)
		// try {
		// PreparedStatement ps =
		// conn.prepareStatement("insert into MEDIAFILE_CASTMEMBER (MEDIFILE_ID, CASTMEMBER_ID) values (?, ?)");
		// ps.setLong(1, item.getId());
		// ps.setLong(2, cb.getId());
		// } catch (Exception e) {
		// // row exists...
		// }
		// }

		// // process deletes
		// for (CastMember cb: coll.additions) {
		// // save the item, if necessary
		// if (cb.isChanged()) relatedDao.save(conn, cb);
		//
		// // delete the relationship... we do not delete the item, though
		// PreparedStatement ps =
		// conn.prepareStatement("delete from MEDIAFILE_CASTMEMBER where MEDIFILE_ID=? and CASTMEMBER_ID=?");
		// ps.setLong(1, item.getId());
		// ps.setLong(2, cb.getId());
		// }
	}

	public int updateChanges(T item) throws Exception {
		if (item == null)
			return 0;
		if (item.getChanges().size() == 0) {
			log.debug("Nothing Changed for item {}; Class: ",
					((HasID) item).getId(), ((Object)item).getClass().getName());
			return 0;
		}

		ConnectionWrapper c = persistenceManager
				.getTransactionalConnection();
		try {
			return updateChanges(item, c.getConnection());
		} finally {
			release(c);
		}
	}

	public int updateChanges(T item, Connection conn) throws Exception {
		updateForeignCollections(item, conn);

		StringBuilder cols = new StringBuilder();
		for (Map.Entry<String, Object> me : item.getChanges().entrySet()) {
			if (cols.length() > 0) {
				cols.append(",");
			}
			cols.append(me.getKey()).append("=?");
		}

		int i = 1;
		PreparedStatement ps = conn.prepareStatement("update " + getTableName()
				+ " set " + cols.toString() + " where _id=?");
		for (Map.Entry<String, Object> me : item.getChanges().entrySet()) {
			ps.setObject(i++, me.getValue());
		}
		ps.setLong(i, ((HasID) item).getId());
		int v = ps.executeUpdate();
		if (v > 0) {
			item.setChanged(false);
		}
		ps.close();

		return v;
	}

	public T setId(T in, Statement st) throws SQLException {
		// http://stackoverflow.com/questions/1915166/how-to-get-the-insert-id-in-jdbc
		// we inserted a record, get the id
		ResultSet generatedKeys = st.getGeneratedKeys();
		try {
			if (generatedKeys.next()) {
				((HasID) in).setId(generatedKeys.getLong(1));
			} else {
				throw new SQLException("Creating user failed, no ID obtained.");
			}
		} finally {
			generatedKeys.close();
		}

		return in;
	}

	public int delete(T item) throws Exception {
		return delete(((HasID) item).getId());
	}

	public int delete(long id) throws Exception {
		ConnectionWrapper c = persistenceManager
				.getTransactionalConnection();
		try {
			Connection conn = c.getConnection();

			PreparedStatement p = conn.prepareStatement("delete from "
					+ getTableName() + " where _id = ?");
			p.setLong(1, id);
			return p.executeUpdate();
		} finally {
			release(c);
		}
	}

	public int deleteAll() throws Exception {
		ConnectionWrapper c = persistenceManager
				.getTransactionalConnection();
		try {
			Connection conn = c.getConnection();

			PreparedStatement p = conn.prepareStatement("delete from "
					+ getTableName());
			
			return p.executeUpdate();
		} finally {
			release(c);
		}
	}

	public int deleteWhere(String where, Object...args) throws Exception {
		ConnectionWrapper c = persistenceManager.getTransactionalConnection();
		try {
			Connection conn = c.getConnection();

			StringBuilder sql = new StringBuilder("delete from "
					+ getTableName());
			if (where != null)
				sql.append(" where ").append(where);

			PreparedStatement p = conn.prepareStatement(sql.toString());
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					p.setObject(i + 1, args[i]);
				}
			}
			
			return p.executeUpdate();
		} finally {
			release(c);
		}
	}
	
	
	public long count() throws Exception {
		ConnectionWrapper c = persistenceManager
				.getTransactionalConnection();
		try {
			Connection conn = c.getConnection();

			PreparedStatement p = conn.prepareStatement("select count(1) from "
					+ getTableName());
			ResultSet rs = p.executeQuery();
			if (rs.first()) {
				return rs.getLong(1);
			}
			return 0;
		} finally {
			release(c);
		}
	}

	public T findById(long id) throws Exception {
		return findById(id, newItem());
	}

	protected T findById(long id, T item) throws Exception {
		ConnectionWrapper c = persistenceManager
				.getTransactionalConnection();
		try {
			Connection conn = c.getConnection();

			PreparedStatement p = conn.prepareStatement("select * from "
					+ getTableName() + " where _id = ?");
			p.setLong(1, id);
			ResultSet rs = p.executeQuery();
			if (rs.first()) {
				copyResultSetToItem(rs, item, conn);
				return item;
			}
			return null;
		} finally {
			release(c);
		}
	}

	public T queryFirst(String where, Object... args) throws Exception {
		ConnectionWrapper c = persistenceManager
				.getTransactionalConnection();
		try {
			Connection conn = c.getConnection();

			PreparedStatement p = conn.prepareStatement("select * from "
					+ getTableName() + " where " + where);
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					p.setObject(i + 1, args[i]);
				}
			}

			ResultSet rs = p.executeQuery();
			if (rs.first()) {
				T item = newItem();
				copyResultSetToItem(rs, item, conn);
				return item;
			}

			return null;
		} finally {
			release(c);
		}
	}

}
