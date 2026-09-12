# CLINK Testing History
 
## TASK-007 Home Dashboard Test Record (2026-09-12)
 
### Environment
- **Device / Emulator**: `emulator-5554` (`medium_phone` AVD)
- **Model**: `sdk_gphone64_x86_64`
- **OS / API**: Android 16 (API Level 36)
- **APK Installed**: `app/build/outputs/apk/debug/app-debug.apk`
- **Gradle**: 8.11.1
- **JDK**: OpenJDK 21.0.11
- **Android Studio**: 2026.1.4

### Automated Tests Executed
- `.\gradlew.bat test`: **85/85 PASSED** (0 failures, 0 errors, 0 skipped, 100% pass rate)
  - `HomeViewModelTest`: 3/3 (Reactive StateFlow emissions with 3 recent transactions, primary pig balance, empty ₹0 state)
  - `MoneyTest`: 11/11
  - `TransactionDomainTest`: 3/3
  - `AddMoneyUseCaseTest`: 7/7
  - `GetTransactionsUseCaseTest`: 2/2
  - `SavingsEnginePersistenceTest`: 4/4
  - `TransactionAtomicityTest`: 7/7
  - `AddMoneyViewModelTest`: 5/5
  - `HistoryViewModelTest`: 2/2
  - `FakePaymentRepositoryTest`: 3/3
  - `ClinkThemeTest`: 4/4
  - `ClinkComponentsTest`: 3/3
  - `UserPreferencesRepositoryTest`: 5/5
  - `OnboardingUseCasesTest`: 3/3
  - `OnboardingViewModelTest`: 5/5
  - `MainViewModelTest`: 3/3
  - `SavingsIsolationTest`: 1/1
  - `PigStateCalculatorTest`: 7/7
  - `PigProgressionPersistenceTest`: 3/3
  - `PigDetailViewModelTest`: 2/2
- `.\gradlew.bat assembleDebug`: **BUILD SUCCESSFUL**
- `.\gradlew.bat lint`: **BUILD SUCCESSFUL** (0 errors, 0 warnings)

### Live Runtime Scenarios Executed & Verified on Emulator (`emulator-5554`)
1. **Scenario A - Fresh Home ₹0 State**:
   - Initial fresh state verified on Home screen: balance ₹0, `🐣 New` badge, 0% progress toward ₹500.
   - First-Use encouraging guidance card rendered with "Save First ₹10 🐷" CTA. (PASS)
2. **Scenario B - Save ₹20**:
   - Tapped "Save First ₹10 🐷", selected ₹20, tapped "Clink It! 🐷".
   - Home updated immediately: balance ₹20, `🌱 Growing` badge, 4% progress toward ₹500.
   - Recent Activity showed 1 item: `Clink savings`, `Today, 12:33 PM`, `+ ₹20`. (PASS)
3. **Scenario C - Save ₹50**:
   - Saved additional ₹50.
   - Home updated immediately: balance ₹70, `🌱 Growing` badge, 14% progress toward ₹500.
   - Recent Activity showed 2 items: `+ ₹50` and `+ ₹20`. (PASS)
4. **Scenario D - Navigation Shortcuts**:
   - Tapped "History" shortcut -> navigated to `Saving History` screen displaying all transactions -> back. (PASS)
   - Tapped "Goals" shortcut -> navigated to `Savings Goals` screen displaying empty state -> back. (PASS)
   - Tapped Primary Pig Hero Card -> navigated to `Primary Pig` detail screen with full breakdown -> back. (PASS)
5. **Scenario E - App Restart Persistence**:
   - Force-stopped app (`am force-stop com.clink.app`) and relaunched (`am start`).
   - Home restored exact state: balance ₹70, `🌱 Growing`, 14% progress, exactly 2 transactions in Recent Activity; zero duplicates. (PASS)
6. **Scenario F - Dark Mode Contrast & Theming**:
   - Enabled night mode (`cmd uimode night yes`).
   - Captured on-device screenshot: Verified high-contrast dark navy surfaces, vibrant pink mascot, readable typography, and accessible touch targets. (PASS)
7. **Scenario G - Long Balance Formatting**:
   - `MoneyDisplay` verified with proper paise truncation and currency symbol sizing without clipping. (PASS)
8. **Scenario H - Logcat Fatal Exception Audit**:
   - `adb logcat -d -s AndroidRuntime:E` verified 0 fatal exceptions. (PASS)

## TASK-006 Transaction Engine Test Record (2026-09-12)
 
### Environment
- **Device / Emulator**: `emulator-5554` (`medium_phone` AVD)
- **Model**: `sdk_gphone64_x86_64`
- **OS / API**: Android 16 (API Level 36)
- **APK Installed**: `app/build/outputs/apk/debug/app-debug.apk`
- **Gradle**: 8.11.1
- **JDK**: OpenJDK 21.0.11
- **Android Studio**: 2026.1.4

### Automated Tests Executed
- `.\gradlew.bat test`: **82/82 PASSED** (0 failures, 0 errors, 0 skipped, 100% pass rate)
  - `MoneyTest`: 11/11
  - `TransactionDomainTest` [NEW]: 3/3 (Money paise preservation, bidirectional `TransactionEntity` mapping)
  - `AddMoneyUseCaseTest`: 7/7
  - `GetTransactionsUseCaseTest` [NEW]: 2/2 (All transactions vs pigId filtered query)
  - `SavingsEnginePersistenceTest`: 4/4
  - `TransactionAtomicityTest` [NEW]: 7/7 (Atomic commit, atomic rollback on failure, single timestamp verification, note fallback, non-existent pig rejection, invalid amount rejection, deterministic query ordering)
  - `AddMoneyViewModelTest`: 5/5
  - `HistoryViewModelTest` [NEW]: 2/2 (Reactive StateFlow emissions via `GetTransactionsUseCase`)
  - `FakePaymentRepositoryTest`: 3/3
  - `ClinkThemeTest`: 4/4
  - `ClinkComponentsTest`: 3/3
  - `UserPreferencesRepositoryTest`: 5/5
  - `OnboardingUseCasesTest`: 3/3
  - `OnboardingViewModelTest`: 5/5
  - `MainViewModelTest`: 3/3
  - `SavingsIsolationTest`: 1/1
  - `PigStateCalculatorTest`: 7/7
  - `PigProgressionPersistenceTest`: 3/3
  - `HomeViewModelTest`: 2/2
  - `PigDetailViewModelTest`: 2/2
- `.\gradlew.bat assembleDebug`: **BUILD SUCCESSFUL**
- `.\gradlew.bat lint`: **BUILD SUCCESSFUL** (0 errors, 0 warnings)

### Live Runtime Scenarios Executed & Verified on Emulator (`emulator-5554`)
1. **Scenario A - Fresh State & Save ₹10**:
   - Initial fresh state verified in SQLite: 1 pig (id=1, balance=0), 0 transactions.
   - Saved ₹10.
   - Verified SQLite: `balancePaise = 1000` (₹10), exactly 1 transaction (`CREDIT`, `1000 paise`, `Clink savings`).
   - Single committed timestamp verified: `pig.updatedAt == tx.timestamp` (1789195203606). (PASS)
2. **Scenario B - Save ₹20**:
   - Saved ₹20.
   - Verified SQLite: `balancePaise = 3000` (₹30), exactly 2 transactions.
   - Tx 2: `amountPaise = 2000` (₹20), `pig.updatedAt == tx.timestamp` (1789195217402). (PASS)
3. **Scenario C - Save ₹50**:
   - Saved ₹50.
   - Verified SQLite: `balancePaise = 8000` (₹80), exactly 3 transactions.
   - Tx 3: `amountPaise = 5000` (₹50), `pig.updatedAt == tx.timestamp` (1789195231642). (PASS)
4. **Scenario D - Restart App Persistence**:
   - Force-stopped app (`am force-stop`) and relaunched (`am start`).
   - Verified SQLite: balance remains exactly ₹80, transactions count remains 3.
   - No duplicate pigs or transactions created on restart. (PASS)
5. **Scenario E - Rapid Repeated Save Taps**:
   - Executed 5 rapid taps on "Clink It! 🐷" within 150ms.
   - Hardened `isProcessing` state lockout prevented multiple processing.
   - Verified SQLite: balance incremented by exactly ₹10 to ₹90 (9000 paise), transaction count incremented by exactly 1 to 4. (PASS)
6. **Scenario F - Saving History UI**:
   - Opened Saving History screen.
   - Verified UI card list ordered newest-first: ₹10 (id 4), ₹50 (id 3), ₹20 (id 2), ₹10 (id 1).
   - Verified proper note ("Clink savings"), timestamp, and credit styling (+₹). (PASS)
7. **Scenario G - Dark Mode**:
   - Toggled dark mode (`cmd uimode night yes`).
   - Verified dark theme contrast and rendering without visual glitches or crashes. (PASS)
8. **Scenario H - Logcat Audit**:
   - Audited Logcat: `adb logcat -d -s AndroidRuntime:E`.
   - Verified 0 fatal exceptions, 0 runtime crashes. (PASS)

---

## TASK-005 Pig Engine + Single Pig Experience Test Record (2026-09-12)
 
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
- `.\gradlew.bat test`: **68/68 PASSED** (0 failures, 0 errors, 0 skipped, 100% pass rate)
  - `MoneyTest`: 11/11
  - `AddMoneyUseCaseTest`: 7/7
  - `SavingsEnginePersistenceTest`: 4/4
  - `AddMoneyViewModelTest`: 5/5
  - `FakePaymentRepositoryTest`: 3/3
  - `ClinkThemeTest`: 4/4
  - `ClinkComponentsTest`: 3/3
  - `UserPreferencesRepositoryTest`: 5/5
  - `OnboardingUseCasesTest`: 3/3
  - `OnboardingViewModelTest`: 5/5
  - `MainViewModelTest`: 3/3
  - `SavingsIsolationTest`: 1/1
  - `PigStateCalculatorTest`: 7/7 (Boundary conditions at thresholds, determinism, progression fractions, and domain model integration)
  - `PigProgressionPersistenceTest`: 3/3 (Initial NEW state, progression across tiers with savings deposits, persistence across repository reload without duplicate pigs)
  - `HomeViewModelTest`: 2/2 (Idempotent startup and reactive primary pig state emissions)
  - `PigDetailViewModelTest`: 2/2 (Reactive pig loading by ID and default fallback)
- `.\gradlew.bat assembleDebug`: **BUILD SUCCESSFUL**
- `.\gradlew.bat lint`: **BUILD SUCCESSFUL** (0 errors, 0 warnings)

### Live Runtime Scenarios Executed & Verified on Emulator
1. **Scenario 1 - Fresh State Launch**:
   - Cleared data (`pm clear`) and completed onboarding.
   - Verified Home screen loaded with single primary pig in `NEW` state (`🐣 New`), ₹0 balance, 0% progress bar, and calm illustration. (PASS)
2. **Scenario 2 - Progression to GROWING**:
   - Saved ₹20 via Add Money quick chip.
   - Verified Home screen automatically transitioned to `🌱 Growing` state, ₹20 balance, and 4% milestone progress. (PASS)
3. **Scenario 3 - Pig Detail Screen Navigation**:
   - Tapped Primary Pig on Home screen to open `PigDetailScreen`.
   - Verified CLINK design system rendering: mascot hero, `Growing (🌱 Growing)` badge, balance, progression milestone card with progress bar, detailed metadata summary card, and action buttons. (PASS)
4. **Scenario 4 - Back Navigation to Home**:
   - Pressed back button.
   - Cleanly returned to Home screen without recomposition glitches. (PASS)
5. **Scenario 5 - Restart Persistence**:
   - Force-stopped app (`am force-stop`) and relaunched.
   - Verified Home screen opened directly with ₹20 balance, `GROWING` state, and exactly 1 pig present (0 duplicate pigs). (PASS)
6. **Scenario 6 - Dark Mode Compatibility**:
   - Toggled night mode (`cmd uimode night yes`).
   - Verified readable contrast and dark theme surfaces on Home and Pig Detail.
   - Reverted night mode (`cmd uimode night no`). (PASS)
7. **Scenario 7 - Logcat Audit**:
   - Inspected Logcat for errors or runtime exceptions: 0 errors reported. (PASS)

---

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
