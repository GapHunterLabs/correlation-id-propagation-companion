<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Correlation ID Propagation Companion Changelog

## [Unreleased]

## [0.1.0]

### Added

- Gutter warning icon on any Java/Kotlin Spring MVC handler method that
  receives a correlation/trace-id header via `@RequestHeader` and makes
  an outbound HTTP call in its own body without ever referencing that
  header parameter again.
- 100% static text/PSI analysis, Java and Kotlin, no network calls, no
  telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/correlation-id-propagation-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/correlation-id-propagation-companion/commits/0.1.0
