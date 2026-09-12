package com.clink.app.domain.usecase

import com.clink.app.domain.model.GoalProgress
import com.clink.app.domain.model.GoalProgressCalculator
import com.clink.app.domain.model.Money
import com.clink.app.domain.repository.GoalRepository
import com.clink.app.domain.repository.PigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

/**
 * Use case to observe goals reactively and calculate their progress deterministically
 * based on the Pig's authoritative savings balance.
 *
 * Ordering:
 * 1. Incomplete/Active goals first (newest created first).
 * 2. Completed goals second (newest created first).
 */
class ObserveGoalsUseCase(
    private val goalRepository: GoalRepository,
    private val pigRepository: PigRepository
) {
    operator fun invoke(pigId: Long? = null): Flow<List<GoalProgress>> {
        val goalsFlow = if (pigId != null) {
            goalRepository.observeGoalsForPig(pigId)
        } else {
            goalRepository.observeAllGoals()
        }

        val pigsFlow = pigRepository.getAllPigs()

        return combine(goalsFlow, pigsFlow) { goals, pigs ->
            val pigMap = pigs.associateBy { it.id }
            val primaryPigBalance = pigs.firstOrNull()?.balance ?: Money.ZERO

            goals.map { goal ->
                val balance = pigMap[goal.pigId]?.balance ?: primaryPigBalance
                GoalProgressCalculator.calculate(goal, balance)
            }.sortedWith(
                compareBy<GoalProgress> { it.isCompleted } // false (active) before true (completed)
                    .thenByDescending { it.goal.createdAt }
                    .thenByDescending { it.goal.id }
            )
        }
    }
}
