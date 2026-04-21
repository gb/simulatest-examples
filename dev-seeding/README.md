# dev-seeding

Simulatest Environments are usually thought of as a testing tool. This demo shows them off the test path entirely: the same classes you'd write for test fixtures are used here by a plain `main()` to seed a local dev database, and then again to run a "what-if" experiment that rolls itself back.

## Stack

Java 17 · Simulatest Environments + Insistence Layer · H2 (file) · JUnit 6 (smoke tests only).

## Why this demo exists

The README at the top of the repo promises three use cases for the Insistence Layer: test isolation, dev seeding, and a prod safety net. The first is covered by every other demo. This one covers the other two in about 200 lines.

## Two modes

### `main seed`

Runs `CompanyEnvironment` → `DepartmentEnvironment` → `EmployeeEnvironment` against a raw H2 file datasource. No Insistence Layer. Every INSERT commits; the file survives the JVM exit. Point your local app at `jdbc:h2:file:./target/dev-seed` (user `sa`, no password) and you have realistic data to develop against.

### `main whatif`

First seeds normally. Then switches to the Insistence-Layer-wrapped datasource, increases the level, deletes an entire subsidiary (company + departments + employees), prints the state mid-experiment, then decreases the level. The deletes are undone. Final print shows the seeded baseline intact.

This is the "prod safety net" story from the README, boiled down: wrap a risky operation in a level, inspect, decide, commit or roll back. In this demo the rollback is hard-coded; in real life it's a human's call.

## Environment tree

```
CompanyEnvironment              2 companies
  └── DepartmentEnvironment     4 departments
        └── EmployeeEnvironment 10 employees
```

## Run

```bash
# Seed a local dev database (data persists in target/dev-seed.mv.db)
mvn -pl dev-seeding -am package
mvn -pl dev-seeding exec:java -Dexec.mainClass=org.simulatest.example.seeding.Main -Dexec.args="seed"

# Or run the what-if experiment
mvn -pl dev-seeding exec:java -Dexec.mainClass=org.simulatest.example.seeding.Main -Dexec.args="whatif"
```

## Key files

| File | What to read it for |
|---|---|
| [`Main.java`](src/main/java/org/simulatest/example/seeding/Main.java) | Both modes in one file. The `whatIf` method is the prod-safety-net pattern. |
| [`DevDatabase.java`](src/main/java/org/simulatest/example/seeding/DevDatabase.java) | A `DataSource` holder. Environments don't know which mode is active. |
| [`EmployeeEnvironment.java`](src/main/java/org/simulatest/example/seeding/environment/EmployeeEnvironment.java) | The same shape of Environment you'd write for tests, reused here. |
| [`MainSmokeTest.java`](src/test/java/org/simulatest/example/seeding/MainSmokeTest.java) | Verifies both modes in CI. Doubles as a readable usage example. |

## What real projects should lift from here

- **Share one set of Environment classes between tests and tooling.** The test suite seeds the H2 in-memory DB for isolated tests; the same classes populate a developer's real DB for exploration. No drift between what tests exercise and what developers see.
- **Wrap risky operations in a level.** Useful far beyond tests: production migrations, data fixes, bulk updates. Increase a level, run, inspect, decrease if wrong.
