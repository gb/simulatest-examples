# Simulatest Examples

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-17%2B-blue.svg)](#build)
[![Simulatest](https://img.shields.io/maven-central/v/org.simulatest/simulatest-insistencelayer?label=simulatest)](https://central.sonatype.com/artifact/org.simulatest/simulatest-insistencelayer)

Runnable demos for [Simulatest](https://github.com/gb/simulatest): the transactional-sandbox + environment-tree toolkit for JVM tests. Every demo pulls Simulatest from Maven Central, so each one mirrors the experience a real consumer has, not a private-repo arrangement.

This is also where new ideas land first. If a pattern, integration, or database is going to be supported, a demo goes here and the docs point to it.

## Demos

Each folder is a self-contained project with its own `README.md` explaining what it teaches.

### Core basics
| Demo | Stack | What it shows |
|---|---|---|
| [`library-java`](library-java) | Java, JUnit 5, H2 | Environment tree, Insistence Layer, JUnit 5 TestEngine. The starting point. |

### Framework integrations
| Demo | Stack | What it shows |
|---|---|---|
| [`spring-kotlin-jpa`](spring-kotlin-jpa) | Kotlin, Spring, Spring Data JPA, Hibernate | Environments with `@Autowired` repositories; JPA backed by Insistence-Layer savepoints. |

> More on the way: Guice DI, Jakarta CDI, plain Hibernate, MyBatis, jOOQ, PostgreSQL, dev-seeding, parallel execution. See [CONTRIBUTING.md](CONTRIBUTING.md) if you want to add one.

## Build

Requires Java 17+ and Maven 3.9+.

```bash
# Build and test every demo
mvn verify

# Build one demo (plus what it depends on)
mvn -pl library-java -am verify

# Or step into a demo and run it directly
cd spring-kotlin-jpa && mvn verify
```

## Tracking a new Simulatest release

All demos track the same Simulatest version, set by the `simulatest.version` property in the root [`pom.xml`](pom.xml). Bump it in one place and every demo follows.

```xml
<properties>
    <simulatest.version>0.1.0</simulatest.version>
</properties>
```

Demos that break on a version bump are intentional — they surface API drift before a release lands in the wild.

## Repository layout

```
simulatest-examples/
├── pom.xml                  aggregator + shared dependency management
├── README.md                this file: catalog of demos
├── CONTRIBUTING.md          how to add a new demo
├── LICENSE                  Apache 2.0
├── .github/workflows/       CI that builds every demo
├── library-java/            a demo — Java, JUnit 5, raw JDBC
│   ├── README.md
│   ├── pom.xml
│   └── src/
└── spring-kotlin-jpa/       a demo — Kotlin, Spring, JPA
    ├── README.md
    ├── pom.xml
    └── src/
```

## Contributing

Pick a pattern Simulatest doesn't have a demo for yet, or a stack you'd like to see it paired with, and open a PR. [CONTRIBUTING.md](CONTRIBUTING.md) walks through the template.

## License

[Apache License 2.0](LICENSE).
