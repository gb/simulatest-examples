package org.simulatest.example.seeding.environment;

import org.simulatest.environment.Environment;
import org.simulatest.environment.annotation.EnvironmentParent;
import org.simulatest.example.seeding.DevDatabase;

@EnvironmentParent(DepartmentEnvironment.class)
public final class EmployeeEnvironment implements Environment {

	@Override
	public void run() {
		// Platform
		insert(1, 1, "Ada Lovelace",     "Principal Engineer");
		insert(2, 1, "Linus Carver",     "Staff Engineer");
		insert(3, 1, "Rina Okafor",      "Senior Engineer");
		// Sales
		insert(4, 2, "Marcus Hale",      "Sales Director");
		insert(5, 2, "Priya Vasquez",    "Account Executive");
		// Operations
		insert(6, 3, "Tomasz Kowalski",  "Ops Manager");
		insert(7, 3, "Amina Njoku",      "Logistics Lead");
		insert(8, 3, "Yuki Takahashi",   "Warehouse Supervisor");
		// Finance
		insert(9,  4, "Elena Rojas",     "Finance Director");
		insert(10, 4, "Jonas Weber",     "Controller");
	}

	private void insert(int id, int departmentId, String name, String title) {
		DevDatabase.execute(
			"INSERT INTO employee (id, department_id, name, title) VALUES (?, ?, ?, ?)",
			id, departmentId, name, title);
	}

}
