package com.clink.app.domain.usecase

import com.clink.app.domain.repository.GoalRepository

/**
 * Use case to delete a savings goal.
 * Note: Deleting a goal does NOT modify pig balance or transactions.
 */
class DeleteGoalUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: Long): Result<Unit> {
        return try {
            goalRepository.deleteGoal(goalId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
