# CLINK Architecture Documentation

## 1. Architectural Overview
CLINK is built using **Clean Architecture** combined with the **MVVM (Model-View-ViewModel)** design pattern in Kotlin and Jetpack Compose.

```
                      +-----------------------------+
                      |     Presentation Layer      |
                      | (Compose UI, ViewModels)    |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |        Domain Layer         |
                      | (Use Cases, Models, Repos)  |
                      +--------------+--------------+
                                     ^
                                     |
                      +--------------+--------------+
                      |         Data Layer          |
                      | (Room DB, Repositories,     |
                      |  DataStore, Fake Payments)  |
                      +-----------------------------+
```

## 2. Dependency Direction & Layer Responsibilities

### Presentation Layer (`com.clink.app.presentation`)
- **Responsibilities**: Displays user interface elements via Jetpack Compose and Material 3, reacts to UI state emitted by ViewModels, handles user interactions, and triggers navigation events.
- **Rules**:
  - Does NOT directly access the Room database, DAOs, or data entities.
  - Consumes Domain Models and interacts through Use Cases or Repository interfaces.
  - State is modeled with immutable Kotlin data classes exposed as `StateFlow`.

### Domain Layer (`com.clink.app.domain`)
- **Responsibilities**: Contains core business logic, entities, value classes, repository interfaces, and use cases.
- **Rules**:
  - Independent of Android UI frameworks, Room annotations, Firebase SDKs, or external payment libraries.
  - Acts as the architectural anchor of the application.

### Data Layer (`com.clink.app.data`)
- **Responsibilities**: Implements repository interfaces, manages persistence with Room, handles user settings with DataStore, and coordinates payment abstractions.
- **Rules**:
  - Contains database entities (`PigEntity`, `TransactionEntity`, `GoalEntity`) and explicit mapping functions (`toDomain()`, `fromDomain()`).
  - Hides underlying storage implementation details from the domain layer.

### Dependency Injection (`com.clink.app.di`)
- Configured via **Dagger Hilt** (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@Module`, `@InstallIn(SingletonComponent::class)`).
- Centralizes dependency creation (`DatabaseModule`, `RepositoryModule`, `DataStoreModule`, `UseCaseModule`).
- Service locators and manual instantiation in Composables are strictly avoided.

---

## 3. Package Structure
```
app/src/main/kotlin/com/clink/app/
├── ClinkApplication.kt
├── MainActivity.kt
│
├── di/
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   ├── DataStoreModule.kt
│   └── UseCaseModule.kt
│
├── domain/
│   ├── model/
│   │   ├── Money.kt
│   │   ├── Pig.kt
│   │   ├── Transaction.kt
│   │   ├── Goal.kt
│   │   └── User.kt
│   ├── repository/
│   │   ├── PigRepository.kt
│   │   ├── TransactionRepository.kt
│   │   ├── GoalRepository.kt
│   │   └── PaymentRepository.kt
│   └── usecase/
│       ├── AddMoneyUseCase.kt
│       └── GetPigSummaryUseCase.kt
│
├── data/
│   ├── local/
│   │   ├── ClinkDatabase.kt
│   │   ├── dao/
│   │   │   ├── PigDao.kt
│   │   │   ├── TransactionDao.kt
│   │   │   └── GoalDao.kt
│   │   └── entity/
│   │       ├── PigEntity.kt
│   │       ├── TransactionEntity.kt
│   │       └── GoalEntity.kt
│   ├── preferences/
│   │   └── UserPreferencesRepository.kt
│   └── repository/
│       ├── PigRepositoryImpl.kt
│       ├── TransactionRepositoryImpl.kt
│       ├── GoalRepositoryImpl.kt
│       └── FakePaymentRepository.kt
│
└── presentation/
    ├── theme/
    │   ├── Color.kt
    │   ├── Type.kt
    │   ├── Shape.kt
    │   └── Theme.kt
    ├── components/
    │   ├── ClinkTopBar.kt
    │   ├── MoneyDisplay.kt
    │   ├── ClinkButton.kt
    │   └── QuickAmountChip
    ├── navigation/
    │   ├── Screen.kt
    │   └── ClinkNavGraph.kt
    └── screens/
        ├── onboarding/
        ├── home/
        ├── addmoney/
        ├── history/
        ├── goals/
        └── pigdetail/
```

---

## 4. Money Representation Invariant
Financial precision is paramount in CLINK. To prevent IEEE-754 floating-point inaccuracies:
- All money values are strictly represented as `Long` integers in **paise** (1 Rupee = 100 paise).
- `Money(paise: Long)` is implemented as a Kotlin `@JvmInline value class`.
- Constants:
  - `RS_10` = 1,000 paise
  - `RS_20` = 2,000 paise
  - `RS_50` = 5,000 paise
  - `RS_100` = 10,000 paise
- `Float` and `Double` are strictly prohibited for monetary calculations.

---

## 5. Repository Pattern & Payment Abstraction
- Repositories expose reactive Kotlin `Flow<T>` streams for observation and `suspend` functions for modifications.
- **Payment Abstraction**: `PaymentRepository` decouples the core domain from banking or UPI integrations.
  - Phase 1 provides `FakePaymentRepository`, which simulates deposit and withdrawal operations entirely offline without requiring network access, payment secrets, or API keys.
  - Future phases can substitute production UPI/gateway implementations without changing domain or UI logic.

---

## 6. Room Database & Schema Evolution
- `ClinkDatabase` (Room v1) manages entities:
  - `pigs`: Table storing virtual piggy banks.
  - `transactions`: Table storing credits/debits with foreign key cascaded to `pigs`.
  - `goals`: Table storing target goals with foreign key cascaded to `pigs`.
- Configured with `exportSchema = true` to facilitate versioned database migrations in later development phases.

---

## 7. Navigation Architecture
- Uses AndroidX Navigation Compose (`NavHost`, `NavHostController`).
- Routes are encapsulated in `Screen.kt` with parameterized paths (e.g. `add_money?pigId={pigId}`, `pig_detail/{pigId}`).
- `ClinkNavGraph.kt` links destinations cleanly and passes navigation callbacks down to screens.
