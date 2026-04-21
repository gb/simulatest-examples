package org.simulatest.example.ticketing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

/**
 * Sells tickets. Writes a row to the {@code purchase} table, then fires a
 * synchronous CDI event. The observer chain that reacts is
 * {@link InventoryObserver}: it decrements {@code ticket_tier.inventory} and
 * appends to {@code inventory_log}. All four writes happen on the same JDBC
 * connection, so the Insistence Layer's savepoint covers them as one unit.
 */
@ApplicationScoped
public class PurchaseService {

	@Inject TicketingDatabase db;
	@Inject TicketTierRepository tiers;
	@Inject Event<PurchaseEvent> purchaseEvents;

	public void buy(String buyerEmail, int tierId, int quantity) {
		if (quantity <= 0) {
			throw new TicketingDatabaseException("quantity must be positive: " + quantity);
		}

		long unitPrice = tiers.priceCentsOf(tierId);
		long amountCents = unitPrice * quantity;

		db.update(
			"INSERT INTO purchase (tier_id, buyer_email, quantity, amount_cents) VALUES (?, ?, ?, ?)",
			tierId, buyerEmail, quantity, amountCents);

		purchaseEvents.fire(new PurchaseEvent(tierId, quantity));
	}

}
