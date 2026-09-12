package com.clink.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.clink.app.domain.repository.UserPreferencesRepository as UserPreferencesRepositoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class UserPreferences(
    val isOnboardingCompleted: Boolean,
    val activePigId: Long?,
    val currencyCode: String
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepositoryContract {

    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ACTIVE_PIG_ID = longPreferencesKey("active_pig_id")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
    }

    override val isOnboardingCompleted: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                isOnboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
                activePigId = preferences[PreferencesKeys.ACTIVE_PIG_ID],
                currencyCode = preferences[PreferencesKeys.CURRENCY_CODE] ?: "INR"
            )
        }

    override val selectedPigId: Flow<Long?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.ACTIVE_PIG_ID]
        }

    override suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit> {
        return runCatching {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
            }
            Unit
        }
    }

    override suspend fun setSelectedPigId(pigId: Long?): Result<Unit> {
        return runCatching {
            dataStore.edit { preferences ->
                if (pigId != null) {
                    preferences[PreferencesKeys.ACTIVE_PIG_ID] = pigId
                } else {
                    preferences.remove(PreferencesKeys.ACTIVE_PIG_ID)
                }
            }
            Unit
        }
    }

    suspend fun setActivePigId(pigId: Long): Result<Unit> = setSelectedPigId(pigId)
}

