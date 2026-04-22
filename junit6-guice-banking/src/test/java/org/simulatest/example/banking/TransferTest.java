package org.simulatest.example.banking;

import com.google.inject.Inject;
import org.junit.jupiter.api.Test;
import org.simulatest.di.guice.SimulatestGuiceConfig;
import org.simulatest.environment.annotation.UseEnvironment;
import org.simulatest.example.banking.environment.FundedBankEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Transfers move money and write two audit rows per transfer. The shiny story:
 * every test below mutates balances and appends audit entries, yet the next
 * test sees the books untouched and the audit log empty. The savepoint stack
 * rolls back the business operation AND its side effects together.
 *
 * <p>Ledger invariant that must hold everywhere: the sum of all audit
 * {@code amount_cents} values equals zero after any number of completed
 * transfers. Siblings to this class never observe a non-zero net.
 */
@SimulatestGuiceConfig(BankingModule.class)
@UseEnvironment(FundedBankEnvironment.class)
class TransferTest {

	// Account IDs seeded by FundedBankEnvironment.
	private static final int ALICE_CHECKING = 1;
	private static final int ALICE_SAVINGS  = 2;
	private static final int BOB_CHECKING   = 3;
	private static final int BOB_SAVINGS    = 4;
	private static final int CAROL_CHECKING = 5;

	@Inject TransferService transfers;
	@Inject AccountRepository accounts;
	@Inject AuditLog audit;

	@Test
	void transferMovesMoneyBetweenAccounts() {
		transfers.transfer(ALICE_CHECKING, BOB_SAVINGS, 10_000L, "rent share");

		assertEquals( 90_000L, accounts.balanceOf(ALICE_CHECKING));
		assertEquals(110_000L, accounts.balanceOf(BOB_SAVINGS));
		assertEquals(600_000L, accounts.totalBalance(), "total book balance is conserved");
	}

	@Test
	void transferWritesMatchingAuditPair() {
		transfers.transfer(ALICE_CHECKING, BOB_SAVINGS, 10_000L, "rent share");

		assertEquals(2, audit.rowCount());
		assertEquals(0L, audit.netCents(), "debit + credit must net to zero");
		assertEquals(-10_000L, audit.netFor(ALICE_CHECKING));
		assertEquals(+10_000L, audit.netFor(BOB_SAVINGS));
	}

	@Test
	void manyTransfersKeepTheLedgerFlat() {
		transfers.transfer(ALICE_CHECKING, BOB_SAVINGS, 5_000L,  "one");
		transfers.transfer(BOB_CHECKING,   ALICE_SAVINGS, 3_000L, "two");
		transfers.transfer(CAROL_CHECKING, ALICE_CHECKING, 1_500L, "three");

		assertEquals(6, audit.rowCount(),      "two audit rows per transfer");
		assertEquals(0L, audit.netCents(),     "net still zero after three transfers");
		assertEquals(600_000L, accounts.totalBalance(), "total book balance is conserved");
	}

	@Test
	void selfTransferRoundTripsToSameBalance() {
		long before = accounts.balanceOf(ALICE_CHECKING);

		transfers.transfer(ALICE_CHECKING, ALICE_SAVINGS, 2_500L, "move to savings");
		transfers.transfer(ALICE_SAVINGS, ALICE_CHECKING, 2_500L, "move back");

		assertEquals(before, accounts.balanceOf(ALICE_CHECKING));
		assertEquals(4, audit.rowCount());
		assertEquals(0L, audit.netCents());
	}

	@Test
	void negativeAmountIsRejected() {
		assertThrows(BankingDatabaseException.class, () ->
			transfers.transfer(ALICE_CHECKING, BOB_SAVINGS, -1L, "nope"));
	}

	// =========================================================================
	// Isolation crown jewels. Each of these MUST see the funded-bank
	// baseline — untouched balances, empty audit log — even though the tests
	// above moved money and appended audit rows.
	// =========================================================================

	@Test
	void allBalancesAreBackToAThousand() {
		for (int id = 1; id <= 6; id++) {
			assertEquals(100_000L, accounts.balanceOf(id), "account " + id);
		}
	}

	@Test
	void auditLogIsEmptyAgainBetweenTests() {
		assertEquals(0, audit.rowCount());
		assertEquals(0L, audit.netCents());
	}

}
