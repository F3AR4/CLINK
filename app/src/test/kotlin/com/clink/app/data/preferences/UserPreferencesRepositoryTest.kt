package com.clink.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class UserPreferencesRepositoryTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private fun createTestPreferencesRepository(): UserPreferencesRepository {
        val testDataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("test_user_preferences_${System.nanoTime()}.preferences_pb") }
        )
        return UserPreferencesRepository(testDataStore)
    }

    @Test
    fun `default onboarding state is incomplete`() = runTest(testDispatcher) {
        val repository = createTestPreferencesRepository()

        val isCompleted = repository.isOnboardingCompleted.first()
        assertThat(isCompleted).isFalse()

        val userPrefs = repository.userPreferencesFlow.first()
        assertThat(userPrefs.isOnboardingCompleted).isFalse()
        assertThat(userPrefs.activePigId).isNull()
        assertThat(userPrefs.currencyCode).isEqualTo("INR")
    }

    @Test
    fun `setOnboardingCompleted persists true and emits via flow`() = runTest(testDispatcher) {
        val repository = createTestPreferencesRepository()

        repository.isOnboardingCompleted.test {
            // Initial default
            assertThat(awaitItem()).isFalse()

            // Complete onboarding
            val result = repository.setOnboardingCompleted(true)
            assertThat(result.isSuccess).isTrue()

            // Emits completed
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `persisted onboarding state survives across repository instances on same file`() = runTest(testDispatcher) {
        val testFile = tmpFolder.newFile("persistent_prefs.preferences_pb")

        val job1 = Job()
        val scope1 = CoroutineScope(testDispatcher + job1)
        val ds1 = PreferenceDataStoreFactory.create(
            scope = scope1,
            produceFile = { testFile }
        )
        val repo1 = UserPreferencesRepository(ds1)
        val writeResult = repo1.setOnboardingCompleted(true)
        assertThat(writeResult.isSuccess).isTrue()
        assertThat(repo1.isOnboardingCompleted.first()).isTrue()

        // Cancel scope1 to ensure no concurrent active DataStore on the same file
        job1.cancel()

        // Second session: simulate restart with new DataStore instance pointing to same file
        val job2 = Job()
        val scope2 = CoroutineScope(testDispatcher + job2)
        val ds2 = PreferenceDataStoreFactory.create(
            scope = scope2,
            produceFile = { testFile }
        )
        val repo2 = UserPreferencesRepository(ds2)
        assertThat(repo2.isOnboardingCompleted.first()).isTrue()
        job2.cancel()
    }

    @Test
    fun `setActivePigId updates active pig in user preferences`() = runTest(testDispatcher) {
        val repository = createTestPreferencesRepository()

        val result = repository.setActivePigId(42L)
        assertThat(result.isSuccess).isTrue()

        val prefs = repository.userPreferencesFlow.first()
        assertThat(prefs.activePigId).isEqualTo(42L)
    }

    @Test
    fun `setOnboardingCompleted returns failure when DataStore throws exception`() = runTest(testDispatcher) {
        val throwingDataStore = object : DataStore<Preferences> {
            override val data: Flow<Preferences> = flowOf(emptyPreferences())
            override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
                throw IOException("Disk full error")
            }
        }

        val repository = UserPreferencesRepository(throwingDataStore)
        val result = repository.setOnboardingCompleted(true)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
    }
}
