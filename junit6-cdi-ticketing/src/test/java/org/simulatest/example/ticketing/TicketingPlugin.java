package org.simulatest.example.ticketing;

import java.util.Collection;

import org.h2.jdbcx.JdbcDataSource;
import org.simulatest.environment.plugin.SimulatestPlugin;
import org.simulatest.insistencelayer.InsistenceLayerFactory;

/**
 * Bootstraps H2, wraps it with the Insistence Layer, creates the ticketing
 * schema. Runs alongside {@code SimulatestJakartaPlugin}; order doesn't matter
 * because CDI isn't the one providing the {@code DataSource}.
 */
public final class TicketingPlugin implements SimulatestPlugin {

	@Override
	public void initialize(Collection<Class<?>> testClasses) {
		JdbcDataSource h2 = new JdbcDataSource();
		h2.setURL("jdbc:h2:mem:ticketing;DB_CLOSE_DELAY=-1");
		h2.setUser("sa");

		InsistenceLayerFactory.configure(h2);
		TicketingSchema.create(InsistenceLayerFactory.requireDataSource());
	}

}
