# Contributing

Thanks for wanting to add a demo. The goal is a shelf of small, readable projects — each one showing one idea clearly.

## What makes a good demo

- **One idea, visibly.** A Guice demo should teach Guice integration. A PostgreSQL demo should teach what changes vs. H2. Don't pile features on to cover more ground.
- **A real domain, not `Foo`/`Bar`.** Concrete names make the isolation story land. The library domain uses books and branches for this reason.
- **Runnable without extra setup.** No manual database install, no credentials. If a real database is needed, use Testcontainers.
- **Small.** Fewer than 300 lines of source, ideally. If it's bigger, it's probably two demos.

## Layout of a demo

```
your-demo/
├── README.md          what it shows, how to run, key files to read
├── pom.xml            inherits from the root parent
└── src/
    ├── main/...       domain code
    └── test/...       tests that use Simulatest
```

## Naming

- Folder: lowercase `kebab-case`, descriptive of the stack or idea — `guice-h2`, `postgres-testcontainers`, `dev-seeding`, `parallel-execution`.
- Artifact id: same as the folder name.
- Package: `org.simulatest.example.<shortname>`.

## Wiring a new demo in

1. Create the folder.
2. Add a `pom.xml` that inherits from the root aggregator:

    ```xml
    <parent>
        <groupId>org.simulatest.examples</groupId>
        <artifactId>simulatest-examples</artifactId>
        <version>0.1.0</version>
    </parent>

    <artifactId>your-demo</artifactId>
    <name>Simulatest Demo - Your Idea</name>
    ```

3. Depend on whichever Simulatest artifacts your demo needs. Versions come from the parent's `dependencyManagement`, so don't pin them in the demo.

    ```xml
    <dependencies>
        <dependency>
            <groupId>org.simulatest</groupId>
            <artifactId>simulatest-environment-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.simulatest</groupId>
            <artifactId>simulatest-insistencelayer</artifactId>
        </dependency>
        <!-- ... -->
    </dependencies>
    ```

4. Append the folder name to the `<modules>` list in the root [`pom.xml`](pom.xml).
5. Write a `README.md` using the template below.
6. Add a row for the demo under the matching section in the root [`README.md`](README.md).
7. Run `mvn verify` from the repo root. CI runs the same command and must stay green.

## Per-demo README template

```markdown
# your-demo

One-sentence summary of the idea.

## Stack

The libraries / language versions in play.

## What it shows

- Two to five bullet points. Each one a concrete thing a reader will learn.

## Run

​```bash
mvn -pl your-demo -am verify
​```

## Key files

| File | What to read it for |
|---|---|
| `path/to/File.java` | Why this file is interesting. |
```

## New dependency versions

If a demo needs something no existing demo uses (e.g. MyBatis), prefer pinning it *in the demo's own pom.xml*. Only promote a version to the root `dependencyManagement` once a second demo adopts it — that keeps the parent honest and stops it drifting into a grab-bag.

## Style

- **Name environments for world-states, not database tables.** `StockedLibraryEnvironment`, `FundedBankEnvironment`, `OnSaleEnvironment` — each one describes a cohesive state of the system, not a bag of rows for one entity. If your tree reads `UserEnvironment → OrderEnvironment → PaymentEnvironment`, step back: the system probably has fewer distinct useful states than it has tables. See `junit6-library` for a tree where every level is a state a real library moves through.
- Comments explain *why*, not *what*. A file called `StockedLibraryEnvironment.java` doesn't need a comment saying `// stocks the library`.
- Prefer fewer moving parts over a complete showcase. A good demo looks embarrassingly short.

## Bumping the Simulatest version

A single property — `simulatest.version` in the root `pom.xml` — drives every demo. Bump it in one place. If a demo breaks, either fix it against the new API or open an issue; never silently pin the demo to an old version.

## Licence

All contributions are released under the same [Apache License 2.0](LICENSE) as the project.
