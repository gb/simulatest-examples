package org.simulatest.example.ticketing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * Reacts to every {@link PurchaseEvent} by decrementing the tier's inventory
 * and writing an inventory-movement audit row. Fires synchronously from the
 * same thread that sold the ticket, so the work lives in the same savepoint.
 */
@ApplicationScoped
public class InventoryObserver {

	@Inject TicketingDatabase db;

	public void onPurchase(@Observes PurchaseEvent event) {
		db.update(
			"UPDATE ticket_tier SET inventory = inventory - ? WHERE id = ?",
			event.quantity(), event.tierId());

		db.update(
			"INSERT INTO inventory_log (tier_id, delta, reason) VALUES (?, ?, ?)",
			event.tierId(), -event.quantity(), "purchase");
	}

}
