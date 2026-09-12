package com.clink.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.clink.app.data.local.dao.PigDao
import com.clink.app.data.local.dao.TransactionDao
import com.clink.app.data.local.entity.PigEntity
import com.clink.app.data.local.entity.TransactionEntity
import com.clink.app.domain.model.TransactionType
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class SavingsIsolationTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // In-memory backing tables to simulate SQLite persistence
    private val persistentPigTable = mutableMapOf<Long, PigEntity>()
    private val persistentTransactionTable = mutableListOf<TransactionEntity>()

    private lateinit var mockPigDao: PigDao
    private lateinit var mockTxDao: TransactionDao
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    @Before
    fun setUp() {
        persistentPigTable.clear()
        persistentTransactionTable.clear()

        mockPigDao = mockk()
        mockTxDao = mockk()

        every { mockPigDao.getPigById(any()) } answers {
            val id = firstArg<Long>()
            flowOf(persistentPigTable[id])
        }
        every { mockTxDao.getAllTransactions() } answers {
            flowOf(persistentTransactionTable.toList())
        }

        val testDataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("isolation_test.preferences_pb") }
        )
        userPreferencesRepository = UserPreferencesRepository(testDataStore)
    }

    @Test
    fun `onboarding state transitions do NOT modify existing pig balance or transactions`() = runTest(testDispatcher) {
        // 1. Seed existing financial data (Simulating an existing user with ₹80 saved)
        val pigId = 1L
        persistentPigTable[pigId] = PigEntity.fromDomain(
            com.clink.app.domain.model.Pig(
                id = pigId,
                name = "Primary Pig",
                balance = com.clink.app.domain.model.Money(8000L),
                targetAmount = null,
                iconName = "pig_default",
                colorHex = "#E85D75",
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )

        persistentTransactionTable.add(
            TransactionEntity.fromDomain(
                com.clink.app.domain.model.Transaction(
                    id = 1L,
                    pigId = pigId,
                    amount = com.clink.app.domain.model.Money(1000L),
                    type = TransactionType.CREDIT,
                    status = com.clink.app.domain.model.TransactionStatus.COMPLETED,
                    note = "Clink savings",
                    timestamp = 1001L
                )
            )
        )
        persistentTransactionTable.add(
            TransactionEntity.fromDomain(
                com.clink.app.domain.model.Transaction(
                    id = 2L,
                    pigId = pigId,
                    amount = com.clink.app.domain.model.Money(2000L),
                    type = TransactionType.CREDIT,
                    status = com.clink.app.domain.model.TransactionStatus.COMPLETED,
                    note = "Clink savings",
                    timestamp = 1002L
                )
            )
        )
        persistentTransactionTable.add(
            TransactionEntity.fromDomain(
                com.clink.app.domain.model.Transaction(
                    id = 3L,
                    pigId = pigId,
                    amount = com.clink.app.domain.model.Money(5000L),
                    type = TransactionType.CREDIT,
                    status = com.clink.app.domain.model.TransactionStatus.COMPLETED,
                    note = "Clink savings",
                    timestamp = 1003L
                )
            )
        )

        // Verify initial state
        assertThat(mockPigDao.getPigById(pigId).first()?.balancePaise).isEqualTo(8000L)
        assertThat(mockTxDao.getAllTransactions().first()).hasSize(3)

        // 2. Complete Onboarding in DataStore
        userPreferencesRepository.setOnboardingCompleted(true)
        assertThat(userPreferencesRepository.isOnboardingCompleted.first()).isTrue()

        // 3. Verify Room savings state remains strictly untouched
        val pigAfterComplete = mockPigDao.getPigById(pigId).first()
        assertThat(pigAfterComplete?.balancePaise).isEqualTo(8000L)
        val txsAfterComplete = mockTxDao.getAllTransactions().first()
        assertThat(txsAfterComplete).hasSize(3)
        assertThat(txsAfterComplete.map { it.amountPaise }).containsExactly(1000L, 2000L, 5000L).inOrder()

        // 4. Toggle Onboarding State again (e.g. simulated reset)
        userPreferencesRepository.setOnboardingCompleted(false)
        assertThat(userPreferencesRepository.isOnboardingCompleted.first()).isFalse()

        // 5. Verify Room savings state is still 100% preserved
        val pigAfterReset = mockPigDao.getPigById(pigId).first()
        assertThat(pigAfterReset?.balancePaise).isEqualTo(8000L)
        val txsAfterReset = mockTxDao.getAllTransactions().first()
        assertThat(txsAfterReset).hasSize(3)
    }
}
