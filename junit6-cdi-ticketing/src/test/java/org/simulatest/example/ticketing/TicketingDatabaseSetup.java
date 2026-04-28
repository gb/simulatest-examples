package org.simulatest.example.ticketing;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.simulatest.environment.bootstrap.SimulatestDatabaseSetup;

/**
 * Bootstraps the ticketing H2 database and creates its schema. Discovered via
 * {@link java.util.ServiceLoader} from
 * {@code META-INF/services/org.simulatest.environment.bootstrap.SimulatestDatabaseSetup}.
 *
 * <p>Coexists with {@code SimulatestJakartaPlugin}: this class supplies the
 * {@code DataSource} (Weld doesn't bind one), then the CDI plugin wires it
 * into the Insistence Layer for {@code @Inject} consumers.
 */
public final class TicketingDatabaseSetup implements SimulatestDatabaseSetup {

	@Override
	public DataSource dataSource() {
		JdbcDataSource h2 = new JdbcDataSource();
		h2.setURL("jdbc:h2:mem:ticketing;DB_CLOSE_DELAY=-1");
		h2.setUser("sa");
		return h2;
	}

	@Override
	public void setupSchema(DataSource dataSource) {
		TicketingSchema.create(dataSource);
	}

}
