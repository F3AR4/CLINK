package com.clink.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
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
) {
    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ACTIVE_PIG_ID = longPreferencesKey("active_pig_id")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            isOnboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
            activePigId = preferences[PreferencesKeys.ACTIVE_PIG_ID],
            currencyCode = preferences[PreferencesKeys.CURRENCY_CODE] ?: "INR"
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setActivePigId(pigId: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_PIG_ID] = pigId
        }
    }
}
