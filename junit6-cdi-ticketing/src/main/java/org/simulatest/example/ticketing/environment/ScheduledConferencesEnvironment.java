package org.simulatest.example.ticketing.environment;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.simulatest.environment.Environment;
import org.simulatest.environment.annotation.EnvironmentParent;
import org.simulatest.example.ticketing.TicketingDatabase;

/**
 * World-state: <b>conferences are scheduled into venues</b>.
 *
 * <p>Three conferences have committed to the available venues — JavaOne and
 * JVM Summit booked into Austin, KotlinConf into San Francisco. The dates
 * are on the calendar but no tickets exist yet; you couldn't attend even
 * if you wanted to.
 *
 * <p>Parent: {@link BookableVenuesEnvironment}.
 */
@Dependent
@EnvironmentParent(BookableVenuesEnvironment.class)
public class ScheduledConferencesEnvironment implements Environment {

	@Inject TicketingDatabase db;

	@Override
	public void run() {
		db.update("INSERT INTO conference VALUES (?, ?, ?)", 1, 1, "JavaOne");
		db.update("INSERT INTO conference VALUES (?, ?, ?)", 2, 2, "KotlinConf");
		db.update("INSERT INTO conference VALUES (?, ?, ?)", 3, 1, "JVM Summit");
	}

}
