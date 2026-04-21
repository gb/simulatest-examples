package org.simulatest.example.banking;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Moves money between two accounts and writes a symmetric pair of audit rows.
 *
 * <p>The four writes below all land on the same JDBC connection (the one wrapped
 * by the Insistence Layer), so they all live under the same savepoint. A test
 * rollback undoes the balance change and the audit trail together; siblings
 * see the books as though nothing happened.
 */
@Singleton
public final class TransferService {

	private final AccountRepository accounts;
	private final AuditLog audit;

	@Inject
	public TransferService(AccountRepository accounts, AuditLog audit) {
		this.accounts = accounts;
		this.audit = audit;
	}

	public void transfer(int fromAccountId, int toAccountId, long amountCents, String description) {
		if (amountCents <= 0) {
			throw new BankingDatabaseException("transfer amount must be positive: " + amountCents);
		}

		accounts.adjust(fromAccountId, -amountCents);
		accounts.adjust(toAccountId,   +amountCents);
		audit.record(fromAccountId, -amountCents, "debit: "  + description);
		audit.record(toAccountId,   +amountCents, "credit: " + description);
	}

}
