package org.simulatest.example.banking.environment;

import com.google.inject.Inject;
import org.simulatest.environment.Environment;
import org.simulatest.example.banking.BankingDatabase;

/**
 * World-state: <b>the bank is operating, but has no customers yet</b>.
 *
 * <p>Three supported currencies (USD, EUR, BRL) are configured — the ledger
 * knows what money is, the chart of accounts is ready to reference them.
 * No one has walked in the door to open an account yet.
 *
 * <pre>
 *   OperatingBankEnvironment   ◄── ROOT (bank open, no customers)
 *     └── FundedBankEnvironment         (customers onboarded, accounts seeded $1000)
 * </pre>
 *
 * <p>Why there's no intermediate "customers exist, accounts don't" state: a
 * customer without an account is not a useful world-state for a banking app —
 * every interaction the system cares about is mediated by an account. Tests
 * that need a richer baseline jump straight from here to fully funded accounts.
 */
public final class OperatingBankEnvironment implements Environment {

	private final BankingDatabase db;

	@Inject
	public OperatingBankEnvironment(BankingDatabase db) {
		this.db = db;
	}

	@Override
	public void run() {
		db.update("INSERT INTO currency VALUES (?, ?)", "USD", "US Dollar");
		db.update("INSERT INTO currency VALUES (?, ?)", "EUR", "Euro");
		db.update("INSERT INTO currency VALUES (?, ?)", "BRL", "Brazilian Real");
	}

}
