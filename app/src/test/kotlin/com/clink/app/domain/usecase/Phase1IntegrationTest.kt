package com.clink.app.domain.usecase

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.clink.app.data.local.ClinkDatabase
import com.clink.app.data.local.dao.GoalDao
import com.clink.app.data.local.dao.PigDao
import com.clink.app.data.local.dao.TransactionDao
import com.clink.app.data.local.entity.GoalEntity
import com.clink.app.data.local.entity.PigEntity
import com.clink.app.data.local.entity.TransactionEntity
import com.clink.app.data.preferences.UserPreferencesRepository
import com.clink.app.data.repository.FakePaymentRepository
import com.clink.app.data.repository.GoalRepositoryImpl
import com.clink.app.data.repository.PigRepositoryImpl
import com.clink.app.data.repository.TransactionRepositoryImpl
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.PigState
import com.clink.app.domain.model.TransactionType
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * TASK-015 Phase 1 End-to-End Integration Test.
 *
 * Verifies the complete interconnected user journey across Clean Architecture layers:
 * 1. Onboarding verification (first-launch incomplete -> completed).
 * 2. Primary pig initialization (idempotent, ₹0 initial balance).
 * 3. Multi-pig creation ("Vacation", "Gadgets", "Emergency") with persistent selection.
 * 4. Isolated savings (Pig A: ₹100, Pig B: ₹250, Pig C: ₹500) with zero balance cross-talk.
 * 5. Authoritative transaction records with notes and timestamps.
 * 6. Scoped goals progression (Goal for Pig A: 20% -> 100% on subsequent save; Pig B and C untouched).
 * 7. Safe pig deletion with SQLite CASCADE cleanup of transactions and goals.
 * 8. Active selection fallback upon deletion and sole-pig deletion protection.
 * 9. Persistence reload across simulated application restarts.
 */
class Phase1IntegrationTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // Simulated SQLite tables
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

    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var paymentRepository: FakePaymentRepository
    private lateinit var pigRepository: PigRepositoryImpl
    private lateinit var transactionRepository: TransactionRepositoryImpl
    private lateinit var goalRepository: GoalRepositoryImpl

    // Domain Use Cases
    private lateinit var getOnboardingStateUseCase: GetOnboardingStateUseCase
    private lateinit var completeOnboardingUseCase: CompleteOnboardingUseCase
    private lateinit var createPigUseCase: CreatePigUseCase
    private lateinit var getSelectedPigUseCase: GetSelectedPigUseCase
    private lateinit var selectPigUseCase: SelectPigUseCase
    private lateinit var updatePigUseCase: UpdatePigUseCase
    private lateinit var deletePigUseCase: DeletePigUseCase
    private lateinit var addMoneyUseCase: AddMoneyUseCase
    private lateinit var getTransactionsUseCase: GetTransactionsUseCase
    private lateinit var createGoalUseCase: CreateGoalUseCase
    private lateinit var observeGoalsUseCase: ObserveGoalsUseCase
    private lateinit var deleteGoalUseCase: DeleteGoalUseCase

    @Before
    fun setUp() {
        val testDataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("phase1_test_${System.nanoTime()}.preferences_pb") }
        )
        userPreferencesRepository = UserPreferencesRepository(testDataStore)
        paymentRepository = FakePaymentRepository()

        setupMockDaos()
        setupRepositoriesAndUseCases()
    }

    private fun setupMockDaos() {
        mockDatabase = mockk(relaxed = true)
        mockPigDao = mockk(relaxed = true)
        mockTxDao = mockk(relaxed = true)
        mockGoalDao = mockk(relaxed = true)

        // PigDao bindings
        every { mockPigDao.getAllPigs() } returns pigsFlow
        every { mockPigDao.getPigById(any()) } answers {
            val id = firstArg<Long>()
            pigsFlow.map { list -> list.find { it.id == id } }
        }
        coEvery { mockPigDao.getPigByIdOnce(any()) } answers {
            pigTable[firstArg<Long>()]
        }
        coEvery { mockPigDao.getFirstPig() } answers {
            pigTable.values.minByOrNull { it.id }
        }
        coEvery { mockPigDao.getPigCount() } answers {
            pigTable.size
        }
        coEvery { mockPigDao.insertPig(any()) } answers {
            val entity = firstArg<PigEntity>()
            val id = if (entity.id == 0L) nextPigId++ else entity.id
            val inserted = entity.copy(id = id)
            pigTable[id] = inserted
            syncFlows()
            id
        }
        coEvery { mockPigDao.updatePig(any()) } answers {
            val entity = firstArg<PigEntity>()
            pigTable[entity.id] = entity
            syncFlows()
        }
        coEvery { mockPigDao.updateBalance(any(), any(), any()) } answers {
            val id = firstArg<Long>()
            val newPaise = secondArg<Long>()
            pigTable[id]?.let {
                pigTable[id] = it.copy(balancePaise = newPaise)
                syncFlows()
            }
        }
        coEvery { mockPigDao.deletePig(any()) } answers {
            val id = firstArg<Long>()
            pigTable.remove(id)
            // SQLite CASCADE semantics
            transactionTable.removeAll { it.pigId == id }
            val goalsToRemove = goalTable.filter { it.value.pigId == id }.keys
            goalsToRemove.forEach { goalTable.remove(it) }
            syncFlows()
        }

        // TransactionDao bindings
        every { mockTxDao.getTransactionsForPig(any()) } answers {
            val pId = firstArg<Long>()
            transactionsFlow.map { list -> list.filter { it.pigId == pId } }
        }
        every { mockTxDao.getAllTransactions() } returns transactionsFlow
        coEvery { mockTxDao.insertTransaction(any()) } answers {
            val entity = firstArg<TransactionEntity>()
            val id = if (entity.id == 0L) nextTxId++ else entity.id
            val inserted = entity.copy(id = id)
            transactionTable.add(0, inserted)
            syncFlows()
            id
        }

        // GoalDao bindings
        every { mockGoalDao.getGoalsForPig(any()) } answers {
            val pId = firstArg<Long>()
            goalsFlow.map { list -> list.filter { it.pigId == pId } }
        }
        every { mockGoalDao.getAllGoals() } returns goalsFlow
        coEvery { mockGoalDao.getGoalById(any()) } answers {
            goalTable[firstArg<Long>()]
        }
        coEvery { mockGoalDao.insertGoal(any()) } answers {
            val entity = firstArg<GoalEntity>()
            val id = if (entity.id == 0L) nextGoalId++ else entity.id
            val inserted = entity.copy(id = id)
            goalTable[id] = inserted
            syncFlows()
            id
        }
        coEvery { mockGoalDao.deleteGoal(any()) } answers {
            val id = firstArg<Long>()
            goalTable.remove(id)
            syncFlows()
        }
    }

    private fun setupRepositoriesAndUseCases() {
        pigRepository = PigRepositoryImpl(
            database = mockDatabase,
            pigDao = mockPigDao,
            transactionDao = mockTxDao
        ).apply {
            transactionRunner = { block -> block() }
        }
        transactionRepository = TransactionRepositoryImpl(mockTxDao)
        goalRepository = GoalRepositoryImpl(mockGoalDao)

        getOnboardingStateUseCase = GetOnboardingStateUseCase(userPreferencesRepository)
        completeOnboardingUseCase = CompleteOnboardingUseCase(userPreferencesRepository)

        createPigUseCase = CreatePigUseCase(pigRepository, userPreferencesRepository)
        getSelectedPigUseCase = GetSelectedPigUseCase(pigRepository, userPreferencesRepository)
        selectPigUseCase = SelectPigUseCase(pigRepository, userPreferencesRepository)
        updatePigUseCase = UpdatePigUseCase(pigRepository)
        deletePigUseCase = DeletePigUseCase(pigRepository, userPreferencesRepository)

        addMoneyUseCase = AddMoneyUseCase(pigRepository, transactionRepository, paymentRepository)
        getTransactionsUseCase = GetTransactionsUseCase(transactionRepository)

        createGoalUseCase = CreateGoalUseCase(goalRepository, pigRepository)
        observeGoalsUseCase = ObserveGoalsUseCase(goalRepository, pigRepository)
        deleteGoalUseCase = DeleteGoalUseCase(goalRepository)
    }

    private fun syncFlows() {
        pigsFlow.value = pigTable.values.toList()
        transactionsFlow.value = transactionTable.toList()
        goalsFlow.value = goalTable.values.toList()
    }

    @Test
    fun `complete Phase 1 end-to-end integration journey`() = runTest(testDispatcher) {
        // --------------------------------------------------------------------
        // Step 1: Fresh install state & Onboarding
        // --------------------------------------------------------------------
        val initialOnboarding = getOnboardingStateUseCase().first()
        assertThat(initialOnboarding).isFalse()

        // Complete onboarding
        val onboardingResult = completeOnboardingUseCase()
        assertThat(onboardingResult.isSuccess).isTrue()
        assertThat(getOnboardingStateUseCase().first()).isTrue()

        // --------------------------------------------------------------------
        // Step 2: Initialize default pig
        // --------------------------------------------------------------------
        val defaultPig = pigRepository.getOrCreateDefaultPig()
        assertThat(defaultPig.name).isEqualTo("Primary Pig")
        assertThat(defaultPig.balance).isEqualTo(Money.ZERO)
        assertThat(defaultPig.state).isEqualTo(PigState.NEW)
        assertThat(getTransactionsUseCase(defaultPig.id).first()).isEmpty()
        assertThat(observeGoalsUseCase(defaultPig.id).first()).isEmpty()

        // Active selection defaults to primary pig
        val initialSelected = getSelectedPigUseCase().first()
        assertThat(initialSelected?.id).isEqualTo(defaultPig.id)

        // --------------------------------------------------------------------
        // Step 3: Multi-Pig Creation (Pig A: Vacation, Pig B: Gadgets, Pig C: Emergency)
        // --------------------------------------------------------------------
        val pigAResult = createPigUseCase(name = "Vacation")
        assertThat(pigAResult.isSuccess).isTrue()
        val pigA = pigAResult.getOrThrow()
        val pigAId = pigA.id

        val pigBResult = createPigUseCase(name = "Gadgets")
        assertThat(pigBResult.isSuccess).isTrue()
        val pigB = pigBResult.getOrThrow()
        val pigBId = pigB.id

        val pigCResult = createPigUseCase(name = "Emergency")
        assertThat(pigCResult.isSuccess).isTrue()
        val pigC = pigCResult.getOrThrow()
        val pigCId = pigC.id

        // Verify all 4 pigs exist
        val allPigs = pigRepository.getAllPigs().first()
        assertThat(allPigs).hasSize(4)

        // The newly created pig (Pig C) was automatically set as active
        assertThat(getSelectedPigUseCase().first()?.id).isEqualTo(pigCId)

        // --------------------------------------------------------------------
        // Step 4: Financial Isolation - Save money independently to each pig
        // --------------------------------------------------------------------
        // Save ₹100 to Pig A
        val saveAResult = addMoneyUseCase(pigId = pigAId, amount = Money.fromRupees(100), note = "Flight tickets")
        assertThat(saveAResult.isSuccess).isTrue()

        // Save ₹250 to Pig B
        val saveBResult = addMoneyUseCase(pigId = pigBId, amount = Money.fromRupees(250), note = "Mechanical keyboard")
        assertThat(saveBResult.isSuccess).isTrue()

        // Save ₹500 to Pig C
        val saveCResult = addMoneyUseCase(pigId = pigCId, amount = Money.fromRupees(500), note = "Rainy day")
        assertThat(saveCResult.isSuccess).isTrue()

        // Verify independent balances
        val fetchedPigA = pigRepository.getPigByIdOnce(pigAId)!!
        val fetchedPigB = pigRepository.getPigByIdOnce(pigBId)!!
        val fetchedPigC = pigRepository.getPigByIdOnce(pigCId)!!

        assertThat(fetchedPigA.balance).isEqualTo(Money.fromRupees(100))
        assertThat(fetchedPigB.balance).isEqualTo(Money.fromRupees(250))
        assertThat(fetchedPigC.balance).isEqualTo(Money.fromRupees(500))

        // Default primary pig remained untouched at ₹0
        val refreshedDefaultPig = pigRepository.getPigByIdOnce(defaultPig.id)!!
        assertThat(refreshedDefaultPig.balance).isEqualTo(Money.ZERO)

        // --------------------------------------------------------------------
        // Step 5: Transaction History Scoping & Atomicity
        // --------------------------------------------------------------------
        val pigATxList = getTransactionsUseCase(pigAId).first()
        assertThat(pigATxList).hasSize(1)
        assertThat(pigATxList[0].amount).isEqualTo(Money.fromRupees(100))
        assertThat(pigATxList[0].note).isEqualTo("Flight tickets")
        assertThat(pigATxList[0].type).isEqualTo(TransactionType.CREDIT)

        val pigBTxList = getTransactionsUseCase(pigBId).first()
        assertThat(pigBTxList).hasSize(1)
        assertThat(pigBTxList[0].amount).isEqualTo(Money.fromRupees(250))
        assertThat(pigBTxList[0].note).isEqualTo("Mechanical keyboard")

        val pigCTxList = getTransactionsUseCase(pigCId).first()
        assertThat(pigCTxList).hasSize(1)
        assertThat(pigCTxList[0].amount).isEqualTo(Money.fromRupees(500))
        assertThat(pigCTxList[0].note).isEqualTo("Rainy day")

        // --------------------------------------------------------------------
        // Step 6: Scoped Goals Creation & Reactive Progress
        // --------------------------------------------------------------------
        // Pig A (balance ₹100): Target ₹500 ("Goa Trip") -> 20% progress
        val goalAResult = createGoalUseCase(pigId = pigAId, title = "Goa Trip", targetAmount = Money.fromRupees(500))
        assertThat(goalAResult.isSuccess).isTrue()
        val goalA = goalAResult.getOrThrow()
        val goalAId = goalA.id

        // Pig B (balance ₹250): Target ₹250 ("Keychron") -> 100% completed
        val goalBResult = createGoalUseCase(pigId = pigBId, title = "Keychron", targetAmount = Money.fromRupees(250))
        assertThat(goalBResult.isSuccess).isTrue()
        val goalB = goalBResult.getOrThrow()
        val goalBId = goalB.id

        // Verify Pig A goal state
        val pigAGoals = observeGoalsUseCase(pigAId).first()
        assertThat(pigAGoals).hasSize(1)
        assertThat(pigAGoals[0].goal.id).isEqualTo(goalAId)
        assertThat(pigAGoals[0].progressPercent).isEqualTo(20)
        assertThat(pigAGoals[0].progressFraction).isEqualTo(0.20f)
        assertThat(pigAGoals[0].remainingAmount).isEqualTo(Money.fromRupees(400))
        assertThat(pigAGoals[0].isCompleted).isFalse()

        // Verify Pig B goal state
        val pigBGoals = observeGoalsUseCase(pigBId).first()
        assertThat(pigBGoals).hasSize(1)
        assertThat(pigBGoals[0].progressPercent).isEqualTo(100)
        assertThat(pigBGoals[0].progressFraction).isEqualTo(1.0f)
        assertThat(pigBGoals[0].remainingAmount).isEqualTo(Money.ZERO)
        assertThat(pigBGoals[0].isCompleted).isTrue()

        // --------------------------------------------------------------------
        // Step 7: Multi-Step Savings Loop & Goal Completion on Pig A
        // --------------------------------------------------------------------
        // Save ₹400 more to Pig A -> Total balance becomes ₹500
        val saveA2Result = addMoneyUseCase(pigId = pigAId, amount = Money.fromRupees(400), note = "Extra savings")
        assertThat(saveA2Result.isSuccess).isTrue()

        val updatedPigA = pigRepository.getPigByIdOnce(pigAId)!!
        assertThat(updatedPigA.balance).isEqualTo(Money.fromRupees(500))
        assertThat(updatedPigA.state).isEqualTo(PigState.HEALTHY)

        // Goal for Pig A now completed
        val updatedPigAGoals = observeGoalsUseCase(pigAId).first()
        assertThat(updatedPigAGoals[0].progressPercent).isEqualTo(100)
        assertThat(updatedPigAGoals[0].isCompleted).isTrue()
        assertThat(updatedPigAGoals[0].remainingAmount).isEqualTo(Money.ZERO)

        // Pig B and Pig C goals and balances strictly untouched
        assertThat(pigRepository.getPigByIdOnce(pigBId)?.balance).isEqualTo(Money.fromRupees(250))
        assertThat(pigRepository.getPigByIdOnce(pigCId)?.balance).isEqualTo(Money.fromRupees(500))

        // --------------------------------------------------------------------
        // Step 8: Safe Deletion, Cascade & Selection Fallback
        // --------------------------------------------------------------------
        // Currently Pig C is active. Delete non-active Pig B:
        selectPigUseCase(pigCId)
        assertThat(getSelectedPigUseCase().first()?.id).isEqualTo(pigCId)

        val deleteBResult = deletePigUseCase(pigBId)
        assertThat(deleteBResult.isSuccess).isTrue()
        // Pig B is gone
        assertThat(pigRepository.getPigByIdOnce(pigBId)).isNull()
        // Pig B's transactions and goals are cascaded
        assertThat(getTransactionsUseCase(pigBId).first()).isEmpty()
        assertThat(observeGoalsUseCase(pigBId).first()).isEmpty()
        // Active selection remains Pig C
        assertThat(getSelectedPigUseCase().first()?.id).isEqualTo(pigCId)

        // Delete currently active Pig C:
        val deleteCResult = deletePigUseCase(pigCId)
        assertThat(deleteCResult.isSuccess).isTrue()
        assertThat(pigRepository.getPigByIdOnce(pigCId)).isNull()
        // Active selection safely falls back to a surviving pig (Pig A or Default Pig)
        val fallbackSelected = getSelectedPigUseCase().first()
        assertThat(fallbackSelected).isNotNull()
        assertThat(listOf(pigAId, defaultPig.id)).contains(fallbackSelected!!.id)

        // Delete default primary pig so only Pig A remains:
        deletePigUseCase(defaultPig.id)
        assertThat(pigRepository.getAllPigs().first()).hasSize(1)
        assertThat(getSelectedPigUseCase().first()?.id).isEqualTo(pigAId)

        // Sole-pig protection: attempting to delete sole remaining Pig A must be rejected
        val deleteSoleResult = deletePigUseCase(pigAId)
        assertThat(deleteSoleResult.isFailure).isTrue()
        assertThat(deleteSoleResult.exceptionOrNull()?.message).contains("Cannot delete your only pig")
        assertThat(pigRepository.getPigByIdOnce(pigAId)).isNotNull()

        // --------------------------------------------------------------------
        // Step 9: Persistence Reload Verification
        // --------------------------------------------------------------------
        // Re-instantiate repositories from the same tables simulating application restart
        val reloadedPigRepo = PigRepositoryImpl(
            database = mockDatabase,
            pigDao = mockPigDao,
            transactionDao = mockTxDao
        ).apply {
            transactionRunner = { block -> block() }
        }
        val reloadedTxRepo = TransactionRepositoryImpl(mockTxDao)
        val reloadedGoalRepo = GoalRepositoryImpl(mockGoalDao)

        // Onboarding remains completed
        assertThat(userPreferencesRepository.isOnboardingCompleted.first()).isTrue()

        // Pig A survives with full ₹500 balance and transactions
        val reloadedPigA = reloadedPigRepo.getPigByIdOnce(pigAId)!!
        assertThat(reloadedPigA.balance).isEqualTo(Money.fromRupees(500))
        assertThat(reloadedPigA.name).isEqualTo("Vacation")

        val reloadedTxs = reloadedTxRepo.getTransactionsForPig(pigAId).first()
        assertThat(reloadedTxs).hasSize(2)
        val reloadedTotalPaise = reloadedTxs.sumOf { it.amount.paise }
        assertThat(reloadedTotalPaise).isEqualTo(50000L) // ₹500 in paise

        // Goal for Pig A survives intact
        val reloadedGoals = reloadedGoalRepo.observeGoalsForPig(pigAId).first()
        assertThat(reloadedGoals).hasSize(1)
        assertThat(reloadedGoals[0].title).isEqualTo("Goa Trip")
    }
}
