package org.simulatest.example.banking;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

/**
 * DDL for the banking domain. Must be applied BEFORE any environment pushes a
 * level, since DDL causes an implicit commit that invalidates open savepoints.
 */
public final class BankingSchema {

	private static final List<String> DDL = List.of(
		"CREATE TABLE IF NOT EXISTS currency (" +
			"  code VARCHAR(3) PRIMARY KEY," +
			"  name VARCHAR(50) NOT NULL" +
			")",
		"CREATE TABLE IF NOT EXISTS customer (" +
			"  id INT PRIMARY KEY," +
			"  name VARCHAR(100) NOT NULL" +
			")",
		"CREATE TABLE IF NOT EXISTS account (" +
			"  id INT PRIMARY KEY," +
			"  customer_id INT NOT NULL REFERENCES customer(id)," +
			"  currency_code VARCHAR(3) NOT NULL REFERENCES currency(code)," +
			"  kind VARCHAR(20) NOT NULL," +
			"  balance_cents BIGINT NOT NULL DEFAULT 0" +
			")",
		"CREATE TABLE IF NOT EXISTS audit_log (" +
			"  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
			"  account_id INT NOT NULL REFERENCES account(id)," +
			"  amount_cents BIGINT NOT NULL," +
			"  description VARCHAR(200) NOT NULL" +
			")"
	);

	private BankingSchema() {}

	public static void create(DataSource dataSource) {
		try (Connection conn = dataSource.getConnection();
			 Statement stmt = conn.createStatement()) {
			for (String ddl : DDL) stmt.execute(ddl);
		} catch (SQLException e) {
			throw new BankingDatabaseException("Failed to create banking schema", e);
		}
	}

}
