# junit6-guice-banking

A tiny bank with customers, accounts, transfers, and a per-transfer audit trail. Built with Guice-managed services and designed to make one thing obvious: **a business operation and its side effects roll back together.** Every test can move money freely, and the next test sees the ledger flat.

## Stack

Java 17 · JUnit 6 · Guice 7 · H2 · raw JDBC.

## What it shows

- **Guice DI inside environments and tests.** `FundedBankEnvironment` takes a `BankingDatabase` and an `AccountRepository` by constructor injection; tests inject `TransferService`, `AccountRepository`, and `AuditLog`. Nothing exotic, just standard Guice.
- **The audit-log invariant.** Every transfer writes a matched pair of audit rows: a negative entry on the source and a positive entry on the destination. The sum of `audit_log.amount_cents` over all rows is always zero. No triggers, no stored procedures; the guarantee is the `TransferService` code plus the savepoint wrap.
- **Rollback covers side effects.** `TransferTest.manyTransfersKeepTheLedgerFlat` posts three transfers and writes six audit rows; `allBalancesAreBackToAThousand` and `auditLogIsEmptyAgainBetweenTests` run afterward and see the baseline. That's the demo's shiny moment.
- **Order-independent SPI wiring.** `BankingDatabaseSetup` returns the H2 `DataSource` and creates the schema; `BankingModule` binds nothing related to the `DataSource`. Services reach the wrapped connection via `BankingDatabase`, which means `SimulatestGuicePlugin` and the database-setup bootstrap can load in either order without fighting.

## Environment tree — a sequence of world-states

```
OperatingBankEnvironment           bank open: USD/EUR/BRL configured, no customers yet
  └── FundedBankEnvironment        open for business: 3 customers × 2 accounts × $1000 each
```

Only two levels, because there are only two useful bank states for this
domain. A customer without an account isn't a meaningful world-state — the
system only interacts with customers through their accounts. So
`FundedBankEnvironment` does the whole thing in one step: onboard the
customers, open their accounts, seed the balances.

## Run

```bash
# From the repo root
mvn -pl junit6-guice-banking -am verify

# Or from this folder
mvn verify
```

## Key files

| File | What to read it for |
|---|---|
| [`TransferService.java`](src/main/java/org/simulatest/example/banking/TransferService.java) | The four writes (debit, credit, audit×2) that all ride the same savepoint. |
| [`AuditLog.java`](src/main/java/org/simulatest/example/banking/AuditLog.java) | Tiny repository; shows the ledger invariant as a couple of SUM queries. |
| [`BankingDatabaseSetup.java`](src/test/java/org/simulatest/example/banking/BankingDatabaseSetup.java) | Hands the H2 `DataSource` to Simulatest via the `SimulatestDatabaseSetup` SPI; installs the schema in `setupSchema(...)`. |
| [`TransferTest.java`](src/test/java/org/simulatest/example/banking/TransferTest.java) | The rollback-covers-the-audit-log story. The two isolation tests at the bottom are the payoff. |
| [`BankingModule.java`](src/test/java/org/simulatest/example/banking/BankingModule.java) | Explicitly empty. Why it must stay that way is in its class comment. |

## Why `BankingModule` doesn't bind `DataSource`

The tempting pattern is to `bind(DataSource.class).toInstance(h2)`. It technically works for Spring (because the Spring plugin installs a `BeanPostProcessor` that swaps the bean for the Insistence-Layer-wrapped version), but Guice has no equivalent hook. If you bind the raw `DataSource`, Guice-injected services bypass the savepoint stack and tests stop isolating.

The workaround here is a thin `BankingDatabase` wrapper that always pulls from `InsistenceLayerFactory.requireDataSource()`. Services inject `BankingDatabase`, not `DataSource`, so the wrap is never sidestepped. Simple and order-independent.
