# Simulatest Examples

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-17%2B-blue.svg)](#build)
[![Simulatest](https://img.shields.io/maven-central/v/org.simulatest/simulatest-insistencelayer?label=simulatest)](https://central.sonatype.com/artifact/org.simulatest/simulatest-insistencelayer)

Runnable demos for [Simulatest](https://github.com/gb/simulatest): the transactional-sandbox + environment-tree toolkit for JVM tests. Every demo pulls Simulatest from Maven Central, so each one mirrors the experience a real consumer has, not a private-repo arrangement.

This is also where new ideas land first. If a pattern, integration, or database is going to be supported, a demo goes here and the docs point to it.

## Demos

Each folder is a self-contained project with its own `README.md` explaining what it teaches. Folders are prefixed with the JUnit version the demo uses; demos that aren't test suites have no prefix.

### Core basics
| Demo | Stack | What it shows |
|---|---|---|
| [`junit6-library`](junit6-library) | Java, JUnit 6, H2, raw JDBC | The environment tree, sibling isolation, per-test savepoint reset, and the JUnit Platform TestEngine. The starting point. Also the cleanest illustration of environments as **world-states** (`StockedLibraryEnvironment`, `ActiveCirculationEnvironment`) rather than one-per-table. |
| [`junit4-library`](junit4-library) | Java, JUnit 4, H2 | Same library domain as `junit6-library`, ported to JUnit 4. Proof that legacy codebases can adopt Simulatest without migrating off JUnit 4. |

### Framework integrations
| Demo | Stack | What it shows |
|---|---|---|
| [`junit6-kotlin-spring-jpa`](junit6-kotlin-spring-jpa) | Kotlin, Spring, Spring Data JPA, Hibernate, JUnit 6 | Environments with `@Autowired` repositories; JPA backed by Insistence-Layer savepoints. |
| [`junit6-guice-banking`](junit6-guice-banking) | Java, Guice 7, JUnit 6, H2 | A banking ledger where a transfer and its audit-log entries roll back together. Shiny moment: tests move money freely, next test sees books flat. |
| [`junit6-cdi-ticketing`](junit6-cdi-ticketing) | Java, Jakarta CDI 4, Weld SE, JUnit 6, H2 | A ticket purchase fires a CDI event; an `@Observes` listener decrements inventory. All four writes participate in one savepoint. |

### Other JVM languages
| Demo | Stack | What it shows |
|---|---|---|
| [`junit6-scala`](junit6-scala) | Scala 3, ScalikeJDBC, JUnit 6, H2 | The environment tree and `@UseEnvironment` from Scala, with ScalikeJDBC for the data layer. Same test engine, same isolation guarantees, different language. |

### Real databases
| Demo | Stack | What it shows |
|---|---|---|
| [`junit6-postgres-testcontainers`](junit6-postgres-testcontainers) | Java, JUnit 6, PostgreSQL 16, Testcontainers | The full library suite running against real PostgreSQL. Proves savepoint mechanics hold on a production-grade engine. |

### Beyond tests
| Demo | Stack | What it shows |
|---|---|---|
| [`dev-seeding`](dev-seeding) | Java, H2 file, no test framework | The same Environment classes reused from a plain `main()`. Two modes: seed a local dev DB (data persists) or run a "what-if" experiment (data undone via an Insistence Layer level). |

> More planned: plain Hibernate, MyBatis, jOOQ, parallel execution, custom plugin. See [CONTRIBUTING.md](CONTRIBUTING.md) if you want to add one.

## Build

Requires Java 17+ and Maven 3.9+. The Postgres demo also needs a running Docker daemon; the rest don't.

```bash
# Build and test every demo
mvn verify

# Skip Docker-based demos (useful locally without a Docker daemon)
mvn verify -DskipDocker

# Build one demo (plus what it depends on)
mvn -pl junit6-library -am verify

# Or step into a demo and run it directly
cd junit6-guice-banking && mvn verify
```

## Tracking a new Simulatest release

All demos track the same Simulatest version, set by the `simulatest.version` property in the root [`pom.xml`](pom.xml). Bump it in one place and every demo follows. Demos that break on a bump surface API drift before users hit it.

```xml
<properties>
    <simulatest.version>0.2.0</simulatest.version>
</properties>
```

## Repository layout

```
simulatest-examples/
├── pom.xml                              aggregator + shared dependency management
├── README.md                            this file
├── CONTRIBUTING.md                      how to add a new demo
├── LICENSE                              Apache 2.0
├── .github/workflows/build.yml          CI: runs mvn verify on every demo
├── dev-seeding/                         environments outside tests
├── junit4-library/                      library domain, JUnit 4 runner
├── junit6-cdi-ticketing/                Jakarta CDI, event-driven domain
├── junit6-guice-banking/                Guice, banking domain with audit log
├── junit6-kotlin-spring-jpa/            Kotlin + Spring + JPA
├── junit6-library/                      library domain, JUnit 6 platform engine
├── junit6-postgres-testcontainers/      library domain on real Postgres
└── junit6-scala/                        Scala 3 with ScalikeJDBC
```

## Contributing

Pick a pattern Simulatest doesn't have a demo for yet, or a stack you'd like to see it paired with, and open a PR. [CONTRIBUTING.md](CONTRIBUTING.md) walks through the template.

## License

[Apache License 2.0](LICENSE).
