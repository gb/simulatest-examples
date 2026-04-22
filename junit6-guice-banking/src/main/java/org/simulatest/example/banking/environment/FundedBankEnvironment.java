package org.simulatest.example.banking.environment;

import com.google.inject.Inject;
import org.simulatest.environment.Environment;
import org.simulatest.environment.annotation.EnvironmentParent;
import org.simulatest.example.banking.AccountRepository;
import org.simulatest.example.banking.BankingDatabase;

/**
 * World-state: <b>the bank is open for business, with funds on deposit</b>.
 *
 * <p>Three customers (Alice, Bob, Carol) each hold one USD CHECKING and one
 * USD SAVINGS account, every account seeded with $1,000. Six accounts total,
 * $6,000 of book value, empty audit log. This is a realistic opening-of-day
 * snapshot: the system is ready for transfers to flow.
 *
 * <p>Customer and account seeding live in a single environment because a
 * customer without an account isn't a meaningful state for this domain — the
 * bank only interacts with customers via their accounts. Splitting them would
 * introduce a level no test cares about.
 *
 * <p>Parent: {@link OperatingBankEnvironment} (currencies configured).
 */
@EnvironmentParent(OperatingBankEnvironment.class)
public final class FundedBankEnvironment implements Environment {

	private static final long OPENING_CENTS = 100_000L; // $1,000.00

	private final BankingDatabase db;
	private final AccountRepository accounts;

	@Inject
	public FundedBankEnvironment(BankingDatabase db, AccountRepository accounts) {
		this.db = db;
		this.accounts = accounts;
	}

	@Override
	public void run() {
		// Customers first — accounts will reference them by FK.
		db.update("INSERT INTO customer VALUES (?, ?)", 1, "Alice Thompson");
		db.update("INSERT INTO customer VALUES (?, ?)", 2, "Bob Martinez");
		db.update("INSERT INTO customer VALUES (?, ?)", 3, "Carol Davis");

		// One CHECKING + one SAVINGS per customer, all USD, all seeded with $1,000.
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
