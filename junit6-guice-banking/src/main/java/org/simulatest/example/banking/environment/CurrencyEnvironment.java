package org.simulatest.example.banking.environment;

import com.google.inject.Inject;
import org.simulatest.environment.Environment;
import org.simulatest.example.banking.BankingDatabase;

/**
 * Root environment — seeds three currencies.
 *
 * <pre>
 *   CurrencyEnvironment       ◄── ROOT (USD, EUR, BRL)
 *     └── CustomersEnvironment
 *           └── AccountsEnvironment
 * </pre>
 */
public final class CurrencyEnvironment implements Environment {

	private final BankingDatabase db;

	@Inject
	public CurrencyEnvironment(BankingDatabase db) {
		this.db = db;
	}

	@Override
	public void run() {
		db.update("INSERT INTO currency VALUES (?, ?)", "USD", "US Dollar");
		db.update("INSERT INTO currency VALUES (?, ?)", "EUR", "Euro");
		db.update("INSERT INTO currency VALUES (?, ?)", "BRL", "Brazilian Real");
	}

}
