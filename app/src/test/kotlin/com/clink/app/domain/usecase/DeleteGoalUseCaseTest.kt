package com.clink.app.domain.usecase

import com.clink.app.domain.repository.GoalRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteGoalUseCaseTest {

    private val goalRepository: GoalRepository = mockk(relaxed = true)
    private lateinit var useCase: DeleteGoalUseCase

    @Before
    fun setUp() {
        useCase = DeleteGoalUseCase(goalRepository)
    }

    @Test
    fun `deleting goal delegates exclusively to goalRepository`() = runTest {
        coJustRun { goalRepository.deleteGoal(42L) }

        val result = useCase(42L)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { goalRepository.deleteGoal(42L) }
    }

    @Test
    fun `deleting goal returns failure when repository fails`() = runTest {
        coEvery { goalRepository.deleteGoal(99L) } throws RuntimeException("Database error")

        val result = useCase(99L)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Database error")
        coVerify(exactly = 1) { goalRepository.deleteGoal(99L) }
    }
}
