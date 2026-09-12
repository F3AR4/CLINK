package com.clink.app.domain.usecase

import com.clink.app.domain.model.Pig
import com.clink.app.domain.repository.PigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case to observe the primary piggy bank reactively.
 * Ensures the single-pig experience always binds to the authoritative primary pig.
 */
class GetPrimaryPigUseCase(
    private val pigRepository: PigRepository
) {
    operator fun invoke(): Flow<Pig?> {
        return pigRepository.getAllPigs().map { pigs ->
            pigs.firstOrNull()
        }
    }

    suspend fun getOrCreateDefault(): Pig {
        return pigRepository.getOrCreateDefaultPig()
    }
}
