package org.simulatest.example.library;

import java.util.Collection;

import org.postgresql.ds.PGSimpleDataSource;
import org.simulatest.environment.plugin.SimulatestPlugin;
import org.simulatest.insistencelayer.InsistenceLayerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Bootstraps a real PostgreSQL 16 instance via Testcontainers, then wires its
 * {@code DataSource} into the Insistence Layer. Schema creation happens before
 * the environment tree runs — DDL implicitly commits and would invalidate any
 * savepoint pushed earlier.
 *
 * <p>The container is shared JVM-wide so Postgres startup is paid once per
 * suite. {@code withReuse(true)} lets a hot Docker daemon keep the container
 * alive between runs when {@code testcontainers.reuse.enable=true} is set in
 * {@code ~/.testcontainers.properties}.
 */
public final class LibraryPlugin implements SimulatestPlugin {

	// Pinning a minor version keeps schemas deterministic. Bump deliberately.
	private static final DockerImageName POSTGRES_IMAGE =
		DockerImageName.parse("postgres:16.4-alpine");

	private static final PostgreSQLContainer<?> POSTGRES =
		new PostgreSQLContainer<>(POSTGRES_IMAGE)
			.withDatabaseName("library")
			.withUsername("library")
			.withPassword("library")
			.withReuse(true);

	static {
		POSTGRES.start();
	}

	@Override
	public void initialize(Collection<Class<?>> testClasses) {
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setUrl(POSTGRES.getJdbcUrl());
		dataSource.setUser(POSTGRES.getUsername());
		dataSource.setPassword(POSTGRES.getPassword());

		InsistenceLayerFactory.configure(dataSource);
		LibraryDatabase.createSchema();
	}

}
