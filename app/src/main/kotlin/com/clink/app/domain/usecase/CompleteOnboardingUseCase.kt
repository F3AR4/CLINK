package com.clink.app.domain.usecase

import com.clink.app.domain.repository.UserPreferencesRepository
import javax.inject.Inject

/**
 * Use case to persist completion of the first-launch onboarding flow.
 */
class CompleteOnboardingUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return userPreferencesRepository.setOnboardingCompleted(true)
    }
}
