package org.simulatest.example.seeding.environment;

import org.simulatest.environment.Environment;
import org.simulatest.example.seeding.DevDatabase;

/**
 * Root environment. Inserts two companies.
 *
 * <pre>
 *   CompanyEnvironment          ◄── ROOT
 *     └── DepartmentEnvironment
 *           └── EmployeeEnvironment
 * </pre>
 *
 * The same classes are used by tests in real projects; here they're reused
 * from a plain {@code main()} via {@link org.simulatest.environment.EnvironmentRunner}.
 */
public final class CompanyEnvironment implements Environment {

	@Override
	public void run() {
		DevDatabase.execute("INSERT INTO company (id, name) VALUES (?, ?)", 1, "Nimbus Cloud");
		DevDatabase.execute("INSERT INTO company (id, name) VALUES (?, ?)", 2, "Harbor Logistics");
	}

}
