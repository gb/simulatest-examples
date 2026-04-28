package org.simulatest.example.banking;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.simulatest.environment.bootstrap.SimulatestDatabaseSetup;

/**
 * Bootstraps the banking H2 database and creates its schema. Discovered via
 * {@link java.util.ServiceLoader} from
 * {@code META-INF/services/org.simulatest.environment.bootstrap.SimulatestDatabaseSetup}.
 *
 * <p>Coexists with {@code SimulatestGuicePlugin}: this class supplies the
 * {@code DataSource} (the Guice module doesn't bind one), then the Guice
 * plugin wires it through for {@code @Inject} consumers.
 */
public final class BankingDatabaseSetup implements SimulatestDatabaseSetup {

	@Override
	public DataSource dataSource() {
		JdbcDataSource h2 = new JdbcDataSource();
		h2.setURL("jdbc:h2:mem:banking;DB_CLOSE_DELAY=-1");
		h2.setUser("sa");
		return h2;
	}

	@Override
	public void setupSchema(DataSource dataSource) {
		BankingSchema.create(dataSource);
	}

}
