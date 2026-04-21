package org.simulatest.example.ticketing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TicketTierRepository {

	@Inject TicketingDatabase db;

	public void create(int id, int conferenceId, String name, long priceCents, int inventory) {
		db.update(
			"INSERT INTO ticket_tier (id, conference_id, name, price_cents, inventory) VALUES (?, ?, ?, ?, ?)",
			id, conferenceId, name, priceCents, inventory);
	}

	public long priceCentsOf(int tierId) {
		return db.queryLong("SELECT price_cents FROM ticket_tier WHERE id = ?", tierId);
	}

	public int inventoryOf(int tierId) {
		return db.queryInt("SELECT inventory FROM ticket_tier WHERE id = ?", tierId);
	}

	public int count() {
		return db.queryInt("SELECT COUNT(*) FROM ticket_tier");
	}

}
