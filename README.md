# Correlation ID Propagation Companion

Gutter warning icon on any Java/Kotlin Spring MVC handler method that
receives a correlation/trace-id header via `@RequestHeader` and makes an
outbound HTTP call in its own body, but never references that header
parameter again anywhere in the body — the common footgun where the
incoming id is captured and then silently dropped instead of being
forwarded to the downstream call, breaking distributed tracing and
making cross-service log correlation impossible for that request.

## Why it exists

Distributed tracing only works end-to-end if every service in the chain
forwards the same correlation/trace id to the next one. Nothing in the
IDE flags a handler that receives the id and then quietly drops it on
the next outbound call — it's a code-review-only discipline today, easy
to miss when a new downstream call is added to an existing handler.

## Why built this way

- **100% static text/PSI analysis** — matches the header parameter name
  (`correlation-id`, `trace-id`, `request-id`, etc.) and a broad set of
  outbound-HTTP call signals (`RestTemplate`, `WebClient`,
  `OkHttpClient`, `.exchange(`, and similar) by simple text, so it works
  whether the real HTTP client libraries are on the classpath or not.
- **Deliberately narrow trigger, deliberately broad "handled" signal** —
  only flags when the header parameter's identifier never appears again
  anywhere in the body; any later reference at all (even an imperfect
  one) is accepted as "forwarded," since a false negative here is far
  cheaper than nagging correct code.

## v0.1 scope — stated honestly, not exhaustively

Only covers the case where the header is received **and** the outbound
call happens in the **same method body**. If the id is passed down into
a helper method that makes the actual call, this won't see it (yet).
Can't confirm the id is forwarded as the *correct* header on the
*correct* request — only that the parameter isn't obviously unused.
Spring MVC only, not JAX-RS or other frameworks.

## Usage

Open any Java/Kotlin Spring controller. A handler that receives a
correlation/trace-id header, makes an outbound HTTP call, but never
references that header again shows a warning icon on the method name.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
