package org.simulatest.example.ticketing.environment;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.simulatest.environment.Environment;
import org.simulatest.example.ticketing.TicketingDatabase;

/**
 * World-state: <b>bookable venues are on the roster</b>.
 *
 * <p>Two physical venues exist and are ready to host events — the Austin
 * Convention Center and the San Francisco Grand Auditorium. Nothing is
 * scheduled yet. This is the inventory state an events agency would list
 * before any conference commits to a date.
 *
 * <pre>
 *   BookableVenuesEnvironment        ◄── ROOT (venues exist, unscheduled)
 *     └── ScheduledConferencesEnvironment    (conferences booked into venues)
 *           └── OnSaleEnvironment            (tiers published, tickets purchasable)
 * </pre>
 */
@Dependent
public class BookableVenuesEnvironment implements Environment {

	@Inject TicketingDatabase db;

	@Override
	public void run() {
		db.update("INSERT INTO venue VALUES (?, ?, ?)", 1, "Convention Center",  "Austin");
		db.update("INSERT INTO venue VALUES (?, ?, ?)", 2, "Grand Auditorium",   "San Francisco");
	}

}
