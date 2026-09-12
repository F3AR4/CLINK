package com.clink.app.domain.usecase

import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.repository.UserPreferencesRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PigCrudUseCaseTest {

    private val pigRepository: PigRepository = mockk(relaxed = true)
    private val preferencesRepository: UserPreferencesRepository = mockk(relaxed = true)

    private val selectedPigIdFlow = MutableStateFlow<Long?>(null)
    private val allPigsFlow = MutableStateFlow<List<Pig>>(emptyList())

    private lateinit var createPigUseCase: CreatePigUseCase
    private lateinit var updatePigUseCase: UpdatePigUseCase
    private lateinit var deletePigUseCase: DeletePigUseCase
    private lateinit var selectPigUseCase: SelectPigUseCase
    private lateinit var getSelectedPigUseCase: GetSelectedPigUseCase

    private val defaultPig = Pig(id = 1L, name = "Primary Pig", balance = Money.RS_100)
    private val emergencyPig = Pig(id = 2L, name = "Emergency Fund", balance = Money.RS_50)

    @Before
    fun setUp() {
        every { preferencesRepository.selectedPigId } returns selectedPigIdFlow
        coEvery { preferencesRepository.setSelectedPigId(any()) } answers {
            selectedPigIdFlow.value = firstArg()
            Result.success(Unit)
        }
        every { pigRepository.getAllPigs() } returns allPigsFlow

        createPigUseCase = CreatePigUseCase(pigRepository, preferencesRepository)
        updatePigUseCase = UpdatePigUseCase(pigRepository)
        deletePigUseCase = DeletePigUseCase(pigRepository, preferencesRepository)
        selectPigUseCase = SelectPigUseCase(pigRepository, preferencesRepository)
        getSelectedPigUseCase = GetSelectedPigUseCase(pigRepository, preferencesRepository)
    }

    @Test
    fun `create pig validates blank name and rejects it`() = runTest {
        val result = createPigUseCase(name = "   ")
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("cannot be blank")
    }

    @Test
    fun `create pig validates name length exceeding 30 chars`() = runTest {
        val longName = "A".repeat(31)
        val result = createPigUseCase(name = longName)
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("30 characters")
    }

    @Test
    fun `create pig with valid name creates pig and selects it`() = runTest {
        coEvery { pigRepository.createPig(any()) } returns 3L

        val result = createPigUseCase(name = "Vacation")
        assertThat(result.isSuccess).isTrue()
        val created = result.getOrNull()
        assertThat(created?.id).isEqualTo(3L)
        assertThat(created?.name).isEqualTo("Vacation")

        // Verifies the newly created pig is set as selected in preferences
        coVerify { preferencesRepository.setSelectedPigId(3L) }
        assertThat(selectedPigIdFlow.value).isEqualTo(3L)
    }

    @Test
    fun `update pig validates blank name and rejects it`() = runTest {
        val result = updatePigUseCase(pigId = 1L, name = "")
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("cannot be blank")
    }

    @Test
    fun `update pig succeeds for existing pig and calls repository`() = runTest {
        coEvery { pigRepository.getPigByIdOnce(1L) } returns defaultPig

        val result = updatePigUseCase(pigId = 1L, name = "Main Savings")
        assertThat(result.isSuccess).isTrue()
        coVerify {
            pigRepository.updatePig(
                match { it.id == 1L && it.name == "Main Savings" && it.balance == Money.RS_100 }
            )
        }
    }

    @Test
    fun `delete pig prevents deleting the only remaining pig in CLINK`() = runTest {
        allPigsFlow.value = listOf(defaultPig)
        coEvery { pigRepository.getPigByIdOnce(1L) } returns defaultPig

        val result = deletePigUseCase(pigId = 1L)
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Cannot delete your only pig")
        coVerify(exactly = 0) { pigRepository.deletePig(any()) }
    }

    @Test
    fun `delete pig succeeds when multiple pigs exist and updates selected pig fallback`() = runTest {
        allPigsFlow.value = listOf(defaultPig, emergencyPig)
        coEvery { pigRepository.getPigByIdOnce(2L) } returns emergencyPig
        selectedPigIdFlow.value = 2L // Currently selected is Emergency Pig

        val result = deletePigUseCase(pigId = 2L)
        assertThat(result.isSuccess).isTrue()
        coVerify { pigRepository.deletePig(2L) }

        // Selected pig should fall back to surviving pig (ID 1)
        assertThat(selectedPigIdFlow.value).isEqualTo(1L)
    }

    @Test
    fun `select pig validates existence before updating preferences`() = runTest {
        coEvery { pigRepository.getPigByIdOnce(999L) } returns null

        val result = selectPigUseCase(999L)
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("does not exist")
    }

    @Test
    fun `getSelectedPig reactively resolves stored ID or falls back to first pig`() = runTest {
        allPigsFlow.value = listOf(defaultPig, emergencyPig)
        coEvery { pigRepository.getOrCreateDefaultPig() } returns defaultPig

        // Stored ID is null -> should resolve to first pig (defaultPig)
        val resolvedFirst = getSelectedPigUseCase().first()
        assertThat(resolvedFirst?.id).isEqualTo(1L)

        // Switch stored ID to emergencyPig (2L)
        selectedPigIdFlow.value = 2L
        val resolvedSecond = getSelectedPigUseCase().first()
        assertThat(resolvedSecond?.id).isEqualTo(2L)
    }
}
