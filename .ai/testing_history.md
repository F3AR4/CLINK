# CLINK Testing History

## TASK-001 Verification Record

### Unit Test Suites Created
1. `com.clink.app.domain.model.MoneyTest`:
   - `money constants represent correct paise amounts`: Validates ₹10=1000, ₹20=2000, ₹50=5000, ₹100=10000.
   - `fromRupees creates correct paise value`: Validates multiplication by 100.
   - `negative paise throws IllegalArgumentException`: Validates non-negative invariant.
   - `negative rupees throws IllegalArgumentException`: Validates non-negative invariant.
   - `addition correctly sums paise`: Validates `+` operator.
   - `subtraction correctly calculates difference`: Validates `-` operator.
   - `subtraction with insufficient funds throws IllegalArgumentException`: Validates balance guard.
   - `formatDisplay shows proper rupee and paise string`: Validates string formatting.
   - `comparison operators work as expected`: Validates `Comparable` implementation.

2. `com.clink.app.domain.usecase.AddMoneyUseCaseTest`:
   - `adding valid money updates pig balance and records transaction`: Validates atomic balance increment and transaction logging.
   - `adding zero or negative money returns failure without calling payment`: Validates input validation boundary.
   - `failed payment logs failed transaction and does not update pig balance`: Validates failure handling and transaction audit logging.

3. `com.clink.app.data.repository.FakePaymentRepositoryTest`:
   - `processDeposit succeeds by default with reference and amount`: Validates mock reference generation.
   - `processWithdrawal succeeds by default`: Validates withdrawal simulation.
   - `processDeposit handles failure toggle accurately`: Validates error state injection.

### Test Execution Status in Current Host Environment
- **Environment**: Windows 11 (64-bit), Git 2.54.0.
- **JDK / Android SDK**: Neither JDK 17+ nor Android SDK is configured in the environment PATH.
- **Execution Result**: Unit tests were designed and implemented with standard JUnit 4, Google Truth, and Mockk. They could NOT be executed directly in the host shell due to missing Java runtime (`java: The term 'java' is not recognized`).
- **Required Verification Step**: Run `./gradlew test` in Android Studio or after setting `JAVA_HOME`.
