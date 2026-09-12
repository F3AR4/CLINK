package com.clink.app.domain.usecase

import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first

/**
 * Use case to delete a Pig safely.
 *
 * Safety guarantees:
 * 1. Checks that the pig exists.
 * 2. Prevents deletion if only 1 pig exists, ensuring the application always has a valid primary pig.
 * 3. When a multi-pig deletion occurs, automatically re-points selectedPigId to another surviving pig.
 * 4. Room's SQLite CASCADE cleans up associated transactions and goals.
 */
class DeletePigUseCase(
    private val pigRepository: PigRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(pigId: Long): Result<Unit> {
        val target = pigRepository.getPigByIdOnce(pigId)
            ?: return Result.failure(IllegalArgumentException("Pig with ID $pigId not found"))

        val allPigs = pigRepository.getAllPigs().first()
        if (allPigs.size <= 1) {
            return Result.failure(IllegalStateException("Cannot delete your only pig. Create another pig first."))
        }

        return try {
            pigRepository.deletePig(pigId)

            // If the deleted pig was the active one, fallback to another surviving pig
            val currentSelected = userPreferencesRepository.selectedPigId.first()
            val activePigId = currentSelected ?: allPigs.firstOrNull()?.id
            if (activePigId == pigId) {
                val remainingPigs = allPigs.filter { it.id != pigId }
                val fallbackPig = remainingPigs.firstOrNull()
                if (fallbackPig != null) {
                    userPreferencesRepository.setSelectedPigId(fallbackPig.id)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
