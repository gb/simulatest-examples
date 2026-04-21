package org.simulatest.example.banking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.simulatest.insistencelayer.InsistenceLayerFactory;

/**
 * Thin JDBC helper that routes every call through the Insistence Layer's wrapped
 * {@code DataSource}. Services inject this instead of {@code DataSource} directly
 * so they always go through the savepoint stack, regardless of whether the Guice
 * plugin or the banking plugin wired the {@code DataSource} first.
 */
@Singleton
public final class BankingDatabase {

	@Inject public BankingDatabase() {}

	public void execute(String sql) {
		withStatement("execute", sql, Statement::execute);
	}

	public void update(String sql, Object... params) {
		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			bind(stmt, params);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new BankingDatabaseException("update failed: " + sql, e);
		}
	}

	public long queryLong(String sql, Object... params) {
		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			bind(stmt, params);
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) throw new BankingDatabaseException("no rows: " + sql);
				return rs.getLong(1);
			}
		} catch (SQLException e) {
			throw new BankingDatabaseException("query failed: " + sql, e);
		}
	}

	public int queryInt(String sql, Object... params) {
		return Math.toIntExact(queryLong(sql, params));
	}

	public String queryString(String sql, Object... params) {
		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			bind(stmt, params);
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) throw new BankingDatabaseException("no rows: " + sql);
				return rs.getString(1);
			}
		} catch (SQLException e) {
			throw new BankingDatabaseException("query failed: " + sql, e);
		}
	}

	private Connection getConnection() throws SQLException {
		return InsistenceLayerFactory.requireDataSource().getConnection();
	}

	@FunctionalInterface
	private interface StatementAction {
		void run(Statement stmt, String sql) throws SQLException;
	}

	private void withStatement(String op, String sql, StatementAction action) {
		try (Connection conn = getConnection();
			 Statement stmt = conn.createStatement()) {
			action.run(stmt, sql);
		} catch (SQLException e) {
			throw new BankingDatabaseException(op + " failed: " + sql, e);
		}
	}

	private static void bind(PreparedStatement stmt, Object... params) throws SQLException {
		for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
	}

}
