package com.clink.app.domain.usecase

import com.clink.app.domain.model.Pig
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transform

/**
 * Use case to observe the currently selected Pig reactively.
 *
 * Behavior:
 * 1. Combines [UserPreferencesRepository.selectedPigId] with [PigRepository.getAllPigs].
 * 2. If the user's selected pig exists, emits it.
 * 3. If selected pig is null or was deleted, falls back gracefully to the first available pig.
 * 4. If no pigs exist at all, initializes and emits the default pig.
 */
class GetSelectedPigUseCase(
    private val pigRepository: PigRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<Pig?> {
        return combine(
            pigRepository.getAllPigs(),
            userPreferencesRepository.selectedPigId
        ) { pigs, selectedId ->
            if (pigs.isEmpty()) {
                pigRepository.getOrCreateDefaultPig()
            } else {
                val match = if (selectedId != null) pigs.find { it.id == selectedId } else null
                match ?: pigs.first()
            }
        }
    }
}
