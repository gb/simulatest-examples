package org.simulatest.example.banking.environment;

import com.google.inject.Inject;
import org.simulatest.environment.Environment;
import org.simulatest.environment.annotation.EnvironmentParent;
import org.simulatest.example.banking.AccountRepository;

/**
 * Every customer gets one CHECKING and one SAVINGS USD account, each seeded
 * with $1,000. Six accounts, total book balance $6,000.
 */
@EnvironmentParent(CustomersEnvironment.class)
public final class AccountsEnvironment implements Environment {

	private static final long OPENING_CENTS = 100_000L; // $1,000.00

	private final AccountRepository accounts;

	@Inject
	public AccountsEnvironment(AccountRepository accounts) {
		this.accounts = accounts;
	}

	@Override
	public void run() {
		// Alice
		accounts.open(1, 1, "USD", "CHECKING", OPENING_CENTS);
		accounts.open(2, 1, "USD", "SAVINGS",  OPENING_CENTS);
		// Bob
		accounts.open(3, 2, "USD", "CHECKING", OPENING_CENTS);
		accounts.open(4, 2, "USD", "SAVINGS",  OPENING_CENTS);
		// Carol
		accounts.open(5, 3, "USD", "CHECKING", OPENING_CENTS);
		accounts.open(6, 3, "USD", "SAVINGS",  OPENING_CENTS);
	}

}
