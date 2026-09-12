package com.clink.app.domain.usecase

import com.clink.app.domain.model.Goal
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.model.TransactionStatus
import com.clink.app.domain.model.TransactionType
import com.clink.app.domain.repository.GoalRepository
import com.clink.app.domain.repository.PaymentMethod
import com.clink.app.domain.repository.PaymentRepository
import com.clink.app.domain.repository.PaymentResult
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.repository.TransactionRepository
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

class MultiPigIsolationTest {

    private val pigRepository: PigRepository = mockk(relaxed = true)
    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val paymentRepository: PaymentRepository = mockk(relaxed = true)
    private val goalRepository: GoalRepository = mockk(relaxed = true)

    private lateinit var addMoneyUseCase: AddMoneyUseCase
    private lateinit var getTransactionsUseCase: GetTransactionsUseCase
    private lateinit var observeGoalsUseCase: ObserveGoalsUseCase

    private val pigA = Pig(id = 1L, name = "Emergency Fund", balance = Money.fromRupees(100))
    private val pigB = Pig(id = 2L, name = "New Phone", balance = Money.fromRupees(50))

    private val pigAPigsFlow = MutableStateFlow(listOf(pigA, pigB))
    private val pigATransactions = MutableStateFlow<List<Transaction>>(emptyList())
    private val pigBTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    private val allGoalsFlow = MutableStateFlow<List<Goal>>(emptyList())

    @Before
    fun setUp() {
        addMoneyUseCase = AddMoneyUseCase(pigRepository, transactionRepository, paymentRepository)
        getTransactionsUseCase = GetTransactionsUseCase(transactionRepository)
        observeGoalsUseCase = ObserveGoalsUseCase(goalRepository, pigRepository)

        every { pigRepository.getAllPigs() } returns pigAPigsFlow
        every { transactionRepository.getTransactionsForPig(1L) } returns pigATransactions
        every { transactionRepository.getTransactionsForPig(2L) } returns pigBTransactions
        every { transactionRepository.observeTransactions(1L) } returns pigATransactions
        every { transactionRepository.observeTransactions(2L) } returns pigBTransactions
        every { goalRepository.observeAllGoals() } returns allGoalsFlow
        every { goalRepository.observeGoalsForPig(1L) } returns MutableStateFlow(emptyList())
        every { goalRepository.observeGoalsForPig(2L) } returns MutableStateFlow(emptyList())
    }

    @Test
    fun `saving to Pig A updates Pig A and records Pig A transaction without altering Pig B`() = runTest {
        val saveAmount = Money.fromRupees(20)
        val expectedPigATx = Transaction(
            id = 101L,
            pigId = 1L,
            amount = saveAmount,
            type = TransactionType.CREDIT,
            status = TransactionStatus.COMPLETED,
            note = "Add to Emergency Fund"
        )

        coEvery { pigRepository.getPigByIdOnce(1L) } returns pigA
        coEvery { pigRepository.getPigByIdOnce(2L) } returns pigB
        coEvery {
            paymentRepository.processDeposit(1L, saveAmount, PaymentMethod.SIMULATED_UPI)
        } returns PaymentResult.Success("REF-123", saveAmount)
        coEvery {
            pigRepository.addSavings(1L, saveAmount, "Add to Emergency Fund")
        } returns expectedPigATx

        val result = addMoneyUseCase(
            pigId = 1L,
            amount = saveAmount,
            note = "Add to Emergency Fund"
        )

        assertThat(result.isSuccess).isTrue()

        // Verify savings strictly added to Pig A
        coVerify(exactly = 1) { pigRepository.addSavings(1L, saveAmount, any()) }

        // Verify Pig B was NEVER modified
        coVerify(exactly = 0) { pigRepository.addSavings(2L, saveAmount, any()) }
        coVerify(exactly = 0) { pigRepository.updatePig(match { it.id == 2L }) }
    }

    @Test
    fun `transaction history for Pig A excludes transactions belonging to Pig B`() = runTest {
        val txA = Transaction(
            id = 1L,
            pigId = 1L,
            amount = Money.fromRupees(20),
            type = TransactionType.CREDIT,
            status = TransactionStatus.COMPLETED,
            note = "Pig A note"
        )
        val txB = Transaction(
            id = 2L,
            pigId = 2L,
            amount = Money.fromRupees(50),
            type = TransactionType.CREDIT,
            status = TransactionStatus.COMPLETED,
            note = "Pig B note"
        )

        pigATransactions.value = listOf(txA)
        pigBTransactions.value = listOf(txB)

        val retrievedA = getTransactionsUseCase(pigId = 1L).first()
        val retrievedB = getTransactionsUseCase(pigId = 2L).first()

        assertThat(retrievedA).containsExactly(txA)
        assertThat(retrievedA).doesNotContain(txB)

        assertThat(retrievedB).containsExactly(txB)
        assertThat(retrievedB).doesNotContain(txA)
    }

    @Test
    fun `goal progress is isolated strictly per pig`() = runTest {
        // Goal A is attached to Pig A (id=1L, balance ₹100, target ₹200)
        val goalA = Goal(
            id = 10L,
            pigId = 1L,
            title = "Emergency Target",
            targetAmount = Money.fromRupees(200),
            createdAt = 1000L
        )

        // Goal B is attached to Pig B (id=2L, balance ₹50, target ₹500)
        val goalB = Goal(
            id = 20L,
            pigId = 2L,
            title = "Phone Target",
            targetAmount = Money.fromRupees(500),
            createdAt = 1000L
        )

        allGoalsFlow.value = listOf(goalA, goalB)

        val progressList = observeGoalsUseCase().first()
        val progressA = progressList.first { it.goal.id == 10L }
        val progressB = progressList.first { it.goal.id == 20L }

        // Goal A must strictly evaluate against Pig A balance (₹100 / ₹200 = 50%)
        assertThat(progressA.currentAmount).isEqualTo(Money.fromRupees(100))
        assertThat(progressA.progressPercent).isEqualTo(50)
        assertThat(progressA.isCompleted).isFalse()

        // Goal B must strictly evaluate against Pig B balance (₹50 / ₹500 = 10%)
        assertThat(progressB.currentAmount).isEqualTo(Money.fromRupees(50))
        assertThat(progressB.progressPercent).isEqualTo(10)
        assertThat(progressB.isCompleted).isFalse()

        // Now simulate Pig A balance increasing to ₹200 (completing Goal A)
        val updatedPigA = pigA.copy(balance = Money.fromRupees(200))
        pigAPigsFlow.value = listOf(updatedPigA, pigB)

        val updatedProgressList = observeGoalsUseCase().first()
        val updatedProgressA = updatedProgressList.first { it.goal.id == 10L }
        val updatedProgressB = updatedProgressList.first { it.goal.id == 20L }

        // Goal A is completed
        assertThat(updatedProgressA.currentAmount).isEqualTo(Money.fromRupees(200))
        assertThat(updatedProgressA.isCompleted).isTrue()

        // Goal B is completely unaffected (still ₹50, 10%)
        assertThat(updatedProgressB.currentAmount).isEqualTo(Money.fromRupees(50))
        assertThat(updatedProgressB.progressPercent).isEqualTo(10)
        assertThat(updatedProgressB.isCompleted).isFalse()
    }
}
