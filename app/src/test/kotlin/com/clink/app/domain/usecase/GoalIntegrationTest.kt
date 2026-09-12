package com.clink.app.domain.usecase

import com.clink.app.data.local.ClinkDatabase
import com.clink.app.data.local.dao.GoalDao
import com.clink.app.data.local.dao.PigDao
import com.clink.app.data.local.dao.TransactionDao
import com.clink.app.data.local.entity.GoalEntity
import com.clink.app.data.local.entity.PigEntity
import com.clink.app.data.local.entity.TransactionEntity
import com.clink.app.data.repository.GoalRepositoryImpl
import com.clink.app.data.repository.PigRepositoryImpl
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.TransactionType
import com.clink.app.domain.repository.PaymentMethod
import com.clink.app.domain.repository.PaymentRepository
import com.clink.app.domain.repository.PaymentResult
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * End-to-end integration test verifying the complete savings-to-goal reactive loop:
 * 1. Create a goal (Pig = ₹0, Goal = ₹500)
 * 2. Save money (Save ₹100 -> Pig = ₹100, Goal = ₹100/₹500, Progress = 20%)
 * 3. Save more money (Save ₹400 -> Pig = ₹500, Goal = ₹500/₹500, Progress = 100%, Completed = true, Remaining = ₹0)
 * 4. Delete the goal -> Goal removed, but Pig balance and Transactions remain completely UNCHANGED.
 */
class GoalIntegrationTest {

    private val pigTable = mutableMapOf<Long, PigEntity>()
    private val transactionTable = mutableListOf<TransactionEntity>()
    private val goalTable = mutableMapOf<Long, GoalEntity>()

    private val pigsFlow = MutableStateFlow<List<PigEntity>>(emptyList())
    private val transactionsFlow = MutableStateFlow<List<TransactionEntity>>(emptyList())
    private val goalsFlow = MutableStateFlow<List<GoalEntity>>(emptyList())

    private var nextPigId = 1L
    private var nextTxId = 1L
    private var nextGoalId = 1L

    private lateinit var mockDatabase: ClinkDatabase
    private lateinit var mockPigDao: PigDao
    private lateinit var mockTxDao: TransactionDao
    private lateinit var mockGoalDao: GoalDao
    private lateinit var mockPaymentRepo: PaymentRepository

    private lateinit var pigRepository: PigRepositoryImpl
    private lateinit var goalRepository: GoalRepositoryImpl

    private lateinit var addMoneyUseCase: AddMoneyUseCase
    private lateinit var createGoalUseCase: CreateGoalUseCase
    private lateinit var observeGoalsUseCase: ObserveGoalsUseCase
    private lateinit var deleteGoalUseCase: DeleteGoalUseCase

    @Before
    fun setUp() {
        pigTable.clear()
        transactionTable.clear()
        goalTable.clear()

        nextPigId = 1L
        nextTxId = 1L
        nextGoalId = 1L

        mockDatabase = mockk(relaxed = true)
        mockPigDao = mockk(relaxed = true)
        mockTxDao = mockk(relaxed = true)
        mockGoalDao = mockk(relaxed = true)
        mockPaymentRepo = mockk(relaxed = true)

        // Setup Pig DAO
        coEvery { mockPigDao.getPigByIdOnce(any()) } answers {
            val id = firstArg<Long>()
            pigTable[id]
        }
        coEvery { mockPigDao.updateBalance(any(), any(), any()) } answers {
            val id = firstArg<Long>()
            val newPaise = secondArg<Long>()
            val existing = pigTable[id]
            if (existing != null) {
                val updated = existing.copy(balancePaise = newPaise)
                pigTable[id] = updated
                pigsFlow.value = pigTable.values.toList()
            }
        }
        coEvery { mockPigDao.insertPig(any()) } answers {
            val entity = firstArg<PigEntity>()
            val assignedId = if (entity.id == 0L) nextPigId++ else entity.id
            val saved = entity.copy(id = assignedId)
            pigTable[assignedId] = saved
            pigsFlow.value = pigTable.values.toList()
            assignedId
        }
        every { mockPigDao.getAllPigs() } returns pigsFlow
        every { mockPigDao.getPigById(any()) } answers {
            val id = firstArg<Long>()
            MutableStateFlow(pigTable[id])
        }

        // Setup Transaction DAO
        coEvery { mockTxDao.insertTransaction(any()) } answers {
            val entity = firstArg<TransactionEntity>()
            val assignedId = if (entity.id == 0L) nextTxId++ else entity.id
            val saved = entity.copy(id = assignedId)
            transactionTable.add(saved)
            transactionsFlow.value = transactionTable.toList()
            assignedId
        }

        // Setup Goal DAO
        coEvery { mockGoalDao.insertGoal(any()) } answers {
            val entity = firstArg<GoalEntity>()
            val assignedId = if (entity.id == 0L) nextGoalId++ else entity.id
            val saved = entity.copy(id = assignedId)
            goalTable[assignedId] = saved
            goalsFlow.value = goalTable.values.toList()
            assignedId
        }
        coEvery { mockGoalDao.getGoalById(any()) } answers {
            val id = firstArg<Long>()
            goalTable[id]
        }
        coEvery { mockGoalDao.deleteGoal(any()) } answers {
            val id = firstArg<Long>()
            goalTable.remove(id)
            goalsFlow.value = goalTable.values.toList()
        }
        every { mockGoalDao.getAllGoals() } returns goalsFlow
        every { mockGoalDao.getGoalsForPig(any()) } answers {
            val pigId = firstArg<Long>()
            MutableStateFlow(goalTable.values.filter { it.pigId == pigId })
        }

        // Payment repo simulation with explicit Money instances
        coEvery {
            mockPaymentRepo.processDeposit(1L, Money(10000L), any())
        } returns PaymentResult.Success("TX-MOCK-1", Money(10000L))
        coEvery {
            mockPaymentRepo.processDeposit(1L, Money(40000L), any())
        } returns PaymentResult.Success("TX-MOCK-2", Money(40000L))

        // Repositories with direct transaction runner
        pigRepository = PigRepositoryImpl(
            database = mockDatabase,
            pigDao = mockPigDao,
            transactionDao = mockTxDao
        ).apply {
            transactionRunner = { block -> block() }
        }
        goalRepository = GoalRepositoryImpl(mockGoalDao)

        addMoneyUseCase = AddMoneyUseCase(
            pigRepository = pigRepository,
            transactionRepository = mockk(relaxed = true),
            paymentRepository = mockPaymentRepo
        )
        createGoalUseCase = CreateGoalUseCase(goalRepository, pigRepository)
        observeGoalsUseCase = ObserveGoalsUseCase(goalRepository, pigRepository)
        deleteGoalUseCase = DeleteGoalUseCase(goalRepository)

        // Seed initial pig with ₹0 balance
        val initialPig = PigEntity(
            id = 1L,
            name = "Test Pig",
            balancePaise = 0L,
            targetAmountPaise = null,
            iconName = "piggy",
            colorHex = "#E91E63",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        pigTable[1L] = initialPig
        pigsFlow.value = listOf(initialPig)
    }

    @Test
    fun `full goal savings lifecycle with reactive progress updates and safe deletion`() = runTest {
        // Step 1: Create Goal of ₹500 (50,000 paise)
        val createResult = createGoalUseCase(
            pigId = 1L,
            title = "New Headphones",
            targetAmount = Money(50000L)
        )
        assertThat(createResult.isSuccess).isTrue()
        val createdGoal = createResult.getOrThrow()

        // Verify initial state: Pig = ₹0, Goal = ₹0 / ₹500, Progress = 0%
        val initialProgressList = observeGoalsUseCase(1L).first()
        assertThat(initialProgressList).hasSize(1)
        val progressStep1 = initialProgressList.first()
        assertThat(progressStep1.currentAmount).isEqualTo(Money.ZERO)
        assertThat(progressStep1.targetAmount).isEqualTo(Money(50000L))
        assertThat(progressStep1.progressPercent).isEqualTo(0)
        assertThat(progressStep1.remainingAmount).isEqualTo(Money(50000L))
        assertThat(progressStep1.isCompleted).isFalse()

        // Step 2: Save ₹100 (10,000 paise) via AddMoneyUseCase
        val saveResult1 = addMoneyUseCase(
            pigId = 1L,
            amount = Money(10000L),
            note = "First savings"
        )
        assertThat(saveResult1.isSuccess).isTrue()

        // Verify progress updates automatically: ₹100 / ₹500 = 20%
        val progressStep2 = observeGoalsUseCase(1L).first().first()
        assertThat(progressStep2.currentAmount).isEqualTo(Money(10000L))
        assertThat(progressStep2.targetAmount).isEqualTo(Money(50000L))
        assertThat(progressStep2.progressPercent).isEqualTo(20)
        assertThat(progressStep2.remainingAmount).isEqualTo(Money(40000L))
        assertThat(progressStep2.isCompleted).isFalse()

        // Step 3: Save another ₹400 (40,000 paise) via AddMoneyUseCase
        val saveResult2 = addMoneyUseCase(
            pigId = 1L,
            amount = Money(40000L),
            note = "Second savings"
        )
        assertThat(saveResult2.isSuccess).isTrue()

        // Verify completion state: ₹500 / ₹500, 100%, Completed = true, Remaining = ₹0
        val progressStep3 = observeGoalsUseCase(1L).first().first()
        assertThat(progressStep3.currentAmount).isEqualTo(Money(50000L))
        assertThat(progressStep3.targetAmount).isEqualTo(Money(50000L))
        assertThat(progressStep3.progressPercent).isEqualTo(100)
        assertThat(progressStep3.remainingAmount).isEqualTo(Money.ZERO)
        assertThat(progressStep3.isCompleted).isTrue()

        // Step 4: Delete the goal
        val deleteResult = deleteGoalUseCase(createdGoal.id)
        assertThat(deleteResult.isSuccess).isTrue()

        // Verify goal is gone from goals
        val progressAfterDelete = observeGoalsUseCase(1L).first()
        assertThat(progressAfterDelete).isEmpty()

        // CRITICAL CHECK: Pig balance and transactions MUST remain intact!
        val pigAfterDelete = pigRepository.getPigByIdOnce(1L)
        assertThat(pigAfterDelete).isNotNull()
        assertThat(pigAfterDelete?.balance).isEqualTo(Money(50000L)) // Still ₹500!

        val transactionsAfterDelete = transactionTable
        assertThat(transactionsAfterDelete).hasSize(2) // 2 transactions preserved!
        assertThat(transactionsAfterDelete[0].amountPaise).isEqualTo(10000L)
        assertThat(transactionsAfterDelete[1].amountPaise).isEqualTo(40000L)
    }
}
