package org.simulatest.example.seeding.environment;

import org.simulatest.environment.Environment;
import org.simulatest.environment.annotation.EnvironmentParent;
import org.simulatest.example.seeding.DevDatabase;

/**
 * World-state: <b>companies have an organizational structure</b>.
 *
 * <p>Each company's org chart is drawn: Nimbus Cloud has Platform and Sales
 * departments; Harbor Logistics has Operations and Finance. Four departments
 * total, wired to their owning companies by FK — but no one has been hired
 * into any of them yet.
 *
 * <p>Parent: {@link IncorporatedCompaniesEnvironment}.
 */
@EnvironmentParent(IncorporatedCompaniesEnvironment.class)
public final class OrganizedCompaniesEnvironment implements Environment {

	@Override
	public void run() {
		DevDatabase.execute("INSERT INTO department (id, company_id, name) VALUES (?, ?, ?)", 1, 1, "Platform");
		DevDatabase.execute("INSERT INTO department (id, company_id, name) VALUES (?, ?, ?)", 2, 1, "Sales");
		DevDatabase.execute("INSERT INTO department (id, company_id, name) VALUES (?, ?, ?)", 3, 2, "Operations");
		DevDatabase.execute("INSERT INTO department (id, company_id, name) VALUES (?, ?, ?)", 4, 2, "Finance");
	}

}
