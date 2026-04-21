package org.simulatest.example.seeding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * Tiny JDBC helper that environments use without knowing whether they're
 * running in seed mode (raw {@code DataSource}, writes persist) or what-if
 * mode (Insistence-Layer-wrapped {@code DataSource}, writes are undone).
 *
 * <p>{@code Main} sets the {@code DataSource} once before invoking
 * {@link org.simulatest.environment.EnvironmentRunner}.
 */
public final class DevDatabase {

	private static volatile DataSource dataSource;

	private DevDatabase() {}

	public static void use(DataSource ds) {
		dataSource = ds;
	}

	public static Connection getConnection() throws SQLException {
		DataSource ds = dataSource;
		if (ds == null) {
			throw new IllegalStateException(
				"DevDatabase.use(DataSource) must be called before any environment runs.");
		}
		return ds.getConnection();
	}

	public static void execute(String sql, Object... params) {
		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException("update failed: " + sql, e);
		}
	}

	public static int queryInt(DataSource ds, String sql) {
		try (Connection conn = ds.getConnection();
			 Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			if (!rs.next()) throw new IllegalStateException("no rows: " + sql);
			return rs.getInt(1);
		} catch (SQLException e) {
			throw new IllegalStateException("query failed: " + sql, e);
		}
	}

}
