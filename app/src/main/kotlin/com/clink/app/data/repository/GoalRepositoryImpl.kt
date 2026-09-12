package com.clink.app.data.repository

import com.clink.app.data.local.dao.GoalDao
import com.clink.app.data.local.entity.GoalEntity
import com.clink.app.domain.model.Goal
import com.clink.app.domain.model.Money
import com.clink.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao
) : GoalRepository {

    override fun observeGoalsForPig(pigId: Long): Flow<List<Goal>> {
        return goalDao.getGoalsForPig(pigId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeAllGoals(): Flow<List<Goal>> {
        return goalDao.getAllGoals().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getGoalById(goalId: Long): Goal? {
        return goalDao.getGoalById(goalId)?.toDomain()
    }

    override suspend fun createGoal(goal: Goal): Long {
        return goalDao.insertGoal(GoalEntity.fromDomain(goal))
    }

    override suspend fun updateGoal(goal: Goal) {
        goalDao.updateGoal(GoalEntity.fromDomain(goal))
    }

    override suspend fun deleteGoal(goalId: Long) {
        goalDao.deleteGoal(goalId)
    }
}
