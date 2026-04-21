package org.simulatest.example.ticketing.environment;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.simulatest.environment.Environment;
import org.simulatest.environment.annotation.EnvironmentParent;
import org.simulatest.example.ticketing.TicketingDatabase;

@Dependent
@EnvironmentParent(VenuesEnvironment.class)
public class ConferencesEnvironment implements Environment {

	@Inject TicketingDatabase db;

	@Override
	public void run() {
		db.update("INSERT INTO conference VALUES (?, ?, ?)", 1, 1, "JavaOne");
		db.update("INSERT INTO conference VALUES (?, ?, ?)", 2, 2, "KotlinConf");
		db.update("INSERT INTO conference VALUES (?, ?, ?)", 3, 1, "JVM Summit");
	}

}
