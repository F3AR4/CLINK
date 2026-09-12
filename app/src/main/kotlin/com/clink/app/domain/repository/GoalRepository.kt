package com.clink.app.domain.repository

import com.clink.app.domain.model.Goal
import com.clink.app.domain.model.Money
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining persistent operations for user savings goals.
 */
interface GoalRepository {
    fun observeGoalsForPig(pigId: Long): Flow<List<Goal>>
    fun observeAllGoals(): Flow<List<Goal>>
    suspend fun getGoalById(goalId: Long): Goal?
    suspend fun createGoal(goal: Goal): Long
    suspend fun updateGoal(goal: Goal)
    suspend fun deleteGoal(goalId: Long)

    // Backward compatibility aliases
    fun getGoalsForPig(pigId: Long): Flow<List<Goal>> = observeGoalsForPig(pigId)
    fun getAllGoals(): Flow<List<Goal>> = observeAllGoals()
    suspend fun updateSavedAmount(goalId: Long, newSavedAmount: Money) {}
}
