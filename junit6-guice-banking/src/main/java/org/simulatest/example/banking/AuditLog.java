package org.simulatest.example.banking;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Append-only record of every money movement. The invariant that makes this
 * demo shine: for any fully completed transfer, the sum of audit rows equals
 * zero (one debit and one credit of matching magnitude). After a test rolls
 * back, the audit log is empty again — proof that business events and their
 * side effects participate in a single savepoint.
 */
@Singleton
public final class AuditLog {

	private final BankingDatabase db;

	@Inject
	public AuditLog(BankingDatabase db) {
		this.db = db;
	}

	public void record(int accountId, long amountCents, String description) {
		db.update(
			"INSERT INTO audit_log (account_id, amount_cents, description) VALUES (?, ?, ?)",
			accountId, amountCents, description);
	}

	public int rowCount() {
		return db.queryInt("SELECT COUNT(*) FROM audit_log");
	}

	public long netCents() {
		return db.queryLong("SELECT COALESCE(SUM(amount_cents), 0) FROM audit_log");
	}

	public int rowCountFor(int accountId) {
		return db.queryInt("SELECT COUNT(*) FROM audit_log WHERE account_id = ?", accountId);
	}

	public long netFor(int accountId) {
		return db.queryLong(
			"SELECT COALESCE(SUM(amount_cents), 0) FROM audit_log WHERE account_id = ?", accountId);
	}

}
