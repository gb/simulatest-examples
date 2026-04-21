package org.simulatest.example.seeding;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

public final class DevSchema {

	private static final List<String> DDL = List.of(
		"DROP TABLE IF EXISTS employee",
		"DROP TABLE IF EXISTS department",
		"DROP TABLE IF EXISTS company",
		"CREATE TABLE company (" +
			"  id INT PRIMARY KEY," +
			"  name VARCHAR(100) NOT NULL" +
			")",
		"CREATE TABLE department (" +
			"  id INT PRIMARY KEY," +
			"  company_id INT NOT NULL REFERENCES company(id)," +
			"  name VARCHAR(100) NOT NULL" +
			")",
		"CREATE TABLE employee (" +
			"  id INT PRIMARY KEY," +
			"  department_id INT NOT NULL REFERENCES department(id)," +
			"  name VARCHAR(100) NOT NULL," +
			"  title VARCHAR(100) NOT NULL" +
			")"
	);

	private DevSchema() {}

	/**
	 * Creates (or recreates) the schema. Uses {@code DROP IF EXISTS} so a second
	 * run of {@code Main seed} starts from a clean slate instead of failing on
	 * duplicate primary keys.
	 */
	public static void create(DataSource dataSource) {
		try (Connection conn = dataSource.getConnection();
			 Statement stmt = conn.createStatement()) {
			for (String ddl : DDL) stmt.execute(ddl);
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to create dev-seed schema", e);
		}
	}

}
