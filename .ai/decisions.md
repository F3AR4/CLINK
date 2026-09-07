# CLINK Architecture Decision Records (ADR)

## ADR-001: Integer Representation of Monetary Values
- **Date**: 2026-09-07
- **Status**: Accepted
- **Context**: Financial calculations involving floating point numbers (`Double`, `Float`) suffer from IEEE-754 precision loss, resulting in rounding errors and incorrect balances.
- **Decision**: All money is represented as 64-bit integers (`Long`) in **paise** (1 Rupee = 100 paise). Encapsulated in the Kotlin inline value class `Money`.
- **Consequences**: Floating point types are prohibited in domain, data, and presentation layers for monetary amounts. Conversions to Rupee strings occur only at UI formatting boundaries.

## ADR-002: Clean Architecture Layer Boundaries
- **Date**: 2026-09-07
- **Status**: Accepted
- **Context**: The app must remain maintainable, testable, and independent of external frameworks or payment gateways.
- **Decision**: Strict layered architecture:
  - `presentation` -> `domain` <- `data`
  - Domain layer has zero dependencies on Android UI, Room, Firebase, or external payment libraries.
  - Repositories in data layer implement domain interfaces.
- **Consequences**: Eliminates leaky abstractions and allows swapping database or payment engines without impacting domain logic.

## ADR-003: Gradle Version Catalog with Kotlin DSL
- **Date**: 2026-09-07
- **Status**: Accepted
- **Context**: Multi-module readiness and consistent dependency management across the project.
- **Decision**: Use `gradle/libs.versions.toml` as single source of truth for versions, libraries, and plugins using Gradle Kotlin DSL.
- **Consequences**: Type-safe accessors in build scripts and centralized updates.

## ADR-004: Payment Layer Abstraction for Phase 1
- **Date**: 2026-09-07
- **Status**: Accepted
- **Context**: Early phases require UI flow validation and domain testing without touching real banking/UPI infrastructure or storing secrets.
- **Decision**: `PaymentRepository` contract with `FakePaymentRepository` implementation.
- **Consequences**: Zero real payment APIs or secrets in the repo during Phase 1. Complete testability in offline environments.

## ADR-005: Room Persistence Strategy
- **Date**: 2026-09-07
- **Status**: Accepted
- **Context**: Need reliable local storage with foreign keys and cascade rules for Pig, Transactions, and Goals.
- **Decision**: Room 2.6.1 with KSP code generation, explicit foreign keys, schema export enabled.
- **Consequences**: Safe migrations, relational integrity, compile-time query verification.
