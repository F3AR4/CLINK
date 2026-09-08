# CLINK Known Issues & Technical Debt

## Environment Status
1. **Host Environment JDK & SDK Resolved**:
   - Environment has Android Studio 2026.1.4, Android SDK at `C:\Users\jowan\AppData\Local\Android\Sdk` (Platform 35 installed), and JDK 21 at `C:\Users\jowan\.jdks\jbr-21.0.11`.
   - `gradle-wrapper.jar` was generated and `gradle.properties` created. CLI builds, tests, and lints execute cleanly.
2. **No Active Emulator or Physical Device Connected**:
   - `adb devices` shows no connected Android device or running emulator on the host. Live UI interaction was verified up to APK compilation and static validation; live on-device testing requires starting an emulator in Android Studio.

## Technical Debt & Placeholders
1. **Screen UI is Placeholder**:
   - Current screens are lightweight placeholders designed to validate navigation and ViewModel wiring. Production UI, coin animations, and playful visual micro-interactions will be built in subsequent tasks.
2. **Room Database Destructive Migration**:
   - Currently configured with `.fallbackToDestructiveMigration()` in `DatabaseModule.kt` for early development velocity. Production migrations must be implemented once production schema stabilizes.
3. **Single Pig Assumption in Sample Navigation**:
   - AddMoney and History navigation routes currently use a fallback `pigId = 1L` in simple default actions until Multi-Pig selection is fully polished.
