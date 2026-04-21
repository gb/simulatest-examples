package org.simulatest.example.ticketing.environment;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.simulatest.environment.Environment;
import org.simulatest.example.ticketing.TicketingDatabase;

/**
 * Root environment — two venues.
 *
 * <pre>
 *   VenuesEnvironment         ◄── ROOT
 *     └── ConferencesEnvironment
 *           └── TicketTiersEnvironment
 * </pre>
 */
@Dependent
public class VenuesEnvironment implements Environment {

	@Inject TicketingDatabase db;

	@Override
	public void run() {
		db.update("INSERT INTO venue VALUES (?, ?, ?)", 1, "Convention Center",  "Austin");
		db.update("INSERT INTO venue VALUES (?, ?, ?)", 2, "Grand Auditorium",   "San Francisco");
	}

}
