package org.simulatest.example.seeding.environment;

import org.simulatest.environment.Environment;
import org.simulatest.example.seeding.DevDatabase;

/**
 * World-state: <b>companies are incorporated</b>.
 *
 * <p>Two companies are on the books — Nimbus Cloud and Harbor Logistics —
 * registered legal entities with nothing else yet. No org chart, no staff.
 * Useful as a dev seed when you want to start your local app with valid
 * tenants but no organizational data getting in the way.
 *
 * <pre>
 *   IncorporatedCompaniesEnvironment   ◄── ROOT (legal entities exist, nothing else)
 *     └── OrganizedCompaniesEnvironment         (org chart drawn up)
 *           └── StaffedOrganizationsEnvironment (people filling roles)
 * </pre>
 *
 * <p>The same classes are used by tests in real projects; here they're reused
 * from a plain {@code main()} via {@link org.simulatest.environment.EnvironmentRunner}.
 */
public final class IncorporatedCompaniesEnvironment implements Environment {

	@Override
	public void run() {
		DevDatabase.execute("INSERT INTO company (id, name) VALUES (?, ?)", 1, "Nimbus Cloud");
		DevDatabase.execute("INSERT INTO company (id, name) VALUES (?, ?)", 2, "Harbor Logistics");
	}

}
