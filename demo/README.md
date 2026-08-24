# Demo data for screenshots

`OrderController.java` — `createOrder` drops the correlation id
(flagged), `cancelOrder` forwards it correctly (not flagged).

## How to get the screenshot

1. `./gradlew runIde` from `correlation-id-propagation-companion`, open
   this `demo/` folder as the project.
2. Full Screen, open `OrderController.java` — a warning icon should
   appear on `createOrder` but not on `cancelOrder`.
3. Screenshot with both methods visible, save into
   `correlation-id-propagation-companion/docs/screenshots/`. Close the
   sandbox.
