package com.clink.app.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for local user preferences and onboarding state.
 * Completely decoupled from Android and DataStore implementation details.
 */
interface UserPreferencesRepository {
    /**
     * Observable stream indicating whether the user has completed onboarding.
     */
    val isOnboardingCompleted: Flow<Boolean>

    /**
     * Persist onboarding completion status.
     * Returns [Result.success] on persistent write, or [Result.failure] if IO fails.
     */
    suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit>

    /**
     * Observable stream of the currently selected active Pig ID.
     * Emits null if no specific pig is selected.
     */
    val selectedPigId: Flow<Long?>

    /**
     * Persist the selected active Pig ID. Pass null to clear selection.
     */
    suspend fun setSelectedPigId(pigId: Long?): Result<Unit>
}

