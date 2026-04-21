package org.simulatest.example.banking;

import com.google.inject.AbstractModule;

/**
 * All the bindings in this demo come from {@code @Inject} constructors marked
 * with {@code @Singleton}, so Guice resolves everything automatically. The
 * {@code DataSource} itself is NOT bound in Guice; services reach it through
 * {@link BankingDatabase}, which pulls from the Insistence Layer's shared
 * wrapped datasource. That keeps this module order-independent with respect
 * to the Simulatest Guice plugin.
 */
public final class BankingModule extends AbstractModule {

	@Override
	protected void configure() {
		// Intentionally empty. See class comment.
	}

}
