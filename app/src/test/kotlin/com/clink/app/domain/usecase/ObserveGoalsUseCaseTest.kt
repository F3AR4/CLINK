package com.clink.app.domain.usecase

import com.clink.app.domain.model.Goal
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.repository.GoalRepository
import com.clink.app.domain.repository.PigRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ObserveGoalsUseCaseTest {

    private val goalRepository: GoalRepository = mockk()
    private val pigRepository: PigRepository = mockk()
    private lateinit var useCase: ObserveGoalsUseCase

    private val testPig = Pig(
        id = 1L,
        name = "Primary Pig",
        balance = Money(100000L) // ₹1,000
    )

    private val activeGoal1 = Goal(
        id = 1L,
        pigId = 1L,
        title = "New Headphones",
        targetAmount = Money(500000L), // ₹5,000 (20% with ₹1,000 balance)
        createdAt = 1000L
    )

    private val activeGoal2 = Goal(
        id = 2L,
        pigId = 1L,
        title = "Shoes",
        targetAmount = Money(200000L), // ₹2,000 (50% with ₹1,000 balance)
        createdAt = 2000L
    )

    private val completedGoal = Goal(
        id = 3L,
        pigId = 1L,
        title = "Book",
        targetAmount = Money(50000L), // ₹500 (100% with ₹1,000 balance)
        createdAt = 3000L
    )

    @Before
    fun setUp() {
        useCase = ObserveGoalsUseCase(goalRepository, pigRepository)
    }

    @Test
    fun `observes goals and derives progress reactively from pig balance`() = runTest {
        every { goalRepository.observeGoalsForPig(1L) } returns flowOf(listOf(activeGoal1))
        every { pigRepository.getAllPigs() } returns flowOf(listOf(testPig))

        val progressList = useCase(1L).first()

        assertThat(progressList).hasSize(1)
        val progress = progressList.first()
        assertThat(progress.goal.id).isEqualTo(1L)
        assertThat(progress.currentAmount).isEqualTo(Money(100000L))
        assertThat(progress.targetAmount).isEqualTo(Money(500000L))
        assertThat(progress.progressPercent).isEqualTo(20)
        assertThat(progress.remainingAmount).isEqualTo(Money(400000L))
        assertThat(progress.isCompleted).isFalse()
    }

    @Test
    fun `orders active goals first by createdAt DESC then completed goals by createdAt DESC`() = runTest {
        every { goalRepository.observeAllGoals() } returns flowOf(
            listOf(activeGoal1, activeGoal2, completedGoal)
        )
        every { pigRepository.getAllPigs() } returns flowOf(listOf(testPig))

        val progressList = useCase().first()

        assertThat(progressList).hasSize(3)
        // Active goals should be first: activeGoal2 (createdAt 2000) then activeGoal1 (createdAt 1000)
        assertThat(progressList[0].goal.id).isEqualTo(2L)
        assertThat(progressList[0].isCompleted).isFalse()

        assertThat(progressList[1].goal.id).isEqualTo(1L)
        assertThat(progressList[1].isCompleted).isFalse()

        // Completed goal comes last
        assertThat(progressList[2].goal.id).isEqualTo(3L)
        assertThat(progressList[2].isCompleted).isTrue()
        assertThat(progressList[2].progressPercent).isEqualTo(100)
    }

    @Test
    fun `returns empty list when no goals exist`() = runTest {
        every { goalRepository.observeAllGoals() } returns flowOf(emptyList())
        every { pigRepository.getAllPigs() } returns flowOf(listOf(testPig))

        val progressList = useCase().first()

        assertThat(progressList).isEmpty()
    }
}
