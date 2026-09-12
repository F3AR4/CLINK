package com.clink.app.domain.usecase

import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.repository.UserPreferencesRepository

/**
 * Use case to select the active Pig for the home dashboard and savings flows.
 * Persists user choice in preferences.
 */
class SelectPigUseCase(
    private val pigRepository: PigRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(pigId: Long): Result<Unit> {
        val pig = pigRepository.getPigByIdOnce(pigId)
            ?: return Result.failure(IllegalArgumentException("Pig with ID $pigId does not exist"))

        return userPreferencesRepository.setSelectedPigId(pig.id)
    }
}
