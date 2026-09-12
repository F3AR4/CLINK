# CLINK Testing History

## TASK-004 Onboarding + Persistent User State Test Record (2026-09-12)
 
### Environment
- **Device / Emulator**: `emulator-5554` (`medium_phone` AVD)
- **Model**: `sdk_gphone64_x86_64`
- **OS / API**: Android 16 (API Level 36)
- **APK Installed**: `app/build/outputs/apk/debug/app-debug.apk`
- **Gradle**: 8.11.1
- **JDK**: OpenJDK 21.0.11
- **Android Studio**: 2026.1.4
-
### Automated Tests Executed
- `.\gradlew.bat test`: **54/54 PASSED** (0 failures, 0 errors, 0 skipped, 100% pass rate)
  - `MoneyTest`: 11/11
  - `AddMoneyUseCaseTest`: 7/7
  - `SavingsEnginePersistenceTest`: 4/4
  - `AddMoneyViewModelTest`: 5/5
  - `FakePaymentRepositoryTest`: 3/3
  - `ClinkThemeTest`: 4/4
  - `ClinkComponentsTest`: 3/3
  - `UserPreferencesRepositoryTest`: 5/5 (Default false, write & reload across instances, corrupt state recovery, write error handling, toggle idempotence)
  - `OnboardingUseCasesTest`: 3/3 (Get state flow, complete success, complete failure propagation)
  - `OnboardingViewModelTest`: 5/5 (Initial state, successful completion, rapid double-tap suppression, failure state, error reset)
  - `MainViewModelTest`: 3/3 (Incomplete -> Onboarding route, Complete -> Home route, IO fallback to Onboarding)
  - `SavingsIsolationTest`: 1/1 (Room tables, pigs, transactions, balances unchanged by onboarding DataStore writes)
- `.\gradlew.bat assembleDebug`: **BUILD SUCCESSFUL**
- `.\gradlew.bat lint`: **BUILD SUCCESSFUL** (0 errors, 0 warnings)

### Live Runtime Scenarios Executed & Verified on Emulator
1. **Scenario A - Fresh Install Launch**:
   - `adb shell pm clear com.clink.app` executed to erase all local data.
   - Launched `com.clink.app/.MainActivity`.
   - Result: App routed cleanly to `OnboardingScreen` without flashing `HomeScreen`. Pig mascot, value proposition cards, and "Start Saving" CTA button displayed. **PASS**
2. **Scenario B - Complete Onboarding**:
   - Tapped "Start Saving" CTA button.
   - Result: Button showed loading state, persisted `isOnboardingCompleted = true` into DataStore, and navigated smoothly to `HomeScreen`. **PASS**
3. **Scenario C - App Restart Persistence**:
   - `adb shell am force-stop com.clink.app` executed.
   - Relaunched `com.clink.app/.MainActivity`.
   - Result: App bypassed `OnboardingScreen` completely and opened directly into `HomeScreen`. **PASS**
4. **Scenario D - Savings Isolation & Data Integrity**:
   - Navigated to `AddMoneyScreen`, selected ₹20, tapped "Clink It! 🐷".
   - Home screen displayed `Total Saved: ₹ 20`.
   - Force-stopped app and relaunched.
   - Result: Home screen retained `Total Saved: ₹ 20` and `OnboardingScreen` remained bypassed. **PASS**
5. **Scenario E - Theme Compatibility (Dark Mode)**:
   - Enabled night mode via `cmd uimode night yes`.
   - Result: Verified dark theme contrast and readable card surfaces on Onboarding and Home screens.
   - Reverted night mode via `cmd uimode night no`. **PASS**
6. **Navigation Smoke Test**:
   - Tapped Goals icon: Navigated to `Goals` screen. Back button returned to Home.
   - Tapped History icon: Navigated to `History` screen. Back button returned to Home.
   - Tapped Add Savings FAB: Navigated to `AddMoney` screen. Back button returned to Home.
   - Result: **PASS**
7. **Logcat & Stability Audit**:
   - Filtered for crashes/errors: 0 fatal exceptions, 0 Room errors, 0 Compose crashes. **PASS**

---

## TASK-003 Design System + Branding Test Record (2026-09-09)

### Environment
- **Device / Emulator**: `emulator-5554` (`medium_phone` AVD)
- **Model**: `sdk_gphone64_x86_64`
- **OS / API**: Android 16 (API Level 36)
- **APK Installed**: `app/build/outputs/apk/debug/app-debug.apk`

### Automated Tests Executed
- `.\gradlew.bat test`: **37/37 PASSED** (0 failures, 0 errors, 0 skipped, 100% pass rate)
  - `MoneyTest`: 11/11
  - `AddMoneyUseCaseTest`: 7/7
  - `SavingsEnginePersistenceTest`: 4/4
  - `AddMoneyViewModelTest`: 5/5
  - `FakePaymentRepositoryTest`: 3/3
  - `ClinkThemeTest`: 4/4 (Theme token verification, contrast, spacing)
  - `ClinkComponentsTest`: 3/3 (Chip denomination values, formatted strings, touch target contracts)
- `.\gradlew.bat assembleDebug`: **BUILD SUCCESSFUL**
- `.\gradlew.bat lint`: **BUILD SUCCESSFUL** (0 errors, 0 warnings)

### Live Runtime Scenarios Executed & Verified on Emulator
1. **Design System & Components Rendering**:
   - Branded Top Bar with pig mascot icon `CLINK 🐷` and accessible 48dp action touch targets.
   - Hero Total Saved Card with bold currency symbol `₹`, integer paise formatting, and custom piggy mascot illustration.
   - Branded `PigListItem` inside elevated `ClinkCard`.
   - Result: **PASS**
2. **Add Money Screen & Amount Chips Flow**:
   - Tapped `Add Savings` FAB.
   - Displayed interactive `ClinkAmountChip` grid: ₹10 (selected), ₹20, ₹50, ₹100.
   - Tapped ₹20 chip -> Active outline, checkmark, and selected amount updated immediately.
   - Tapped `Clink It! 🐷` button -> Saved ₹20.
   - Home screen balance updated reactively to ₹120.
   - Result: **PASS**
3. **Transaction History Screen**:
   - Tapped `Saving History` icon.
   - Transactions rendered in elevated `ClinkCard`s with credit badge `+ ₹`, timestamp, and note.
   - Result: **PASS**
4. **Goals Screen**:
   - Navigated to `Goals` screen.
   - Rendered `ClinkEmptyState` with piggy mascot illustration and call to action.
   - Result: **PASS**
5. **Dark Mode Verification**:
   - Switched system to night mode (`cmd uimode night yes`).
   - Verified readable contrast, dark theme surfaces, and smooth rendering.
   - Reverted night mode (`cmd uimode night no`).
   - Result: **PASS**
6. **Logcat & Stability Audit**:
   - 0 crashes, 0 ANRs, 0 SQLite/Room errors, 0 runtime exceptions.
   - Result: **PASS**

---

## TASK-002 Live Runtime Smoke Test Record (2026-09-09)

### Environment
- **Device / Emulator**: `emulator-5554` (`medium_phone` AVD)
- **Model**: `sdk_gphone64_x86_64`
- **OS / API**: Android 16 (Release 16, API Level 36)
- **APK Installed**: `app/build/outputs/apk/debug/app-debug.apk` (via `adb install -r`)

### Live Test Scenarios Executed & Verified
1. **Clean Installation & Launch**:
   - Initial state cleared via `adb shell pm clear com.clink.app`.
   - Launched `com.clink.app/.MainActivity`.
   - Home screen loaded: `Total Saved: ₹ 0`, `Primary Pig: ₹ 0`.
   - Result: **PASS**

2. **Sequential Micro-Savings Flow**:
   - Save ₹10: Tapped Add Money FAB -> Selected ₹10 -> Tapped "Clink It! 🐷" -> Home screen updated reactively to `Total Saved: ₹ 10`.
   - Save ₹20: Tapped Add Money FAB -> Selected ₹20 chip -> Tapped "Clink It! 🐷" -> Home screen updated reactively to `Total Saved: ₹ 30`.
   - Save ₹50: Tapped Add Money FAB -> Selected ₹50 chip -> Tapped "Clink It! 🐷" -> Home screen updated reactively to `Total Saved: ₹ 80`.
   - Result: **PASS** (Final balance reached exactly ₹80 from ₹0 initial state)

3. **Restart & Persistence Verification**:
   - Force-stopped app via `adb shell am force-stop com.clink.app`.
   - Relaunched app via `adb shell am start -n com.clink.app/.MainActivity`.
   - Verified Home screen state: `Total Saved: ₹ 80`, `Primary Pig: ₹ 80`.
   - Verified pig list: Exactly 1 pig present (`Primary Pig`), 0 duplicate pigs created.
   - Result: **PASS**

4. **Transaction Audit Verification**:
   - Navigated to `History` screen from top bar.
   - Verified transaction list: Exactly 3 entries recorded with correct timestamps and notes:
     - ₹50 (09 Sep, 07:26 PM) - "Clink savings"
     - ₹20 (09 Sep, 07:26 PM) - "Clink savings"
     - ₹10 (09 Sep, 07:25 PM) - "Clink savings"
   - Result: **PASS**

5. **Rapid Double-Tap Protection**:
   - Executed 5 rapid consecutive taps on Save button within milliseconds.
   - Verified resulting state: exactly 1 save transaction processed (balance incremented by single ₹10 to ₹90/₹100).
   - Verified history log: No duplicate transactions recorded.
   - Result: **PASS**

6. **Logcat & Crash Audit**:
   - Inspected Logcat for `com.clink.app`, `FATAL EXCEPTION`, `AndroidRuntime`, `SQLiteException`, `Room`.
   - Findings: 0 fatal exceptions, 0 Room errors, 0 SQLite corruption errors, 0 Compose runtime crashes.
   - Result: **PASS**

7. **Post-Verification Build, Test, & Lint**:
   - `.\gradlew.bat test` -> **BUILD SUCCESSFUL** (30/30 unit tests pass, 100%).
   - `.\gradlew.bat assembleDebug` -> **BUILD SUCCESSFUL**.
   - `.\gradlew.bat lint` -> **BUILD SUCCESSFUL** (0 errors).

---

## TASK-002 Unit Test Suites Record (2026-09-08)
- Total Unit Tests: 30
- Passed: 30, Failed: 0 (100% pass rate)
  - `MoneyTest`: 11/11
  - `AddMoneyUseCaseTest`: 7/7
  - `SavingsEnginePersistenceTest`: 4/4
  - `AddMoneyViewModelTest`: 5/5
  - `FakePaymentRepositoryTest`: 3/3

---

## TASK-001 Verification Record (2026-09-08)
- Tests Executed: 15 tests, 15 passed, 0 failures.
