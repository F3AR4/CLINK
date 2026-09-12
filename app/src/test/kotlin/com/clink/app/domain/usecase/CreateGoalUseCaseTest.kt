package com.clink.app.domain.usecase

import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.repository.GoalRepository
import com.clink.app.domain.repository.PigRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CreateGoalUseCaseTest {

    private val goalRepository: GoalRepository = mockk(relaxed = true)
    private val pigRepository: PigRepository = mockk(relaxed = true)
    private lateinit var useCase: CreateGoalUseCase

    private val testPig = Pig(
        id = 1L,
        name = "Primary Pig",
        balance = Money.RS_100
    )

    @Before
    fun setUp() {
        useCase = CreateGoalUseCase(goalRepository, pigRepository)
    }

    @Test
    fun `creating goal with valid title and positive amount succeeds`() = runTest {
        coEvery { pigRepository.getPigByIdOnce(1L) } returns testPig
        coEvery { goalRepository.createGoal(any()) } returns 10L

        val result = useCase(
            pigId = 1L,
            title = "  New Headphones  ",
            targetAmount = Money(500000L)
        )

        assertThat(result.isSuccess).isTrue()
        val created = result.getOrNull()
        assertThat(created).isNotNull()
        assertThat(created?.id).isEqualTo(10L)
        assertThat(created?.title).isEqualTo("New Headphones")

        coVerify(exactly = 1) {
            goalRepository.createGoal(match {
                it.pigId == 1L && it.title == "New Headphones" && it.targetAmount == Money(500000L)
            })
        }
    }

    @Test
    fun `creating goal with blank title fails without saving`() = runTest {
        val result = useCase(
            pigId = 1L,
            title = "   ",
            targetAmount = Money(50000L)
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Goal title cannot be blank")
        coVerify(exactly = 0) { goalRepository.createGoal(any()) }
    }

    @Test
    fun `creating goal with excessive title length fails without saving`() = runTest {
        val longTitle = "A".repeat(51)
        val result = useCase(
            pigId = 1L,
            title = longTitle,
            targetAmount = Money(50000L)
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("cannot exceed 50 characters")
        coVerify(exactly = 0) { goalRepository.createGoal(any()) }
    }

    @Test
    fun `creating goal with zero amount fails`() = runTest {
        val result = useCase(
            pigId = 1L,
            title = "Emergency Fund",
            targetAmount = Money.ZERO
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("greater than zero paise")
        coVerify(exactly = 0) { goalRepository.createGoal(any()) }
    }

    @Test
    fun `creating goal fails when target pig does not exist`() = runTest {
        coEvery { pigRepository.getPigByIdOnce(999L) } returns null

        val result = useCase(
            pigId = 999L,
            title = "Vacation",
            targetAmount = Money(100000L)
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Pig with ID 999 not found")
        coVerify(exactly = 0) { goalRepository.createGoal(any()) }
    }
}
