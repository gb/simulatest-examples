package org.simulatest.example.banking;

import com.google.inject.Inject;
import org.junit.jupiter.api.Test;
import org.simulatest.di.guice.SimulatestGuiceConfig;
import org.simulatest.environment.annotation.UseEnvironment;
import org.simulatest.example.banking.environment.AccountsEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Baseline assertions at the leaf environment: six seeded accounts, $6,000
 * total, an empty audit log. These anchor the invariants that the transfer
 * tests shake.
 */
@SimulatestGuiceConfig(BankingModule.class)
@UseEnvironment(AccountsEnvironment.class)
class AccountsTest {

	@Inject AccountRepository accounts;
	@Inject AuditLog audit;

	@Test
	void sixAccountsExist() {
		assertEquals(6, accounts.count());
	}

	@Test
	void everyAccountStartsAtAThousand() {
		for (int id = 1; id <= 6; id++) {
			assertEquals(100_000L, accounts.balanceOf(id), "account " + id);
		}
	}

	@Test
	void totalBookBalanceIsSixThousand() {
		assertEquals(600_000L, accounts.totalBalance());
	}

	@Test
	void auditLogStartsEmpty() {
		assertEquals(0, audit.rowCount());
		assertEquals(0L, audit.netCents());
	}

}
