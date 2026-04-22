package org.simulatest.example.ticketing;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.simulatest.environment.annotation.UseEnvironment;
import org.simulatest.example.ticketing.environment.OnSaleEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Dependent
@UseEnvironment(OnSaleEnvironment.class)
class TicketTiersTest {

	@Inject TicketTierRepository tiers;
	@Inject TicketingDatabase db;

	@Test
	void sixTiersExist() {
		assertEquals(6, tiers.count());
	}

	@Test
	void standardTiersHave100Seats() {
		assertEquals(100, tiers.inventoryOf(1));
		assertEquals(100, tiers.inventoryOf(3));
		assertEquals(100, tiers.inventoryOf(5));
	}

	@Test
	void vipTiersHave20Seats() {
		assertEquals(20, tiers.inventoryOf(2));
		assertEquals(20, tiers.inventoryOf(4));
		assertEquals(20, tiers.inventoryOf(6));
	}

	@Test
	void noPurchasesYet() {
		assertEquals(0, db.queryInt("SELECT COUNT(*) FROM purchase"));
		assertEquals(0, db.queryInt("SELECT COUNT(*) FROM inventory_log"));
	}

}
