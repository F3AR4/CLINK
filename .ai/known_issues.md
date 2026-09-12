# CLINK Known Issues & Technical Debt

## Environment Status
1. **Host Environment JDK & SDK Resolved**:
   - Environment has Android Studio 2026.1.4, Android SDK at `C:\Users\jowan\AppData\Local\Android\Sdk` (Platforms 35 and 37 installed), and JDK 21 at `C:\Users\jowan\.jdks\jbr-21.0.11`.
   - `gradle-wrapper.jar` was generated and `gradle.properties` created. CLI builds, tests, and lints execute cleanly.
2. **Android Emulator Configured & Runtime Verified**:
   - `medium_phone` AVD created and verified on API 36 (`sdk_gphone64_x86_64`, Android 16).
   - Live end-to-end smoke testing executed on device: fresh launch, savings flow (₹10, ₹20, ₹50), restart persistence, and rapid double-tap suppression all verified.

## Technical Debt & Future Production Hardening
1. **Room Database Destructive Migration**:
   - Currently configured with `.fallbackToDestructiveMigration()` in `DatabaseModule.kt` for early development velocity. When schema version 2 is introduced for future features, formal `Migration(1, 2)` implementations will replace destructive migration to preserve user data across production releases.
2. **Payment Layer Fake Abstraction**:
   - Phase 1 intentionally uses `FakePaymentRepository` for offline and deterministic financial operation. Integration with authentic banking/UPI payment gateways will occur in a dedicated payment task.

