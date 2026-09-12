package com.clink.app.data.repository

import com.clink.app.data.local.dao.GoalDao
import com.clink.app.data.local.entity.GoalEntity
import com.clink.app.domain.model.Goal
import com.clink.app.domain.model.Money
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GoalRepositoryImplTest {

    private val goalTable = mutableMapOf<Long, GoalEntity>()
    private val goalFlow = MutableStateFlow<List<GoalEntity>>(emptyList())
    private var nextGoalId = 1L

    private lateinit var mockGoalDao: GoalDao
    private lateinit var repository: GoalRepositoryImpl

    @Before
    fun setUp() {
        goalTable.clear()
        nextGoalId = 1L
        goalFlow.value = emptyList()

        mockGoalDao = mockk(relaxed = true)

        coEvery { mockGoalDao.insertGoal(any()) } answers {
            val entity = firstArg<GoalEntity>()
            val assignedId = if (entity.id == 0L) nextGoalId++ else entity.id
            val saved = entity.copy(id = assignedId)
            goalTable[assignedId] = saved
            goalFlow.value = goalTable.values.toList()
            assignedId
        }

        coEvery { mockGoalDao.getGoalById(any()) } answers {
            val id = firstArg<Long>()
            goalTable[id]
        }

        coEvery { mockGoalDao.deleteGoal(any()) } answers {
            val id = firstArg<Long>()
            goalTable.remove(id)
            goalFlow.value = goalTable.values.toList()
        }

        every { mockGoalDao.getGoalsForPig(any()) } answers {
            val pigId = firstArg<Long>()
            MutableStateFlow(goalTable.values.filter { it.pigId == pigId })
        }

        every { mockGoalDao.getAllGoals() } returns goalFlow

        repository = GoalRepositoryImpl(mockGoalDao)
    }

    @Test
    fun `createGoal stores goal and returns generated id`() = runTest {
        val newGoal = Goal(
            id = 0L,
            pigId = 1L,
            title = "New Headphones",
            targetAmount = Money(500000L),
            createdAt = 1000L
        )

        val createdId = repository.createGoal(newGoal)

        assertThat(createdId).isEqualTo(1L)
        assertThat(goalTable).containsKey(1L)
        val stored = repository.getGoalById(createdId)
        assertThat(stored).isNotNull()
        assertThat(stored?.title).isEqualTo("New Headphones")
        assertThat(stored?.targetAmount).isEqualTo(Money(500000L))
    }

    @Test
    fun `getGoalById returns domain goal when entity exists`() = runTest {
        val savedEntity = GoalEntity(
            id = 42L,
            pigId = 1L,
            title = "Trip to Goa",
            targetPaise = 2000000L,
            savedPaise = 0L,
            deadline = null,
            isCompleted = false,
            createdAt = 5000L
        )
        goalTable[42L] = savedEntity

        val goal = repository.getGoalById(42L)

        assertThat(goal).isNotNull()
        assertThat(goal?.id).isEqualTo(42L)
        assertThat(goal?.title).isEqualTo("Trip to Goa")
        assertThat(goal?.targetAmount).isEqualTo(Money(2000000L))
    }

    @Test
    fun `getGoalById returns null when entity does not exist`() = runTest {
        val goal = repository.getGoalById(999L)
        assertThat(goal).isNull()
    }

    @Test
    fun `deleteGoal removes goal from storage`() = runTest {
        val savedEntity = GoalEntity(
            id = 10L,
            pigId = 1L,
            title = "Shoes",
            targetPaise = 300000L,
            savedPaise = 0L,
            deadline = null,
            isCompleted = false,
            createdAt = 1000L
        )
        goalTable[10L] = savedEntity

        repository.deleteGoal(10L)

        assertThat(goalTable).doesNotContainKey(10L)
    }

    @Test
    fun `observeAllGoals emits reactive updates as goals are added and removed`() = runTest {
        val initialGoals = repository.observeAllGoals().first()
        assertThat(initialGoals).isEmpty()

        repository.createGoal(
            Goal(id = 0L, pigId = 1L, title = "Goal 1", targetAmount = Money(100000L), createdAt = 100L)
        )

        val updatedGoals = repository.observeAllGoals().first()
        assertThat(updatedGoals).hasSize(1)
        assertThat(updatedGoals[0].title).isEqualTo("Goal 1")

        repository.deleteGoal(1L)
        val afterDelete = repository.observeAllGoals().first()
        assertThat(afterDelete).isEmpty()
    }
}
