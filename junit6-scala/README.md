# junit6-scala

A product-catalog demo in **Scala 3** using **ScalikeJDBC**. Proof that Simulatest's `@UseEnvironment` flow works from any JVM language whose classes compile to bytecode Jupiter understands, and pairs cleanly with idiomatic Scala data-access libraries.

## Stack

Scala 3.5 · ScalikeJDBC 4.3 · JUnit 6 · H2.

## What it shows

- **Scala environments.** `CategoriesEnvironment` and `ProductsEnvironment` are plain Scala classes implementing `org.simulatest.environment.Environment`. The `@EnvironmentParent(classOf[CategoriesEnvironment])` annotation works exactly as in Java.
- **ScalikeJDBC through the savepoint stack.** `ConnectionPool.add(DEFAULT_NAME, DataSourceConnectionPool(wrapped))` points ScalikeJDBC at the Insistence-Layer-wrapped DataSource. Every `sql"..."` call rides the shared connection.
- **Jupiter from Scala.** Tests use `org.junit.jupiter.api.Test` and `Assertions.assertEquals`. Nothing Scala-specific in the test framework layer.
- **Same isolation guarantees as the Java demos.** `updateProductPrice` then `priceIsBackToOriginal`; `deleteAllElectronics` then `electronicsAreBack`. Sibling tests never see each other's mutations.

## Environment tree

```
CategoriesEnvironment     3 categories (Books, Electronics, Homeware)
  └── ProductsEnvironment 7 products spread across them
```

## Run

```bash
# From the repo root
mvn -pl junit6-scala -am verify

# Or from this folder
mvn verify
```

## Why `DB.autoCommit` and not `DB.localTx`

ScalikeJDBC's `localTx` calls `Connection.commit()` at the end of each block. `ConnectionWrapper` translates that into a `USER_COMMIT` savepoint bump; JDBC's spec then cascade-releases any savepoints created *after* that bump, including the ones Simulatest pushed for the current environment level. The stack gets corrupted and cleanup fails at the end of the suite.

`DB.autoCommit` asks for auto-commit mode (silently ignored by `ConnectionWrapper` so the real connection stays at `autoCommit=false`) and never issues an explicit commit. Statements land behind Simulatest's savepoints, the level stack stays intact, and rollback between tests works as expected.

This is a sharp edge; the same category of issue can bite any Scala library that wraps each operation in an explicit commit. A future Simulatest release may eliminate it at the `ConnectionWrapper` level.

## Key files

| File | What to read it for |
|---|---|
| [`CatalogDb.scala`](src/main/scala/org/simulatest/example/catalog/CatalogDb.scala) | ScalikeJDBC wrappers. See the class comment for why `DB.autoCommit` is used. |
| [`CatalogPlugin.scala`](src/test/scala/org/simulatest/example/catalog/CatalogPlugin.scala) | Bootstraps H2, wraps via the Insistence Layer, registers the wrapped DataSource with ScalikeJDBC's connection pool. |
| [`ProductsEnvironment.scala`](src/main/scala/org/simulatest/example/catalog/environment/ProductsEnvironment.scala) | The parent-annotation in Scala syntax. |
| [`ProductsTest.scala`](src/test/scala/org/simulatest/example/catalog/ProductsTest.scala) | Classic Simulatest isolation story told in Scala. |

## Scala-specific notes

- **No ScalaTest.** Simulatest's TestEngine delegates class execution to Jupiter internally. ScalaTest (and Spock, for that matter) registers itself as a separate JUnit Platform engine; Simulatest doesn't route to it. Use Jupiter annotations directly from Scala for now. The language benefits (pattern matching, string interpolation, `using` blocks) are all still there.
- **Scala 3 significant indentation.** Nothing stops you from writing curly-brace Scala if you prefer. The demo uses indent syntax except inside `DB.autoCommit { implicit session => ... }` blocks, where Scala 3's parser currently can't combine indent-blocks with `implicit` parameter syntax.
