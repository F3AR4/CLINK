package com.clink.app.domain.usecase

import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.repository.PigRepository

/**
 * Use case to update an existing Pig's name, target amount, or visual properties.
 * Protects financial invariants (balance is never altered by metadata updates).
 */
class UpdatePigUseCase(
    private val pigRepository: PigRepository
) {
    suspend operator fun invoke(
        pigId: Long,
        name: String,
        targetAmount: Money? = null,
        iconName: String? = null,
        colorHex: String? = null
    ): Result<Pig> {
        val existing = pigRepository.getPigByIdOnce(pigId)
            ?: return Result.failure(IllegalArgumentException("Pig with ID $pigId not found"))

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

        val updated = existing.copy(
            name = cleanName,
            targetAmount = targetAmount ?: existing.targetAmount,
            iconName = iconName ?: existing.iconName,
            colorHex = colorHex ?: existing.colorHex,
            updatedAt = System.currentTimeMillis()
        )

        return try {
            pigRepository.updatePig(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
