# junit4-library

The same library domain as [`junit6-library`](../junit6-library), ported to **JUnit 4**. If your team is on JUnit 4 and can't migrate right now, this demo proves you don't have to — Simulatest supports both runners with identical semantics.

## Stack

Java 17 · JUnit 4.13.2 · H2 · raw JDBC.

## What it shows

- **`@RunWith(EnvironmentJUnitRunner.class)`** — the JUnit 4 hook. Everything else (environments, `@UseEnvironment`, the Insistence Layer, the plugin) is identical to the JUnit 6 demo.
- **Same isolation guarantees.** Sibling environments are invisible; per-test rollback is automatic.
- **One real API difference worth knowing.** `@RunWith(Parameterized.class)` can't coexist with the Simulatest runner, so tables of inputs are written as separate methods sharing a helper. See `MemberTest.duplicateEmailIsRejected` and its siblings.

## Compare side-by-side

The only files that differ from `junit6-library` are:

```
pom.xml                             junit + simulatest-environment-junit4
src/test/java/.../*Test.java        @RunWith(EnvironmentJUnitRunner.class)
                                    org.junit.Test, org.junit.Assert.*
```

Everything under `src/main` — `LibraryDatabase`, the schema, the six environments — is **byte-for-byte identical** to the JUnit 6 demo. Run both demos and diff the `src/main` trees; they match. That's the point.

## Environment tree — a sequence of world-states

```
ReferenceDataEnvironment          chartered: genres & tiers defined, nothing physical yet
  └── OpenLibraryEnvironment      doors open: 3 branches exist at real addresses
        ├── StockedLibraryEnvironment     shelves full: 10 books, 18 copies across branches
        │     └── LendingLibraryEnvironment    ready to lend: 8 members enrolled, no loans yet
        │           └── ActiveCirculationEnvironment    5 loans out (1 overdue), 2 holds queued
        └── StaffedLibraryEnvironment     staffed building: 7 employees on duty, no stock
```

Names describe the state of the world at each level, not the table whose rows
got inserted. See [`junit6-library`](../junit6-library) for the full narrative.

## Run

```bash
# From the repo root
mvn -pl junit4-library -am verify

# Or from this folder
mvn verify
```

## Key files

| File | What to read it for |
|---|---|
| [`LibraryPlugin.java`](src/test/java/org/simulatest/example/library/LibraryPlugin.java) | Bootstraps H2 into the Insistence Layer — same as junit6-library. |
| [`StaffTest.java`](src/test/java/org/simulatest/example/library/StaffTest.java) | Crown jewel: sibling isolation. Its `@RunWith` is the whole diff. |
| [`MemberTest.duplicateEmailIsRejected`](src/test/java/org/simulatest/example/library/MemberTest.java) | How to replace a `@ParameterizedTest` when the class-level runner is already taken. |
