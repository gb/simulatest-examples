# spring-kotlin-jpa

Simulatest composed with Spring DI, Spring Data JPA, and Hibernate. Same isolation guarantees as the plain-Java demo, but with entity managers, repositories, and `@Autowired` in the picture.

## Stack

Kotlin 2.3 · Spring 7 · Spring Data JPA 4 · Hibernate ORM 7 · JUnit 5 · H2.

## What it shows

- **Spring DI inside environments.** `TaskListEnvironment` autowires a `TaskRepository` and seeds rows through it.
- **JPA repositories backed by an Insistence-Layer connection.** The `DataSource` is wrapped transparently, so Hibernate's `SELECT`/`INSERT` and the savepoint stack cooperate without any special code on the application side.
- **A two-level tree with JPA state inheritance.** `TaskListDayTwoEnvironment` extends `TaskListEnvironment` and mutates rows the parent created. Both day-one and day-two tests see the parent data, day-two tests also see the mutations, and siblings of day-two would not.
- **The `simulatest-di-spring` plugin.** It bridges Spring's `ApplicationContext` into the environment instantiation path.

## Environment tree

```
TaskListEnvironment              10 fresh tasks, none done
  └── TaskListDayTwoEnvironment  marks 5 of them done
```

## Run

```bash
# From the repo root
mvn -pl spring-kotlin-jpa -am verify

# Or from this folder
mvn verify
```

## Key files

| File | What to read it for |
|---|---|
| [`JpaConfig.kt`](src/test/kotlin/org/simulatest/example/springboot/JpaConfig.kt) | Spring config: H2 `DataSource`, entity manager, JPA transaction manager. |
| [`Task.kt`](src/main/kotlin/org/simulatest/example/springboot/Task.kt) | JPA entity and `JpaRepository`. |
| [`Environments.kt`](src/main/kotlin/org/simulatest/example/springboot/Environments.kt) | Environments that use `@Autowired` repositories. |
| [`TaskListTest.kt`](src/test/kotlin/org/simulatest/example/springboot/TaskListTest.kt) | Day-one tests; see how each one starts from the same seeded state. |
| [`TaskListDayTwoTest.kt`](src/test/kotlin/org/simulatest/example/springboot/TaskListDayTwoTest.kt) | Day-two tests; they inherit day-one data plus the day-two mutations. |
