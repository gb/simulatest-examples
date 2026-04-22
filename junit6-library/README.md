# junit6-library

A plain-Java demo built around a tiny library domain (books, members, branches, loans). It's the shortest path to seeing the three things Simulatest actually does: an environment tree, the Insistence Layer savepoint stack, and the JUnit 6 TestEngine.

## Stack

Java 17 · JUnit 6 · H2 · raw JDBC. No Spring, no ORM, nothing else.

## What it shows

- **Environment tree.** Parent-child setup composed via `@EnvironmentParent`. Reference data is inserted once and every subtree trusts it exists.
- **Insistence Layer isolation.** Sibling environments never see each other's data; every test starts clean via `resetCurrentLevel()`. No `@After`, no `TRUNCATE`.
- **JUnit Platform TestEngine.** Tests run with plain `@Test` annotations; the Simulatest engine is auto-discovered via `ServiceLoader`.
- **Plugin bootstrap.** `LibraryPlugin` configures H2 and creates the schema before the tree starts running.

## Environment tree — a sequence of world-states

Each node is a complete state of the library at a moment in its life — not a
dump of one database table. Names describe what the world looks like at that
state, not what rows got inserted.

```
ReferenceDataEnvironment          chartered: genres & tiers defined, nothing physical yet
  └── OpenLibraryEnvironment      doors open: 3 branches exist at real addresses
        ├── StockedLibraryEnvironment     shelves full: 10 books, 18 copies across branches
        │     └── LendingLibraryEnvironment    ready to lend: 8 members enrolled, no loans yet
        │           └── ActiveCirculationEnvironment    5 loans out (1 overdue), 2 holds queued
        └── StaffedLibraryEnvironment     staffed building: 7 employees on duty, no stock
```

Sibling world-states (stocked vs. staffed) are independent next-states of an
open library: a library can be stocked without staff or staffed without stock.
The Insistence Layer rolls back each sibling subtree before the next one runs.

## Run

```bash
# From the repo root
mvn -pl junit6-library -am verify

# Or from this folder
mvn verify
```

## Key files

| File | What to read it for |
|---|---|
| [`LibraryPlugin.java`](src/test/java/org/simulatest/example/library/LibraryPlugin.java) | How to bootstrap a `DataSource` into the Insistence Layer. |
| [`ReferenceDataEnvironment.java`](src/main/java/org/simulatest/example/library/environment/ReferenceDataEnvironment.java) | A root environment. |
| [`StockedLibraryEnvironment.java`](src/main/java/org/simulatest/example/library/environment/StockedLibraryEnvironment.java) | An environment that trusts its parent. |
| [`LoanTest.java`](src/test/java/org/simulatest/example/library/LoanTest.java) | How a test picks the environment it needs via `@UseEnvironment`. |
| [`META-INF/services/...SimulatestPlugin`](src/test/resources/META-INF/services/org.simulatest.environment.plugin.SimulatestPlugin) | The `ServiceLoader` hook that registers the plugin. |
