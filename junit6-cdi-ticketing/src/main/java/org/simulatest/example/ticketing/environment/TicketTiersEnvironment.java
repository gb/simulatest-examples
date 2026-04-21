package org.simulatest.example.ticketing.environment;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.simulatest.environment.Environment;
import org.simulatest.environment.annotation.EnvironmentParent;
import org.simulatest.example.ticketing.TicketTierRepository;

/**
 * Each of the 3 conferences gets STANDARD (100 seats) and VIP (20 seats) tiers.
 * Six tiers total: IDs 1..6 in conference order.
 */
@Dependent
@EnvironmentParent(ConferencesEnvironment.class)
public class TicketTiersEnvironment implements Environment {

	@Inject TicketTierRepository tiers;

	@Override
	public void run() {
		// JavaOne (conference 1)
		tiers.create(1, 1, "STANDARD",  20_000L,  100);
		tiers.create(2, 1, "VIP",       60_000L,   20);
		// KotlinConf (conference 2)
		tiers.create(3, 2, "STANDARD",  25_000L,  100);
		tiers.create(4, 2, "VIP",       75_000L,   20);
		// JVM Summit (conference 3)
		tiers.create(5, 3, "STANDARD",  15_000L,  100);
		tiers.create(6, 3, "VIP",       50_000L,   20);
	}

}
