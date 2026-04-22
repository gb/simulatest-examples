package org.simulatest.example.ticketing;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.simulatest.environment.annotation.UseEnvironment;
import org.simulatest.example.ticketing.environment.OnSaleEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The showpiece test, run at the <b>on sale</b> world-state: a purchase
 * fires a CDI event, the observer decrements inventory AND writes an
 * inventory-log row. Tests below verify the chain, then the two isolation
 * tests at the bottom prove the savepoint covers both the primary write
 * and the observer's side effects.
 */
@Dependent
@UseEnvironment(OnSaleEnvironment.class)
class PurchaseTest {

	private static final int STANDARD_JAVAONE = 1; // 100 seats, $200
	private static final int VIP_KOTLINCONF   = 4; //  20 seats, $750
	private static final int STANDARD_SUMMIT  = 5; // 100 seats, $150

	@Inject PurchaseService purchases;
	@Inject TicketTierRepository tiers;
	@Inject TicketingDatabase db;

	@Test
	void purchaseDecrementsInventoryViaObserver() {
		purchases.buy("alice@example.com", STANDARD_JAVAONE, 1);

		assertEquals(99, tiers.inventoryOf(STANDARD_JAVAONE));
	}

	@Test
	void purchaseWritesPurchaseAndInventoryLogRows() {
		purchases.buy("alice@example.com", STANDARD_JAVAONE, 1);

		assertEquals(1, db.queryInt("SELECT COUNT(*) FROM purchase"));
		assertEquals(1, db.queryInt("SELECT COUNT(*) FROM inventory_log"));
		assertEquals(-1, db.queryInt(
			"SELECT delta FROM inventory_log WHERE tier_id = ?", STANDARD_JAVAONE));
	}

	@Test
	void bulkPurchaseDecrementsByQuantity() {
		purchases.buy("team@example.com", VIP_KOTLINCONF, 5);

		assertEquals(15, tiers.inventoryOf(VIP_KOTLINCONF));
		assertEquals(1, db.queryInt("SELECT COUNT(*) FROM purchase"));
		assertEquals(-5, db.queryInt(
			"SELECT delta FROM inventory_log WHERE tier_id = ?", VIP_KOTLINCONF));
	}

	@Test
	void multiplePurchasesAccumulate() {
		purchases.buy("a@example.com", STANDARD_SUMMIT, 2);
		purchases.buy("b@example.com", STANDARD_SUMMIT, 3);
		purchases.buy("c@example.com", STANDARD_SUMMIT, 1);

		assertEquals(94, tiers.inventoryOf(STANDARD_SUMMIT));
		assertEquals(3, db.queryInt("SELECT COUNT(*) FROM purchase"));
		assertEquals(3, db.queryInt("SELECT COUNT(*) FROM inventory_log"));
		assertEquals(-6, db.queryInt(
			"SELECT SUM(delta) FROM inventory_log WHERE tier_id = ?", STANDARD_SUMMIT));
	}

	@Test
	void purchaseAmountMatchesUnitPriceTimesQuantity() {
		purchases.buy("finance@example.com", VIP_KOTLINCONF, 3);

		assertEquals(225_000L, db.queryLong(
			"SELECT amount_cents FROM purchase WHERE buyer_email = 'finance@example.com'"));
	}

	@Test
	void zeroQuantityIsRejected() {
		assertThrows(TicketingDatabaseException.class, () ->
			purchases.buy("a@example.com", STANDARD_JAVAONE, 0));

		assertEquals(100, tiers.inventoryOf(STANDARD_JAVAONE), "inventory untouched on rejection");
		assertEquals(0, db.queryInt("SELECT COUNT(*) FROM purchase"));
	}

	// =========================================================================
	// Observer-chain isolation. Every purchase above triggered the observer
	// and wrote to TWO tables. These tests confirm the savepoint rolled back
	// both the primary purchase row AND the observer's writes.
	// =========================================================================

	@Test
	void inventoryIsBackToFullBetweenTests() {
		assertEquals(100, tiers.inventoryOf(STANDARD_JAVAONE));
		assertEquals( 20, tiers.inventoryOf(VIP_KOTLINCONF));
		assertEquals(100, tiers.inventoryOf(STANDARD_SUMMIT));
	}

	@Test
	void noPurchaseOrInventoryLogRowsSurvive() {
		assertEquals(0, db.queryInt("SELECT COUNT(*) FROM purchase"));
		assertEquals(0, db.queryInt("SELECT COUNT(*) FROM inventory_log"));
	}

}
