package com.clink.app.domain.usecase

import com.clink.app.domain.model.Goal
import com.clink.app.domain.model.Money
import com.clink.app.domain.repository.GoalRepository
import com.clink.app.domain.repository.PigRepository

/**
 * Use case to validate and create a new savings [Goal] for a Pig.
 */
class CreateGoalUseCase(
    private val goalRepository: GoalRepository,
    private val pigRepository: PigRepository
) {
    suspend operator fun invoke(
        pigId: Long,
        title: String,
        targetAmount: Money
    ): Result<Goal> {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Goal title cannot be blank"))
        }

        if (trimmedTitle.length > 50) {
            return Result.failure(IllegalArgumentException("Goal title cannot exceed 50 characters"))
        }

        if (!targetAmount.isPositive) {
            return Result.failure(IllegalArgumentException("Target amount must be greater than zero paise"))
        }

        val pig = pigRepository.getPigByIdOnce(pigId)
            ?: return Result.failure(IllegalArgumentException("Pig with ID $pigId not found"))

        val goal = Goal(
            pigId = pig.id,
            title = trimmedTitle,
            targetAmount = targetAmount,
            createdAt = System.currentTimeMillis()
        )

        return try {
            val generatedId = goalRepository.createGoal(goal)
            Result.success(goal.copy(id = generatedId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
