package org.simulatest.example.library;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;
import org.simulatest.environment.bootstrap.SimulatestDatabaseSetup;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Bootstraps a real PostgreSQL 16 instance via Testcontainers, hands its
 * {@code DataSource} to Simulatest, and creates the library schema.
 * Discovered via {@link java.util.ServiceLoader} from
 * {@code META-INF/services/org.simulatest.environment.bootstrap.SimulatestDatabaseSetup}.
 *
 * <p>The container is shared JVM-wide so Postgres startup is paid once per
 * suite. {@code withReuse(true)} lets a hot Docker daemon keep the container
 * alive between runs when {@code testcontainers.reuse.enable=true} is set in
 * {@code ~/.testcontainers.properties}.
 */
public final class LibraryDatabaseSetup implements SimulatestDatabaseSetup {

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
	public DataSource dataSource() {
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setUrl(POSTGRES.getJdbcUrl());
		dataSource.setUser(POSTGRES.getUsername());
		dataSource.setPassword(POSTGRES.getPassword());
		return dataSource;
	}

	@Override
	public void setupSchema(DataSource dataSource) {
		LibraryDatabase.createSchema(dataSource);
	}

}
