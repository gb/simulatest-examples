# junit6-cdi-ticketing

A conference ticket shop with Jakarta CDI. Its hook: a ticket purchase fires a CDI event, an `@Observes` listener decrements inventory and writes a movement log. All four writes ride the same savepoint, so a test can buy tickets freely and the next test sees a pristine box office.

## Stack

Java 17 · JUnit 6 · Jakarta CDI 4.1 · Weld SE 5 · H2 · raw JDBC.

## What it shows

- **CDI bean wiring.** Services are `@ApplicationScoped`, environments are `@Dependent`, tests are `@Dependent`. The CDI container auto-discovers them via `beans.xml`.
- **Synchronous CDI events.** `PurchaseService` fires a `PurchaseEvent` after inserting the purchase row; `InventoryObserver.onPurchase(@Observes PurchaseEvent)` reacts on the same thread and same connection. No transactional boilerplate — the Insistence Layer wraps everything.
- **Side effects participate in the savepoint.** The purchase row, the inventory decrement, and the inventory-log row are three writes triggered by one method call, involving two classes. `PurchaseTest` moves seats around; `inventoryIsBackToFullBetweenTests` proves every seat is back in stock afterward.

## Environment tree

```
VenuesEnvironment                   2 venues (Austin, San Francisco)
  └── ConferencesEnvironment        3 conferences
        └── TicketTiersEnvironment  STANDARD (100) + VIP (20) per conference
```

## Run

```bash
# From the repo root
mvn -pl junit6-cdi-ticketing -am verify

# Or from this folder
mvn verify
```

## Key files

| File | What to read it for |
|---|---|
| [`PurchaseService.java`](src/main/java/org/simulatest/example/ticketing/PurchaseService.java) | Writes a purchase row, fires a `PurchaseEvent`. |
| [`InventoryObserver.java`](src/main/java/org/simulatest/example/ticketing/InventoryObserver.java) | Observes the event and does two follow-up writes. Synchronous, so it rides the savepoint. |
| [`PurchaseTest.java`](src/test/java/org/simulatest/example/ticketing/PurchaseTest.java) | Purchases + the two isolation tests at the bottom that prove observer writes also roll back. |
| [`TicketingPlugin.java`](src/test/java/org/simulatest/example/ticketing/TicketingPlugin.java) | Installs schema and configures the Insistence Layer. Runs alongside `SimulatestJakartaPlugin`; order-independent. |
| [`META-INF/beans.xml`](src/main/resources/META-INF/beans.xml) | CDI discovery descriptor. `annotated` mode means only annotated classes become beans. |

## Why `TicketingDatabase` instead of `@Inject DataSource`

CDI will happily inject whatever bean resolves to `DataSource`, but the Jakarta plugin in Simulatest (unlike the Spring plugin) doesn't replace a producer bean with the wrapped version. If services inject `DataSource` directly, Hibernate-style code would bypass the savepoint stack.

`TicketingDatabase` is a thin `@ApplicationScoped` bean whose every method pulls from `InsistenceLayerFactory.requireDataSource()`. Services inject it, not `DataSource`, so the wrap is never sidestepped. The same pattern is used in `junit6-guice-banking`.
