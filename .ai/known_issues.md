# CLINK Known Issues & Technical Debt

## Environment Limitations
1. **Host Environment Lacks JDK and Android SDK**:
   - The current Windows host does not have JDK 17+ or the Android SDK (command-line tools / platform-tools) installed in PATH or standard system directories.
   - Consequently, local command line `./gradlew.bat assembleDebug` or `./gradlew.bat test` cannot execute within the agent subshell without a configured JDK.
   - The codebase must be opened in Android Studio (Ladybug / Iguana / Jellyfish) with embedded JDK 17 or 21, or JDK must be installed on the host system.

## Technical Debt & Placeholders
1. **Screen UI is Placeholder**:
   - Current screens are lightweight placeholders designed to validate navigation and ViewModel wiring. Production UI, coin animations, and playful visual micro-interactions will be built in subsequent tasks.
2. **Room Database Destructive Migration**:
   - Currently configured with `.fallbackToDestructiveMigration()` in `DatabaseModule.kt` for early development velocity. Production migrations must be implemented once production schema stabilizes.
3. **Single Pig Assumption in Sample Navigation**:
   - AddMoney and History navigation routes currently use a fallback `pigId = 1L` in simple default actions until Multi-Pig selection is fully polished.
