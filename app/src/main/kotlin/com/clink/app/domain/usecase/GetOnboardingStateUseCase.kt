package com.clink.app.domain.usecase

import com.clink.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to observe whether onboarding has been completed by the local user.
 */
class GetOnboardingStateUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<Boolean> = userPreferencesRepository.isOnboardingCompleted
}
