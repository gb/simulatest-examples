package org.simulatest.example.banking;

import java.util.Collection;

import org.h2.jdbcx.JdbcDataSource;
import org.simulatest.environment.plugin.SimulatestPlugin;
import org.simulatest.insistencelayer.InsistenceLayerFactory;

/**
 * Bootstraps the H2 in-memory bank, wraps it with the Insistence Layer, and
 * creates the schema before any environment runs. Registered via
 * {@code META-INF/services/org.simulatest.environment.plugin.SimulatestPlugin}.
 *
 * <p>This plugin is independent of {@code SimulatestGuicePlugin}: both run
 * during plugin initialization and the order doesn't matter. Guice's
 * auto-configuration is a no-op here because the module doesn't bind
 * {@code DataSource}.
 */
public final class BankingPlugin implements SimulatestPlugin {

	@Override
	public void initialize(Collection<Class<?>> testClasses) {
		JdbcDataSource h2 = new JdbcDataSource();
		h2.setURL("jdbc:h2:mem:banking;DB_CLOSE_DELAY=-1");
		h2.setUser("sa");

		InsistenceLayerFactory.configure(h2);
		BankingSchema.create(InsistenceLayerFactory.requireDataSource());
	}

}
