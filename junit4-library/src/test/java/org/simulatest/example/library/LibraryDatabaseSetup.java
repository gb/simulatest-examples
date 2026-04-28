package org.simulatest.example.library;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.simulatest.environment.bootstrap.SimulatestDatabaseSetup;

/**
 * Tells Simulatest where the library database lives and how to create its
 * schema. Discovered via {@link java.util.ServiceLoader} from
 * {@code META-INF/services/org.simulatest.environment.bootstrap.SimulatestDatabaseSetup}.
 */
public class LibraryDatabaseSetup implements SimulatestDatabaseSetup {

	@Override
	public DataSource dataSource() {
		JdbcDataSource h2 = new JdbcDataSource();
		h2.setURL("jdbc:h2:mem:library;DB_CLOSE_DELAY=-1");
		h2.setUser("sa");
		return h2;
	}

	@Override
	public void setupSchema(DataSource dataSource) {
		LibraryDatabase.createSchema(dataSource);
	}

}
