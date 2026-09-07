# Phase 01: Foundation Context

## Scope & Boundaries
- **Goal**: Establish a robust, scalable Android application foundation following Clean Architecture + MVVM principles.
- **Allowed Components**:
  - Jetpack Compose (Material 3) UI shell
  - Navigation Compose graph with core screen routes
  - Room database (local SQLite)
  - DataStore preferences
  - In-memory FakePaymentRepository for testing
  - Hilt Dependency Injection
- **Explicit Exclusions for Phase 1**:
  - NO real UPI payment gateway (e.g. Razorpay, Cashfree, PhonePe SDKs)
  - NO payment credentials or production keys
  - NO real money transactions
  - NO Firebase Authentication or Cloud Firestore
  - NO Remote Config or Cloud Messaging
  - NO real user sync or external APIs

## Key Conventions
1. All monetary values are `Money(val paise: Long)`.
2. Repositories return Kotlin `Flow` for observational data streams.
3. Screens receive ViewModels via `@HiltViewModel` injection (`hiltViewModel()`).
4. Data layer maps entities to domain models explicitly via `toDomain()` and `fromDomain()`.
