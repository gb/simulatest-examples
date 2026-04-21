# junit6-postgres-testcontainers

The same library suite as [`junit6-library`](../junit6-library), running against **real PostgreSQL 16** via [Testcontainers](https://testcontainers.com/). Proof that the Insistence Layer's savepoint mechanics hold on a production-grade engine, not just on H2.

## Stack

Java 17 · JUnit 6 · PostgreSQL 16 · Testcontainers · raw JDBC.

## Why this demo matters

H2 is generous about a lot of things PostgreSQL enforces strictly: transaction isolation, FK cascade timing, savepoint semantics on DDL. If the savepoint stack works here, it'll work on your production database.

## What it shows

- **Savepoints on real Postgres.** Every `increaseLevel()` maps to a `SAVEPOINT`, every `decreaseLevel()` to a `ROLLBACK TO SAVEPOINT`. The library's 100+ assertions ride entirely on that being correct.
- **FK-constraint survival.** Unlike H2, Postgres aborts the whole transaction on any SQL error ("current transaction is aborted, commands ignored"). `LibraryDatabase.execute` pushes a nested savepoint per statement so a constraint violation only rolls back that one statement, not the enclosing Insistence Layer savepoint. The `deletingBranchWithCopiesFails`-style tests prove the outer savepoint survives.
- **Delete-and-reinsert with the same PK.** Postgres enforces PK uniqueness at statement level; the savepoint restores the original row at rollback. Tricky on any DB, nailed here.
- **Testcontainers wiring.** `LibraryPlugin` spins up a container with `withReuse(true)` so local runs after the first are near-instant. Schema is created once, before any savepoint.

## Prerequisites

A running Docker daemon on the host. `mvn verify` from the repo root picks this module up automatically when Docker is available; pass `-DskipDocker` to skip it.

Optional: enable Testcontainers reuse for snappier repeat runs:

```properties
# ~/.testcontainers.properties
testcontainers.reuse.enable=true
```

## Run

```bash
# From the repo root
mvn -pl junit6-postgres-testcontainers -am verify

# Or from this folder
mvn verify
```

First run pulls `postgres:16.4-alpine` (~80 MB). Subsequent runs with reuse enabled start in under a second.

## Porting notes (vs. junit6-library)

Only two things change vs. the H2 version:

1. **`LibraryPlugin`** — swap the in-memory H2 `DataSource` for a Testcontainers-managed `PGSimpleDataSource`. That's the whole wiring difference.
2. **Date arithmetic** — H2 uses `DATEADD('DAY', N, CURRENT_DATE)`; Postgres uses `CURRENT_DATE + N`. One find-and-replace across `LoansEnvironment` and `LoanTest`.

Schema DDL, all environments, and all other tests are byte-for-byte identical to the H2 demo.

## Key files

| File | What to read it for |
|---|---|
| [`LibraryPlugin.java`](src/test/java/org/simulatest/example/library/LibraryPlugin.java) | Testcontainers wiring: container, datasource, schema. |
| [`LoansEnvironment.java`](src/main/java/org/simulatest/example/library/environment/LoansEnvironment.java) | The one environment whose SQL had to change for Postgres. |
| [`LoanTest.java`](src/test/java/org/simulatest/example/library/LoanTest.java) | Full-tree savepoint tests running on a real engine. |
