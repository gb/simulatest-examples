package org.simulatest.example.seeding.environment;

import org.simulatest.environment.Environment;
import org.simulatest.environment.annotation.EnvironmentParent;
import org.simulatest.example.seeding.DevDatabase;

@EnvironmentParent(CompanyEnvironment.class)
public final class DepartmentEnvironment implements Environment {

	@Override
	public void run() {
		DevDatabase.execute("INSERT INTO department (id, company_id, name) VALUES (?, ?, ?)", 1, 1, "Platform");
		DevDatabase.execute("INSERT INTO department (id, company_id, name) VALUES (?, ?, ?)", 2, 1, "Sales");
		DevDatabase.execute("INSERT INTO department (id, company_id, name) VALUES (?, ?, ?)", 3, 2, "Operations");
		DevDatabase.execute("INSERT INTO department (id, company_id, name) VALUES (?, ?, ?)", 4, 2, "Finance");
	}

}
