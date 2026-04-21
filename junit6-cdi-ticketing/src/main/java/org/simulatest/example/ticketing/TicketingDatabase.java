package org.simulatest.example.ticketing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.enterprise.context.ApplicationScoped;
import org.simulatest.insistencelayer.InsistenceLayerFactory;

/**
 * Thin JDBC helper routing every call through the Insistence Layer's wrapped
 * {@code DataSource}. CDI beans inject this rather than {@code DataSource}
 * directly; see the README's "Why not inject DataSource?" note.
 */
@ApplicationScoped
public class TicketingDatabase {

	public void update(String sql, Object... params) {
		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			bind(stmt, params);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new TicketingDatabaseException("update failed: " + sql, e);
		}
	}

	public long queryLong(String sql, Object... params) {
		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			bind(stmt, params);
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) throw new TicketingDatabaseException("no rows: " + sql);
				return rs.getLong(1);
			}
		} catch (SQLException e) {
			throw new TicketingDatabaseException("query failed: " + sql, e);
		}
	}

	public int queryInt(String sql, Object... params) {
		return Math.toIntExact(queryLong(sql, params));
	}

	private Connection getConnection() throws SQLException {
		return InsistenceLayerFactory.requireDataSource().getConnection();
	}

	private static void bind(PreparedStatement stmt, Object... params) throws SQLException {
		for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
	}

}
