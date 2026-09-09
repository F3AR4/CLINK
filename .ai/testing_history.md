# CLINK Testing History

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
