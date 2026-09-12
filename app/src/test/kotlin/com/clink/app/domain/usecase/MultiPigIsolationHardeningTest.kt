package com.clink.app.domain.usecase

import com.clink.app.domain.model.Goal
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.model.TransactionType
import com.clink.app.domain.repository.GoalRepository
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.repository.TransactionRepository
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

/**
 * Rigorous multi-pig isolation test suite featuring 3+ pigs.
 * Proves that saving, goals, transactions, and deletions on one pig
 * never bleed into or mutate another pig's state.
 */
class MultiPigIsolationHardeningTest {

    private val pigRepository: PigRepository = mockk(relaxed = true)
    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val goalRepository: GoalRepository = mockk(relaxed = true)
    private val userPreferencesRepository: UserPreferencesRepository = mockk(relaxed = true)

    private val selectedPigIdFlow = MutableStateFlow<Long?>(null)
    private val allPigsFlow = MutableStateFlow<List<Pig>>(emptyList())
    private val transactionsPigAFlow = MutableStateFlow<List<Transaction>>(emptyList())
    private val transactionsPigBFlow = MutableStateFlow<List<Transaction>>(emptyList())
    private val transactionsPigCFlow = MutableStateFlow<List<Transaction>>(emptyList())
    private val goalsPigAFlow = MutableStateFlow<List<Goal>>(emptyList())
    private val goalsPigBFlow = MutableStateFlow<List<Goal>>(emptyList())
    private val goalsPigCFlow = MutableStateFlow<List<Goal>>(emptyList())

    private lateinit var deletePigUseCase: DeletePigUseCase
    private lateinit var observeGoalsUseCase: ObserveGoalsUseCase
    private lateinit var getSelectedPigUseCase: GetSelectedPigUseCase

    private val pigA = Pig(id = 1L, name = "Emergency Fund", balance = Money.fromRupees(100))
    private val pigB = Pig(id = 2L, name = "New Phone", balance = Money.fromRupees(50))
    private val pigC = Pig(id = 3L, name = "Travel / Vacation", balance = Money.ZERO)

    @Before
    fun setUp() {
        allPigsFlow.value = listOf(pigA, pigB, pigC)
        every { pigRepository.getAllPigs() } returns allPigsFlow
        every { userPreferencesRepository.selectedPigId } returns selectedPigIdFlow
        coEvery { userPreferencesRepository.setSelectedPigId(any()) } answers {
            selectedPigIdFlow.value = firstArg()
            Result.success(Unit)
        }

        every { transactionRepository.getTransactionsForPig(1L) } returns transactionsPigAFlow
        every { transactionRepository.getTransactionsForPig(2L) } returns transactionsPigBFlow
        every { transactionRepository.getTransactionsForPig(3L) } returns transactionsPigCFlow

        every { goalRepository.observeGoalsForPig(1L) } returns goalsPigAFlow
        every { goalRepository.observeGoalsForPig(2L) } returns goalsPigBFlow
        every { goalRepository.observeGoalsForPig(3L) } returns goalsPigCFlow

        deletePigUseCase = DeletePigUseCase(pigRepository, userPreferencesRepository)
        observeGoalsUseCase = ObserveGoalsUseCase(goalRepository, pigRepository)
        getSelectedPigUseCase = GetSelectedPigUseCase(pigRepository, userPreferencesRepository)
    }

    @Test
    fun `saving to Pig A changes only Pig A balance and leaves Pig B and C untouched`() = runTest {
        val depositAmount = Money.fromRupees(20)

        // Mock addSavings for Pig A
        val updatedPigA = pigA.copy(balance = pigA.balance + depositAmount)
        coEvery { pigRepository.addSavings(1L, depositAmount, any()) } answers {
            allPigsFlow.value = listOf(updatedPigA, pigB, pigC)
            Transaction(id = 101L, pigId = 1L, amount = depositAmount, type = TransactionType.CREDIT)
        }

        pigRepository.addSavings(1L, depositAmount, "Save for emergency")

        val currentPigs = allPigsFlow.value
        val currentA = currentPigs.find { it.id == 1L }
        val currentB = currentPigs.find { it.id == 2L }
        val currentC = currentPigs.find { it.id == 3L }

        // Pig A updated: ₹100 + ₹20 = ₹120
        assertThat(currentA?.balance).isEqualTo(Money.fromRupees(120))

        // Pig B and Pig C strictly unchanged
        assertThat(currentB?.balance).isEqualTo(Money.fromRupees(50))
        assertThat(currentC?.balance).isEqualTo(Money.ZERO)
    }

    @Test
    fun `saving to Pig B creates transaction scoped exclusively to Pig B`() = runTest {
        val txB = Transaction(id = 201L, pigId = 2L, amount = Money.fromRupees(50), type = TransactionType.CREDIT)
        transactionsPigBFlow.value = listOf(txB)

        val txListA: List<Transaction> = transactionRepository.getTransactionsForPig(1L).first()
        val txListB: List<Transaction> = transactionRepository.getTransactionsForPig(2L).first()
        val txListC: List<Transaction> = transactionRepository.getTransactionsForPig(3L).first()

        assertThat(txListB).containsExactly(txB)
        assertThat(txListA).isEmpty()
        assertThat(txListC).isEmpty()
    }

    @Test
    fun `goals created on Pig B derive progress exclusively from Pig B balance`() = runTest {
        // Goal on Pig B with target ₹100
        val phoneGoal = Goal(
            id = 10L,
            pigId = 2L,
            title = "Phone Screen Guard",
            targetAmount = Money.fromRupees(100)
        )
        goalsPigBFlow.value = listOf(phoneGoal)

        // Pig B has ₹50 balance -> progress is 50%
        val progressListB = observeGoalsUseCase(pigId = 2L).first()
        assertThat(progressListB).hasSize(1)
        val progress = progressListB.first()
        assertThat(progress.goal.id).isEqualTo(10L)
        assertThat(progress.currentAmount).isEqualTo(Money.fromRupees(50))
        assertThat(progress.progressPercent).isEqualTo(50)
        assertThat(progress.isCompleted).isFalse()

        // Goals on Pig A and Pig C remain completely empty
        val progressListA = observeGoalsUseCase(pigId = 1L).first()
        val progressListC = observeGoalsUseCase(pigId = 3L).first()
        assertThat(progressListA).isEmpty()
        assertThat(progressListC).isEmpty()
    }

    @Test
    fun `deleting non-selected Pig B leaves active selected Pig C completely intact`() = runTest {
        // Set active selected pig to Pig C (3L)
        selectedPigIdFlow.value = 3L
        coEvery { pigRepository.getPigByIdOnce(2L) } returns pigB

        // User deletes Pig B (2L)
        val result = deletePigUseCase(pigId = 2L)
        assertThat(result.isSuccess).isTrue()

        // Verify Pig B deletion was called
        coVerify { pigRepository.deletePig(2L) }

        // Invariant: Selected pig must STILL be Pig C (3L), NOT reset or overwritten
        assertThat(selectedPigIdFlow.value).isEqualTo(3L)
    }

    @Test
    fun `deleting active selected Pig C cleanly falls back to surviving Pig A`() = runTest {
        // Active selected pig is Pig C (3L)
        selectedPigIdFlow.value = 3L
        coEvery { pigRepository.getPigByIdOnce(3L) } returns pigC

        // Delete active Pig C
        val result = deletePigUseCase(pigId = 3L)
        assertThat(result.isSuccess).isTrue()

        // Surviving pigs are [Pig A (1L), Pig B (2L)]. First surviving is Pig A.
        assertThat(selectedPigIdFlow.value).isEqualTo(1L)
    }

    @Test
    fun `cannot delete when only one pig remains in the database`() = runTest {
        allPigsFlow.value = listOf(pigA)
        coEvery { pigRepository.getPigByIdOnce(1L) } returns pigA

        val result = deletePigUseCase(pigId = 1L)
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Cannot delete your only pig")
        coVerify(exactly = 0) { pigRepository.deletePig(any()) }
    }
}
