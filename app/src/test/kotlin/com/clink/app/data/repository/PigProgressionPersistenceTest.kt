package com.clink.app.data.repository

import com.clink.app.data.local.ClinkDatabase
import com.clink.app.data.local.dao.PigDao
import com.clink.app.data.local.dao.TransactionDao
import com.clink.app.data.local.entity.PigEntity
import com.clink.app.data.local.entity.TransactionEntity
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.PigState
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Verifies that the Pig's derived state and progression transitions
 * correctly as savings accumulate through the authoritative repository engine,
 * and that the derived state persists deterministically across repository reloads.
 */
class PigProgressionPersistenceTest {

    private val persistentPigTable = mutableMapOf<Long, PigEntity>()
    private val persistentTransactionTable = mutableListOf<TransactionEntity>()
    private var nextPigId = 1L
    private var nextTxId = 1L

    private lateinit var mockDatabase: ClinkDatabase
    private lateinit var mockPigDao: PigDao
    private lateinit var mockTxDao: TransactionDao

    @Before
    fun setUp() {
        persistentPigTable.clear()
        persistentTransactionTable.clear()
        nextPigId = 1L
        nextTxId = 1L

        mockDatabase = mockk(relaxed = true)
        mockPigDao = mockk(relaxed = true)
        mockTxDao = mockk(relaxed = true)

        setupDaoMockBehavior(mockPigDao, mockTxDao)
    }

    private fun setupDaoMockBehavior(pigDao: PigDao, txDao: TransactionDao) {
        coEvery { pigDao.getFirstPig() } answers {
            persistentPigTable.values.minByOrNull { it.id }
        }

        coEvery { pigDao.getPigByIdOnce(any()) } answers {
            val id = firstArg<Long>()
            persistentPigTable[id]
        }

        coEvery { pigDao.getPigById(any()) } answers {
            val id = firstArg<Long>()
            flowOf(persistentPigTable[id])
        }

        coEvery { pigDao.insertPig(any()) } answers {
            val entity = firstArg<PigEntity>()
            val assignedId = if (entity.id == 0L) nextPigId++ else entity.id
            val stored = entity.copy(id = assignedId)
            persistentPigTable[assignedId] = stored
            assignedId
        }

        coEvery { pigDao.updateBalance(any(), any(), any()) } answers {
            val id = firstArg<Long>()
            val newBalance = secondArg<Long>()
            val updated = thirdArg<Long>()
            val existing = persistentPigTable[id]
            if (existing != null) {
                persistentPigTable[id] = existing.copy(
                    balancePaise = newBalance,
                    updatedAt = updated
                )
            }
        }

        coEvery { txDao.insertTransaction(any()) } answers {
            val tx = firstArg<TransactionEntity>()
            val id = nextTxId++
            val saved = tx.copy(id = id)
            persistentTransactionTable.add(saved)
            id
        }
    }

    private fun createRepository(pigDao: PigDao, txDao: TransactionDao): PigRepositoryImpl {
        return PigRepositoryImpl(mockDatabase, pigDao, txDao).apply {
            transactionRunner = { block -> block() }
        }
    }

    @Test
    fun `initial pig starts in NEW state with zero balance`() = runTest {
        val repository = createRepository(mockPigDao, mockTxDao)
        val pig = repository.getOrCreateDefaultPig()

        assertThat(pig.balance).isEqualTo(Money.ZERO)
        assertThat(pig.state).isEqualTo(PigState.NEW)
        assertThat(pig.progression.state).isEqualTo(PigState.NEW)
        assertThat(pig.progression.progressToNextTier).isEqualTo(0.0f)
    }

    @Test
    fun `pig progresses through all states as savings accumulate`() = runTest {
        val repository = createRepository(mockPigDao, mockTxDao)
        val pig = repository.getOrCreateDefaultPig()

        // 1. Initial: NEW
        assertThat(pig.state).isEqualTo(PigState.NEW)

        // 2. Save ₹20 -> GROWING (2,000 paise)
        repository.addSavings(pig.id, Money.RS_20, "First save")
        val growingPig = repository.getPigById(pig.id).first()!!
        assertThat(growingPig.balance).isEqualTo(Money.fromRupees(20))
        assertThat(growingPig.state).isEqualTo(PigState.GROWING)
        assertThat(growingPig.progression.state).isEqualTo(PigState.GROWING)

        // 3. Save ₹480 (Total ₹500) -> HEALTHY (50,000 paise)
        repository.addSavings(pig.id, Money.fromRupees(480), "Crossed to healthy")
        val healthyPig = repository.getPigById(pig.id).first()!!
        assertThat(healthyPig.balance).isEqualTo(Money.fromRupees(500))
        assertThat(healthyPig.state).isEqualTo(PigState.HEALTHY)
        assertThat(healthyPig.progression.state).isEqualTo(PigState.HEALTHY)

        // 4. Save ₹1500 (Total ₹2,000) -> FULL (200,000 paise)
        repository.addSavings(pig.id, Money.fromRupees(1500), "Crossed to full")
        val fullPig = repository.getPigById(pig.id).first()!!
        assertThat(fullPig.balance).isEqualTo(Money.fromRupees(2000))
        assertThat(fullPig.state).isEqualTo(PigState.FULL)
        assertThat(fullPig.progression.state).isEqualTo(PigState.FULL)
        assertThat(fullPig.progression.progressToNextTier).isEqualTo(1.0f)
    }

    @Test
    fun `pig state and balance persist across repository reload`() = runTest {
        // Session 1: Accumulate ₹2,000
        val repo1 = createRepository(mockPigDao, mockTxDao)
        val pig = repo1.getOrCreateDefaultPig()
        repo1.addSavings(pig.id, Money.fromRupees(2000), "Reaching milestone")

        // Session 2: Fresh repository instance over same persistent tables
        val mockPigDao2 = mockk<PigDao>(relaxed = true)
        val mockTxDao2 = mockk<TransactionDao>(relaxed = true)
        setupDaoMockBehavior(mockPigDao2, mockTxDao2)
        val repo2 = createRepository(mockPigDao2, mockTxDao2)

        val reloadedPig = repo2.getOrCreateDefaultPig()
        assertThat(reloadedPig.id).isEqualTo(pig.id)
        assertThat(reloadedPig.balance).isEqualTo(Money.fromRupees(2000))
        assertThat(reloadedPig.state).isEqualTo(PigState.FULL)

        // Verify no duplicate pigs were created
        assertThat(persistentPigTable.size).isEqualTo(1)
    }
}
