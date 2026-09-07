package com.clink.app.domain.repository

import com.clink.app.domain.model.Goal
import com.clink.app.domain.model.Money
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining operations for user savings goals.
 */
interface GoalRepository {
    fun getGoalsForPig(pigId: Long): Flow<List<Goal>>
    fun getAllGoals(): Flow<List<Goal>>
    suspend fun createGoal(goal: Goal): Long
    suspend fun updateGoal(goal: Goal)
    suspend fun updateSavedAmount(goalId: Long, newSavedAmount: Money)
    suspend fun deleteGoal(goalId: Long)
}
