package org.simulatest.example.seeding;

import java.nio.file.Path;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.simulatest.environment.EnvironmentFactory;
import org.simulatest.environment.EnvironmentReflectionFactory;
import org.simulatest.environment.EnvironmentRunner;
import org.simulatest.example.seeding.environment.StaffedOrganizationsEnvironment;
import org.simulatest.insistencelayer.InsistenceLayer;
import org.simulatest.insistencelayer.InsistenceLayerFactory;

/**
 * Two uses of the same Environment tree outside a test runner:
 *
 * <ul>
 *   <li>{@code main seed} — populate a local H2 file with realistic dev data
 *       by running the environment tree against the raw datasource. Data
 *       persists; run the app against the file afterwards.</li>
 *   <li>{@code main whatif} — wrap the tree in a single Insistence Layer
 *       level, run a "risky" operation against the seeded data, print what
 *       happened, then pop the level. Nothing persists. The "prod safety
 *       net" use case, reduced to a runnable script.</li>
 * </ul>
 *
 * The environments don't know which mode is active. They just call
 * {@link DevDatabase#execute} and whatever datasource was installed is used.
 */
public final class Main {

	private static final Path DB_FILE = Path.of("target", "dev-seed").toAbsolutePath();
	private static final String JDBC_URL = "jdbc:h2:file:" + DB_FILE + ";DB_CLOSE_DELAY=-1";

	public static void main(String[] args) {
		String mode = args.length > 0 ? args[0] : "seed";
		switch (mode) {
			case "seed" -> seed();
			case "whatif" -> whatIf();
			default -> {
				System.err.println("Usage: mvn exec:java -Dexec.args=\"seed|whatif\"");
				System.exit(1);
			}
		}
	}

	/**
	 * Seed mode. No Insistence Layer. Each INSERT commits; the H2 file keeps
	 * the data after the JVM exits. Run the app against {@code JDBC_URL}.
	 */
	private static void seed() {
		JdbcDataSource ds = h2File();
		DevSchema.create(ds);

		DevDatabase.use(ds);
		EnvironmentRunner.runEnvironment(StaffedOrganizationsEnvironment.class, factory());

		report("SEEDED", ds);
		System.out.println();
		System.out.println("JDBC URL: " + JDBC_URL);
		System.out.println("User: sa  (no password)");
	}

	/**
	 * What-if mode. Seed, then run a risky operation inside a level, show the
	 * intermediate state, pop the level, show that the books are back.
	 */
	private static void whatIf() {
		JdbcDataSource ds = h2File();
		DevSchema.create(ds);

		// Run the seed phase on the RAW datasource so the base data persists,
		// just like a normal dev-seed run.
		DevDatabase.use(ds);
		EnvironmentRunner.runEnvironment(StaffedOrganizationsEnvironment.class, factory());

		report("BASELINE (persistent)", ds);

		// Switch to the wrapped datasource. From here on, everything lives
		// in a savepoint that will be rolled back.
		InsistenceLayerFactory.configure(ds);
		DataSource wrapped = InsistenceLayerFactory.requireDataSource();
		DevDatabase.use(wrapped);

		InsistenceLayer layer = InsistenceLayerFactory.resolve().orElseThrow();
		layer.increaseLevel();
		try {
			// Risky operation: delete every employee from Harbor Logistics (company 2).
			DevDatabase.execute(
				"DELETE FROM employee WHERE department_id IN (SELECT id FROM department WHERE company_id = 2)");
			DevDatabase.execute("DELETE FROM department WHERE company_id = 2");
			DevDatabase.execute("DELETE FROM company WHERE id = 2");

			report("INSIDE LEVEL (after risky delete)", wrapped);
		} finally {
			layer.decreaseLevel();
		}

		// Read the raw datasource directly; the level rolled everything back,
		// so we're back to the baseline.
		report("AFTER ROLLBACK (raw datasource)", ds);
	}

	private static void report(String title, DataSource ds) {
		System.out.println();
		System.out.println("== " + title + " ==");
		System.out.println("companies:    " + DevDatabase.queryInt(ds, "SELECT COUNT(*) FROM company"));
		System.out.println("departments:  " + DevDatabase.queryInt(ds, "SELECT COUNT(*) FROM department"));
		System.out.println("employees:    " + DevDatabase.queryInt(ds, "SELECT COUNT(*) FROM employee"));
	}

	// Use the built-in reflection factory. Real projects that need DI in their
	// environments would hand in Spring/Guice/CDI factories instead; the
	// runner doesn't care which.
	private static EnvironmentFactory factory() {
		return new EnvironmentReflectionFactory();
	}

	private static JdbcDataSource h2File() {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL(JDBC_URL);
		ds.setUser("sa");
		return ds;
	}

}
