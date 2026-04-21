package org.simulatest.example.banking;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public final class AccountRepository {

	private final BankingDatabase db;

	@Inject
	public AccountRepository(BankingDatabase db) {
		this.db = db;
	}

	public void open(int id, int customerId, String currencyCode, String kind, long openingCents) {
		db.update(
			"INSERT INTO account (id, customer_id, currency_code, kind, balance_cents) VALUES (?, ?, ?, ?, ?)",
			id, customerId, currencyCode, kind, openingCents);
	}

	public long balanceOf(int accountId) {
		return db.queryLong("SELECT balance_cents FROM account WHERE id = ?", accountId);
	}

	public long totalBalance() {
		return db.queryLong("SELECT COALESCE(SUM(balance_cents), 0) FROM account");
	}

	public int count() {
		return db.queryInt("SELECT COUNT(*) FROM account");
	}

	public void adjust(int accountId, long deltaCents) {
		db.update("UPDATE account SET balance_cents = balance_cents + ? WHERE id = ?", deltaCents, accountId);
	}

}
