package com.clink.app.domain.usecase

import app.cash.turbine.test
import com.clink.app.domain.repository.UserPreferencesRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

class OnboardingUseCasesTest {

    private val userPreferencesRepository = mockk<UserPreferencesRepository>()

    @Test
    fun `GetOnboardingStateUseCase emits flow from repository`() = runTest {
        every { userPreferencesRepository.isOnboardingCompleted } returns flowOf(false, true)

        val useCase = GetOnboardingStateUseCase(userPreferencesRepository)

        useCase().test {
            assertThat(awaitItem()).isFalse()
            assertThat(awaitItem()).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `CompleteOnboardingUseCase calls setOnboardingCompleted true and succeeds`() = runTest {
        coEvery { userPreferencesRepository.setOnboardingCompleted(true) } returns Result.success(Unit)

        val useCase = CompleteOnboardingUseCase(userPreferencesRepository)
        val result = useCase()

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { userPreferencesRepository.setOnboardingCompleted(true) }
    }

    @Test
    fun `CompleteOnboardingUseCase propagates failure when repository fails`() = runTest {
        val error = IOException("Storage unavailable")
        coEvery { userPreferencesRepository.setOnboardingCompleted(true) } returns Result.failure(error)

        val useCase = CompleteOnboardingUseCase(userPreferencesRepository)
        val result = useCase()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(error)
    }
}
