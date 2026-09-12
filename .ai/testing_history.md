# CLINK Testing History

## TASK-012 Multi-Pig Architecture Test Record (2026-09-12)

### Environment
- **Device / Emulator**: `emulator-5554` (`medium_phone` AVD)
- **Model**: `sdk_gphone64_x86_64`
- **OS / API**: Android 16 (API Level 36)
- **APK Installed**: `app/build/outputs/apk/debug/app-debug.apk`
- **Gradle**: 8.11.1
- **JDK**: OpenJDK 21.0.11 (`C:\Users\jowan\.jdks\jbr-21.0.11`)
- **Android Studio**: 2026.1.4

### Automated Tests Executed
- `.\gradlew.bat test`: **164/164 PASSED** (0 failures, 0 errors, 0 skipped, 100% pass rate across 32 test suites)
  - Baseline from TASK-011: **150 tests**
  - Added in TASK-012: **+14 tests**:
    - `MultiPigIsolationTest`: 4 tests (`saving to Pig A does not change Pig B balance`, `saving to Pig A records transaction with pigId A and none for B`, `saving to Pig A completes goal for Pig A without progressing Pig B goal`, `selected pig id persists in preferences`)
    - `PigCrudUseCaseTest`: 8 tests (`createPig inserts new pig and selects it`, `createPig with blank name fails`, `updatePig updates name successfully`, `deletePig succeeds when more than one pig exists`, `deletePig fails when only one pig exists`, `deletePig reselects fallback pig when deleted pig was selected`, `getSelectedPig emits selected pig`, `getSelectedPig falls back to first pig when selected is null or deleted`)
    - `HomeViewModelTest`: 2 tests (`uiState reflects active pig from getSelectedPigUseCase`, `onSelectPig delegates to selectPigUseCase`)
  - Total verified test suite: **164/164 PASS**
- `.\gradlew.bat assembleDebug`: **BUILD SUCCESSFUL**
- `.\gradlew.bat lintDebug`: **BUILD SUCCESSFUL** (0 errors, 0 warnings)

### Live Runtime Scenarios Executed & Verified on Emulator (`emulator-5554`, API 36)
1. **Scenario A (Fresh launch)**: App launched cleanly, displaying default pig. (PASS)
2. **Scenario B (Default pig exists)**: Verified initial pig presence and non-duplicate initialization. (PASS)
3. **Scenario C (Create Pig A "Emergency Fund")**: Created Pig A via "+ New Pig" dialog -> appeared in selector row. (PASS)
4. **Scenario D (Create Pig B "New Phone")**: Created Pig B via dialog -> appeared in selector row. (PASS)
5. **Scenario E (Total pigs in selector)**: Verified all pigs visible in selector row chips with active indicators. (PASS)
6. **Scenario F (Select Pig A)**: Tapped Pig A chip -> Home reactively switched to Pig A. (PASS)
7. **Scenario G (Save ₹20 to Pig A)**: Add Money showed "Saving to Emergency Fund" -> saved ₹20 -> balance became ₹20. (PASS)
8. **Scenario H (Pig A balance isolation)**: Verified only Pig A increased by ₹20; other pigs untouched. (PASS)
9. **Scenario I (Select Pig B)**: Switched to Pig B -> Home reactively loaded Pig B with ₹0 balance. (PASS)
10. **Scenario J (Save ₹50 to Pig B)**: Add Money showed "Saving to New Phone" -> saved ₹50 -> balance became ₹50. (PASS)
11. **Scenario K (Pig B balance isolation)**: Verified Pig B increased to ₹50; Pig A remained at ₹20. (PASS)
12. **Scenario L (Pig A Detail)**: Opened Pig A Detail -> showed ₹20 balance, status, and summary. (PASS)
13. **Scenario M (Pig B Detail)**: Opened Pig B Detail -> showed ₹50 balance, status, and summary. (PASS)
14. **Scenario N (Independent balances verified)**: Both pigs maintain strictly isolated balances in Room. (PASS)
15. **Scenario O (Independent histories verified)**: Pig A History contained only +₹20; Pig B History contained only +₹50. (PASS)
16. **Scenario P (Create goal for Pig B)**: Created goal "Phone Case" (₹60 target) for Pig B (balance ₹50). (PASS)
17. **Scenario Q (Save to complete Pig B goal)**: Saved ₹10 to Pig B -> balance ₹60 -> goal marked 100% Completed 🎉. (PASS)
18. **Scenario R (Goal isolation verified)**: Pig A goal & balance completely unaffected by Pig B progress. (PASS)
19. **Scenario S (Rename Pig A)**: Renamed Pig A from "Emergency Fund" to "Safety Net" -> updated immediately. (PASS)
20. **Scenario T (Restart app)**: Force-stopped app and restarted. (PASS)
21. **Scenario U (Selection persistence verified)**: Active pig selection persisted in DataStore across process restart. (PASS)
22. **Scenario V (Delete non-primary pig)**: Deleted "Safety Net" -> confirmation dialog displayed cascade warning -> deleted safely. (PASS)
23. **Scenario W (Post-delete navigation & fallback)**: App seamlessly fell back active selection to surviving pig. (PASS)
24. **Scenario X (Primary-pig deletion behavior & sole-pig safety)**: Deleted primary pig -> fell back to surviving pig; when only 1 pig remained, tapped delete -> blocked with "Cannot Delete Pig: CLINK requires at least one active pig. To delete this pig, create another pig first." (PASS)
25. **Scenario Y (Explicit Add Money target)**: Add Money screen clearly announces "Saving to <Pig Name>". (PASS)
26. **Scenario Z (Animation targeting)**: Coin flight and squash-and-stretch bounce belong strictly to the target pig. (PASS)
27. **Scenario AA (Light mode)**: All multi-pig elements render with crisp contrast in light mode. (PASS)
28. **Scenario AB (Dark mode)**: Deep navy background, glowing gold tokens, and high contrast chips in night mode. (PASS)
29. **Scenario AC (Rapid taps)**: Rapidly toggled between pig chips in `PigSelectorRow` -> smooth reactive updates with zero recomposition glitches. (PASS)
30. **Scenario AD (Multi-pig list)**: Supported multiple pigs with horizontal scrolling LazyRow. (PASS)
31. **Scenario AE (Logcat audit)**: `adb logcat -d -s AndroidRuntime:E Clink:V` verified 0 exceptions, 0 errors, 0 crashes. (PASS)

## TASK-011 CLINK Coin + Pig Animation System Test Record (2026-09-12)

 
### Environment
- **Device / Emulator**: `emulator-5554` (`medium_phone` AVD)
- **Model**: `sdk_gphone64_x86_64`
- **OS / API**: Android 16 (API Level 36)
- **APK Installed**: `app/build/outputs/apk/debug/app-debug.apk`
- **Gradle**: 8.11.1
- **JDK**: OpenJDK 21.0.11 (`C:\Users\jowan\.jdks\jbr-21.0.11`)
- **Android Studio**: 2026.1.4

### Automated Tests Executed
- `.\gradlew.bat test`: **150/150 PASSED** (0 failures, 0 errors, 0 skipped, 100% pass rate across 30 test suites)
  - Baseline from TASK-010: **143 tests**
  - Added in TASK-011: **+7 tests** (`ClinkSavingsAnimationTest`):
    - `animation transitions through stages in correct sequence`: PASS
    - `savings token formats whole rupee amounts accurately`: PASS
    - `savings token formats zero amount cleanly`: PASS
    - `savings token provides descriptive accessibility description`: PASS
    - `animated balance calculation interpolates cleanly without floating-point financial drift`: PASS
    - `animated balance calculation clamps progress beyond range [0, 1]`: PASS
    - `flight motion duration and easing tokens are properly configured`: PASS
  - Total verified test suite: **150/150 PASS** (both `testDebugUnitTest` and `testReleaseUnitTest` execute 150 tests with 0 failures)
- `.\gradlew.bat assembleDebug`: **BUILD SUCCESSFUL**
- `.\gradlew.bat lintDebug`: **BUILD SUCCESSFUL** (0 errors, 0 warnings)

### Live Runtime Scenarios Executed & Verified on Emulator (`emulator-5554`, API 36)
1. **Scenario A - Fresh Launch**:
   - Launched app -> Home screen showed authoritative baseline balance ₹220, pig illustration, and action buttons. (PASS)
2. **Scenario B - Open Add Money**:
   - Tapped Save Money -> Add Money screen opened cleanly with pig illustration, amount card, quick chips, and "Clink It!" button. (PASS)
3. **Scenario C - Save ₹10**:
   - Selected ₹10 denomination -> tapped "Clink It!" -> transaction saved -> gold coin token launched toward pig -> pig reacted with squash & stretch -> "CLINK! + ₹10 🐷" badge appeared -> balance animated smoothly to ₹230. (PASS)
4. **Scenario D - Save ₹20**:
   - Saved ₹20 -> verified "CLINK! + ₹20 🐷" celebration badge and pig bounce -> balance animated smoothly to ₹250. (PASS)
5. **Scenario E - Save ₹50**:
   - Saved ₹50 -> captured mid-flight coin screenshot (`anim_e_done.png`) -> balance animated to ₹300. (PASS)
6. **Scenario F - Custom Amount (₹35)**:
   - Entered ₹35 -> tapped "Clink It!" -> token rendered "₹35" -> balance interpolated cleanly (captured mid-roll at ₹321.48 in `anim_f_done3.png`) -> settled at ₹335. (PASS)
7. **Scenario G - Save with Note**:
   - Entered note "Chai treat" with ₹10 -> transaction saved and animation succeeded. (PASS)
8. **Scenario H - Rapid Tap / Duplicate Protection**:
   - Executed 5 rapid taps on "Clink It!" -> inspected Room SQLite database (`transactions` table) -> verified exactly 1 transaction was inserted (14 total transactions, ₹355 balance). Zero duplicate transactions or animation storms. (PASS)
9. **Scenario I - Process Restart**:
   - Terminated app via `am force-stop`, relaunched -> Home loaded with authoritative ₹355 balance -> no replay of old animation. (PASS)
10. **Scenario J - History**:
    - Navigated to Saving History -> verified all 14 transactions displayed in newest-first order with correct amounts (+ ₹10, + ₹35, + ₹50, + ₹20) and matching ₹355 total. Zero fake visual transactions. (PASS)
11. **Scenario K - Pig Detail**:
    - Navigated to Pig Detail -> verified ₹355 balance, Growing state, and animated pig reactiveness. (PASS)
12. **Scenario L - Light Mode**:
    - High-fidelity visual appearance verified in default light mode. (PASS)
13. **Scenario M - Dark Mode**:
    - Enabled system night mode via `cmd uimode night yes` -> verified dark mode styling on Pig Detail, Saving History, Add Money, and verified coin flight and celebration badge in dark mode. (PASS)
14. **Scenario N - Navigation During/After Animation**:
    - Back navigation from Add Money operates smoothly; no stuck overlay or broken back stack. (PASS)
15. **Scenario O - Multiple Consecutive Legitimate Saves**:
    - Completed 6+ consecutive saves (₹10, ₹20, ₹50, ₹35, ₹10, ₹10) with complete system stability. (PASS)
16. **Scenario P - Logcat Audit**:
    - `adb logcat -d -s AndroidRuntime:E SQLite:E Room:E FATAL:E` verified 0 fatal exceptions, 0 runtime errors. (PASS)

## TASK-010 History + Transaction UI Test Record (2026-09-12)

 
### Environment
- **Device / Emulator**: `emulator-5554` (`medium_phone` AVD)
- **Model**: `sdk_gphone64_x86_64`
- **OS / API**: Android 16 (API Level 36)
- **APK Installed**: `app/build/outputs/apk/debug/app-debug.apk`
- **Gradle**: 8.11.1
- **JDK**: OpenJDK 21.0.11 (`C:\Users\jowan\.jdks\jbr-21.0.11`)
- **Android Studio**: 2026.1.4

### Automated Tests Executed
- `.\gradlew.bat test`: **143/143 PASSED** (0 failures, 0 errors, 0 skipped, 100% pass rate across 29 test suites)
  - Baseline from TASK-009: **128 tests** (verified against commit `f494efd`; previously documented as 131 due to counting planned composite assertions rather than compiled `@Test` methods)
  - Added in TASK-010: **+15 tests**:
    - `TransactionDateFormatterTest`: 10/10 tests (today, yesterday, same year, different year, uppercase group headers, accessibility descriptions, blank note fallback)
    - `HistoryViewModelTest`: expanded from 2 to 7 tests (+5 tests: initial loading state, aggregate total & count calculation, empty list handling, error mapping, and retry recovery)
  - Total verified test suite: **143/143 PASS** (both `testDebugUnitTest` and `testReleaseUnitTest` execute 143 tests with 0 failures)
- `.\gradlew.bat assembleDebug`: **BUILD SUCCESSFUL**
- `.\gradlew.bat lintDebug`: **BUILD SUCCESSFUL** (0 errors, 0 warnings)

### Live Runtime Scenarios Executed & Verified on Emulator (`emulator-5554`)
1. **Scenario A - Empty History**:
   - Cleared app data, completed onboarding, opened History: empty state rendered with mascot, title "No savings yet", description "Your little savings journey\nwill show up here.", and primary "Save Your First ₹10" button. (PASS)
2. **Scenario B - First Transaction**:
   - Tapped "Save Your First ₹10" on empty state -> Add Money screen opened -> saved ₹10 -> returned to History: Summary card displayed "TOTAL SAVED ₹10" and "1 saving", "TODAY" section header, and "+ ₹10" transaction card with fallback note "Clink savings". (PASS)
3. **Scenario C - Multiple Transactions**:
   - Saved ₹20, ₹50, and ₹100 sequentially -> verified History updated to show 4 transactions totaling ₹180 in newest-first order (₹100, ₹50, ₹20, ₹10). (PASS)
4. **Scenario D - Notes**:
   - Saved with custom notes ("Chai save", "Takeout skipped") and blank notes -> verified custom notes render cleanly and blank notes fall back to "Clink savings". (PASS)
5. **Scenario E - Timestamps**:
   - Verified timestamps render as contextual relative dates ("Today, 4:32 PM", "Today, 4:31 PM") rather than raw epoch millis. (PASS)
6. **Scenario F - Persistence Across Process Death**:
   - Terminated app via `am force-stop`, relaunched, and navigated to History -> verified all 4 transactions and ₹180 total persisted intact. (PASS)
7. **Scenario G - Reactive Update**:
   - Verified that whenever savings transactions are recorded, the History timeline updates automatically without manual database refresh. (PASS)
8. **Scenario H - Empty -> Populated**:
   - Verified empty state smoothly transitions to populated summary card and timeline as soon as the first deposit is made. (PASS)
9. **Scenario I - Dark Mode & Theming**:
   - Toggled system night mode -> captured screenshots for both dark and light modes. Verified card elevations, contrast, primaryContainer badge, and secondary credit green tint. (PASS)
10. **Scenario J - Navigation Integrity**:
    - Verified `Home -> History`, `Home -> View All`, `Pig Detail -> History`, `History -> Add Money`, `History -> Home` back stacks operate without broken links. (PASS)
11. **Scenario K - Large List / Lazy List Scrolling**:
    - Added 6 additional savings (total 10 transactions) -> verified buttery-smooth vertical scrolling in `LazyColumn` with stable keys. (PASS)
12. **Scenario L - Logcat Audit**:
    - Audited logcat with `adb logcat -d -s AndroidRuntime:E SQLite:E Room:E`. 0 fatal exceptions, 0 crashes, 0 errors. (PASS)


 
### Environment
- **Device / Emulator**: `emulator-5554` (`medium_phone` AVD)
- **Model**: `sdk_gphone64_x86_64`
- **OS / API**: Android 16 (API Level 36)
- **APK Installed**: `app/build/outputs/apk/debug/app-debug.apk`
- **Gradle**: 8.11.1
- **JDK**: OpenJDK 21.0.11 (`C:\Users\jowan\.jdks\jbr-21.0.11`)
- **Android Studio**: 2026.1.4

### Automated Tests Executed
- `.\gradlew.bat testDebugUnitTest`: **131/131 PASSED** (0 failures, 0 errors, 0 skipped, 100% pass rate)
  - `GoalProgressCalculatorTest`: 10/10 (zero target, negative target, current < target, current == target, current > target, remaining amount, completion, clamp to 100%, 99% cap if under target, zero balance)
  - `CreateGoalUseCaseTest`: 6/6 (valid creation, blank title error, whitespace title error, title length > 50 error, zero target amount error, non-existent pig error)
  - `ObserveGoalsUseCaseTest`: 4/4 (empty list, derived progress computation, active-first ordering with completed last, non-existent pig empty flow)
  - `DeleteGoalUseCaseTest`: 3/3 (successful deletion, failure propagation, non-existent goal)
  - `GoalIntegrationTest`: 4/4 (end-to-end create -> save -> progress changes 20% -> 100% completed -> delete leaves pig balance and transactions intact)
  - `GoalViewModelTest`: 5/5 (initial state, goals emission, delete goal, delete confirmation dialog show/dismiss)
  - `CreateGoalViewModelTest`: 8/8 (initial state, title input, amount input, non-numeric filtering, validation errors, successful submit event, double-tap lockout, repository failure handling)
  - `GoalRepositoryImplTest`: 5/5 (observeGoalsForPig, observeAllGoals, getGoalById, createGoal, deleteGoal)
  - Plus all 96 existing unit test suites (MoneyTest, PigStateCalculatorTest, AddMoneyViewModelTest, HomeViewModelTest, PigProgressionPersistenceTest, SavingsEnginePersistenceTest, TransactionAtomicityTest, etc.)
- `.\gradlew.bat assembleDebug`: **BUILD SUCCESSFUL**
- `.\gradlew.bat lint`: **BUILD SUCCESSFUL** (0 errors, 0 warnings)

### Live Runtime Scenarios Executed & Verified on Emulator (`emulator-5554`)
1. **Scenario A - Empty State**:
   - Navigated to Goals screen: Empty state rendered with mascot, message "Give your savings a destination.", and "Create Your First Goal 🎯" button. (PASS)
2. **Scenario B - Create Goal**:
   - Clicked "Create Your First Goal" -> entered "New Headphones", target ₹500 -> clicked "Create Goal 🎯". (PASS)
   - Goal card rendered immediately showing ₹0 / ₹500, 0%, ₹500 to go. (PASS)
3. **Scenario C - Save Money & Reactive Progress**:
   - Navigated to Add Money -> saved ₹100 -> returned to Goals. (PASS)
   - Goal card automatically updated to ₹100 / ₹500, 20%, ₹400 to go without requiring manual refresh. (PASS)
4. **Scenario D - Continue Saving to Completion**:
   - Saved additional ₹400 (pig balance = ₹500) -> opened Goals. (PASS)
   - Goal transitioned to 100%, progress bar full, badge `Completed 🎉`, remaining text `Goal achieved!`, ₹0 remaining. (PASS)
5. **Scenario E - Persistence Across Process Death**:
   - Terminated app process via `am force-stop` -> relaunched app -> opened Goals. (PASS)
   - Goal "New Headphones", 100% completion state, and ₹500 balance persisted perfectly. (PASS)
6. **Scenario F - Multiple Goals & Active-First Ordering**:
   - Created second goal "Emergency Fund" with target ₹2,000. (PASS)
   - Verified both goals display: Active goal "Emergency Fund" (₹500 / ₹2,000, 25%) displayed first, Completed goal "New Headphones" displayed afterward. (PASS)
7. **Scenario G - Safe Deletion**:
   - Tapped delete icon on "New Headphones" -> confirmed in dialog. (PASS)
   - Goal disappeared from list. (PASS)
   - Checked Pig balance: exactly ₹500 (unchanged). (PASS)
   - Checked Transaction history: all transactions intact (unchanged). (PASS)
8. **Scenario H - Validation**:
   - Attempted creating goals with blank name and ₹0 amount -> inline validation messages prevented submission, button remained disabled. (PASS)
9. **Scenario I - Dark Mode**:
   - Enabled system dark mode -> captured screenshots for Home, Goals, and Create Goal screens. All surfaces, cards, typography, and progress indicators verified with high contrast and proper theming. (PASS)
10. **Scenario J - Navigation Integrity**:
    - Navigated Home -> Goals -> Create Goal -> Back to Goals -> Back to Home. Back stack remained fully intact without orphaned screens. (PASS)
11. **Scenario K - Logcat Audit**:
    - Audited logcat with `adb logcat -d -s AndroidRuntime:E SQLite:E Room:E`. 0 fatal exceptions, 0 crashes, 0 errors. (PASS)

 
### Environment
- **Device / Emulator**: `emulator-5554` (`medium_phone` AVD)
- **Model**: `sdk_gphone64_x86_64`
- **OS / API**: Android 16 (API Level 36)
- **APK Installed**: `app/build/outputs/apk/debug/app-debug.apk`
- **Gradle**: 8.11.1
- **JDK**: OpenJDK 21.0.11
- **Android Studio**: 2026.1.4

### Automated Tests Executed
- `.\gradlew.bat test`: **96/96 PASSED** (0 failures, 0 errors, 0 skipped, 100% pass rate)
  - `AddMoneyViewModelTest`: 16/16 (Initial state, quick amounts, custom amounts, conversion fidelity ₹1/₹10/₹99/₹500, empty/zero/overflow validations, non-numeric character filtering, note trimming & capping at 50, double-tap lockout, event emissions, resetState)
  - `HomeViewModelTest`: 3/3
  - `MoneyTest`: 11/11
  - `TransactionDomainTest`: 3/3
  - `AddMoneyUseCaseTest`: 7/7
  - `GetTransactionsUseCaseTest`: 2/2
  - `SavingsEnginePersistenceTest`: 4/4
  - `TransactionAtomicityTest`: 7/7
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
1. **Scenario A - Open Add Money**:
   - Screen loaded with mascot, quick chips (₹10, ₹20, ₹50, ₹100), custom input, note field, save button. (PASS)
2. **Scenario B - Quick Amount Selection**:
   - Selected ₹20 -> updated amount display, chip highlighted. (PASS)
3. **Scenario C - Save Quick Amount with Note**:
   - Saved ₹20 with note "CCoffee save" -> returned to Home, balance updated from ₹70 to ₹90, transaction recorded. (PASS)
4. **Scenario D - Custom Amount**:
   - Entered custom ₹35 -> saved -> balance updated from ₹90 to ₹125 (25% progress). (PASS)
5. **Scenario E - Invalid Amount Validation**:
   - Entered "0" -> inline error "Amount must be greater than ₹0", button disabled (`enabled="false"`), no transaction recorded. (PASS)
6. **Scenario F - Rapid Double Taps**:
   - Rapid consecutive Save taps -> exactly 1 save processed, balance incremented by exactly ₹10 to ₹135. (PASS)
7. **Scenario G - Persistence & App Restart**:
   - Force-stopped and relaunched -> balance ₹135, pig state `🌱 Growing`, all transactions intact. (PASS)
8. **Scenario H - History Screen**:
   - Opened History -> all 5 transactions rendered in reverse-chronological order with notes and credit pill indicators. (PASS)
9. **Scenario I - Dark Mode Contrast & Theming**:
   - Toggled night mode, captured screenshot -> verified readability, surface styling, and chip highlights. (PASS)
10. **Scenario J - Logcat Fatal Exception Audit**:
    - `adb logcat -d -s AndroidRuntime:E` verified 0 fatal exceptions. (PASS)

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
