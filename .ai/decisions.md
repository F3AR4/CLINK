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

## ADR-006: Derived Goal Progress and Authorization Semantics
- **Date**: 2026-09-12
- **Status**: Accepted
- **Context**: A goal represents a savings target for a Pig. Introducing an independent mutable `savedAmount` column on `Goal` would create a duplicate balance system prone to drift, synchronization lag, and confusion when transactions or adjustments occur.
- **Decision**:
  1. The Pig's balance and Room transaction records remain solely authoritative (`Pig.balance -> Goal progress`, never `Goal.savedAmount -> Pig.balance`).
  2. `Goal` entity stores only identity and target specifications (`id`, `pigId`, `title`, `targetAmount`, `createdAt`).
  3. Goal progress is derived dynamically in domain via `GoalProgressCalculator` using 100% integer paise (`Long`).
  4. Deleting a goal has zero side effects on Pig balance or Transaction history.
  5. Deterministic active-first ordering: incomplete goals are presented first (sorted by `createdAt DESC`), followed by completed goals (`createdAt DESC`).
- **Consequences**: Eliminates data drift and reconciliation bugs. Ensures zero floating-point monetary arithmetic and guarantees safe goal management.
## ADR-012: Multi-Pig Architecture, Persistent Selection, and Financial Isolation
- **Date**: 2026-09-12
- **Status**: Accepted
- **Context**: Transitioning CLINK from a single-pig implicit model (`pigId = 1L`) to a true multi-pig architecture supporting multiple savings pots (e.g., Emergency Fund, New Phone, Travel). Financial actions and domain state must guarantee complete isolation so that operations on one pig never leak to another.
- **Decision**:
  1. **Strict Financial Isolation**: Every saving operation, transaction record, and goal evaluation requires an explicit `pigId`. No implicit fallback or global balance mixing.
  2. **Authoritative Balance**: Pig balance remains strictly authoritative on the `Pig` entity in 100% integer paise (`Long`). No duplicate balance caches.
  3. **Persistent UI Selection via DataStore**: Active pig selection is persisted in `UserPreferencesRepository` under `active_pig_id`. Selection defaults to primary/default pig and reactively falls back to another surviving pig if the currently active pig is deleted.
  4. **Delete Safety Protection**: Deletion requires at least 2 pigs to exist in the database. Deleting the last remaining pig is strictly forbidden, ensuring the application always possesses a valid active pig. Room's SQLite `CASCADE` handles associated transactions and goals.
  5. **Explicit Presentation Routing**: Navigation routes (`AddMoneyRoute`, `GoalsRoute`, `HistoryRoute`, `PigDetailRoute`) accept explicit `pigId` arguments, ensuring UI views clearly state their target pig and target animation context.
- **Consequences**: Zero cross-pig corruption, seamless reactive switching across screens, robust process-restart persistence, and zero floating-point currency math.
