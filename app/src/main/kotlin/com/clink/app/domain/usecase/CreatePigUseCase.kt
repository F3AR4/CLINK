package com.clink.app.domain.usecase

import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.repository.UserPreferencesRepository

/**
 * Use case to create a new digital Piggy Bank with validated name and target amount.
 * Automatically activates the new pig upon creation.
 */
class CreatePigUseCase(
    private val pigRepository: PigRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(
        name: String,
        targetAmount: Money? = null,
        iconName: String = "default_pig",
        colorHex: String = "#E85D75"
    ): Result<Pig> {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Pig name cannot be blank"))
        }
        if (cleanName.length > 30) {
            return Result.failure(IllegalArgumentException("Pig name cannot exceed 30 characters"))
        }

        if (targetAmount != null && !targetAmount.isPositive) {
            return Result.failure(IllegalArgumentException("Target amount must be greater than zero paise"))
        }

        val now = System.currentTimeMillis()
        val newPig = Pig(
            name = cleanName,
            balance = Money.ZERO,
            targetAmount = targetAmount,
            iconName = iconName,
            colorHex = colorHex,
            createdAt = now,
            updatedAt = now
        )

        return try {
            val generatedId = pigRepository.createPig(newPig)
            val createdPig = newPig.copy(id = generatedId)
            userPreferencesRepository.setSelectedPigId(generatedId)
            Result.success(createdPig)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
