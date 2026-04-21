package org.simulatest.example.ticketing;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

public final class TicketingSchema {

	private static final List<String> DDL = List.of(
		"CREATE TABLE IF NOT EXISTS venue (" +
			"  id INT PRIMARY KEY," +
			"  name VARCHAR(100) NOT NULL," +
			"  city VARCHAR(100) NOT NULL" +
			")",
		"CREATE TABLE IF NOT EXISTS conference (" +
			"  id INT PRIMARY KEY," +
			"  venue_id INT NOT NULL REFERENCES venue(id)," +
			"  name VARCHAR(200) NOT NULL" +
			")",
		"CREATE TABLE IF NOT EXISTS ticket_tier (" +
			"  id INT PRIMARY KEY," +
			"  conference_id INT NOT NULL REFERENCES conference(id)," +
			"  name VARCHAR(50) NOT NULL," +
			"  price_cents BIGINT NOT NULL," +
			"  inventory INT NOT NULL" +
			")",
		"CREATE TABLE IF NOT EXISTS purchase (" +
			"  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
			"  tier_id INT NOT NULL REFERENCES ticket_tier(id)," +
			"  buyer_email VARCHAR(200) NOT NULL," +
			"  quantity INT NOT NULL," +
			"  amount_cents BIGINT NOT NULL" +
			")",
		"CREATE TABLE IF NOT EXISTS inventory_log (" +
			"  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
			"  tier_id INT NOT NULL REFERENCES ticket_tier(id)," +
			"  delta INT NOT NULL," +
			"  reason VARCHAR(50) NOT NULL" +
			")"
	);

	private TicketingSchema() {}

	public static void create(DataSource dataSource) {
		try (Connection conn = dataSource.getConnection();
			 Statement stmt = conn.createStatement()) {
			for (String ddl : DDL) stmt.execute(ddl);
		} catch (SQLException e) {
			throw new TicketingDatabaseException("Failed to create ticketing schema", e);
		}
	}

}
