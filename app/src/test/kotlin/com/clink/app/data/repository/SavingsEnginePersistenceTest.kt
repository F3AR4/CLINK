package com.clink.app.data.repository

import com.clink.app.data.local.ClinkDatabase
import com.clink.app.data.local.dao.PigDao
import com.clink.app.data.local.dao.TransactionDao
import com.clink.app.data.local.entity.PigEntity
import com.clink.app.data.local.entity.TransactionEntity
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.model.TransactionType
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Verifies core savings persistence mechanics:
 * - Balance updates
 * - Transaction creation
 * - Multiple consecutive savings operations (₹0 -> ₹10 -> ₹30 -> ₹80)
 * - State persistence across repository reloads / recreation
 */
class SavingsEnginePersistenceTest {

    // In-memory backing tables to simulate SQLite persistence across instances
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

        coEvery { pigDao.getAllPigs() } answers {
            flowOf(persistentPigTable.values.toList())
        }

        coEvery { pigDao.insertPig(any()) } answers {
            val entity = firstArg<PigEntity>()
            val id = if (entity.id == 0L) nextPigId++ else entity.id
            val saved = entity.copy(id = id)
            persistentPigTable[id] = saved
            id
        }

        coEvery { pigDao.updateBalance(any(), any(), any()) } answers {
            val id = firstArg<Long>()
            val newPaise = secondArg<Long>()
            val existing = persistentPigTable[id]
            if (existing != null) {
                persistentPigTable[id] = existing.copy(balancePaise = newPaise)
            }
        }

        coEvery { txDao.insertTransaction(any()) } answers {
            val entity = firstArg<TransactionEntity>()
            val id = nextTxId++
            val saved = entity.copy(id = id)
            persistentTransactionTable.add(saved)
            id
        }

        coEvery { txDao.getTransactionsForPig(any()) } answers {
            val pigId = firstArg<Long>()
            flowOf(persistentTransactionTable.filter { it.pigId == pigId })
        }

        coEvery { txDao.getAllTransactions() } answers {
            flowOf(persistentTransactionTable.toList())
        }
    }

    private fun createRepository(pigDao: PigDao, txDao: TransactionDao): PigRepositoryImpl {
        return PigRepositoryImpl(
            database = mockDatabase,
            pigDao = pigDao,
            transactionDao = txDao
        ).apply {
            transactionRunner = { block -> block() }
        }
    }

    @Test
    fun `initial pig creation creates exactly one pig with zero balance`() = runTest {
        val repo = createRepository(mockPigDao, mockTxDao)

        val pig = repo.getOrCreateDefaultPig()
        assertThat(pig.id).isEqualTo(1L)
        assertThat(pig.balance).isEqualTo(Money.ZERO)

        // Calling it again returns the same pig without creating a duplicate
        val pigSecondCall = repo.getOrCreateDefaultPig()
        assertThat(pigSecondCall.id).isEqualTo(1L)
        assertThat(persistentPigTable).hasSize(1)
    }

    @Test
    fun `addSavings atomically updates balance and logs credit transaction`() = runTest {
        val repo = createRepository(mockPigDao, mockTxDao)
        val pig = repo.getOrCreateDefaultPig()

        val tx = repo.addSavings(pigId = pig.id, amount = Money.RS_10, note = "First clink")

        assertThat(tx.pigId).isEqualTo(pig.id)
        assertThat(tx.amount).isEqualTo(Money.RS_10)
        assertThat(tx.type).isEqualTo(TransactionType.CREDIT)

        val updatedPig = repo.getPigByIdOnce(pig.id)
        assertThat(updatedPig?.balance).isEqualTo(Money.RS_10)
        assertThat(persistentTransactionTable).hasSize(1)
    }

    @Test
    fun `multiple consecutive saves correctly accumulate balance and record all transactions`() = runTest {
        val repo = createRepository(mockPigDao, mockTxDao)
        val pig = repo.getOrCreateDefaultPig()

        // Initial = ₹0
        assertThat(pig.balance).isEqualTo(Money.ZERO)

        // Save ₹10 -> balance = ₹10
        repo.addSavings(pig.id, Money.RS_10, "Save 10")
        var current = repo.getPigByIdOnce(pig.id)
        assertThat(current?.balance).isEqualTo(Money.RS_10)

        // Save ₹20 -> balance = ₹30
        repo.addSavings(pig.id, Money.RS_20, "Save 20")
        current = repo.getPigByIdOnce(pig.id)
        assertThat(current?.balance).isEqualTo(Money.fromRupees(30))

        // Save ₹50 -> balance = ₹80
        repo.addSavings(pig.id, Money.RS_50, "Save 50")
        current = repo.getPigByIdOnce(pig.id)
        assertThat(current?.balance).isEqualTo(Money.fromRupees(80))

        // Verify transaction history contains exactly three deposits
        val txRepo = TransactionRepositoryImpl(mockTxDao)
        val transactions = txRepo.getTransactionsForPig(pig.id).first()
        assertThat(transactions).hasSize(3)
        assertThat(transactions[0].amount).isEqualTo(Money.RS_10)
        assertThat(transactions[1].amount).isEqualTo(Money.RS_20)
        assertThat(transactions[2].amount).isEqualTo(Money.RS_50)
    }

    @Test
    fun `persistence across reload retains balance and transactions`() = runTest {
        // Phase A: Perform operations using initial repository instance
        val repoInstance1 = createRepository(mockPigDao, mockTxDao)
        val pig = repoInstance1.getOrCreateDefaultPig()

        repoInstance1.addSavings(pig.id, Money.RS_20, "Before restart")
        repoInstance1.addSavings(pig.id, Money.RS_50, "Before restart 2")

        // Phase B: Simulate app restart / repository recreation with fresh DAO instances
        val newPigDao = mockk<PigDao>(relaxed = true)
        val newTxDao = mockk<TransactionDao>(relaxed = true)
        setupDaoMockBehavior(newPigDao, newTxDao)

        val reloadedPigRepo = createRepository(newPigDao, newTxDao)
        val reloadedTxRepo = TransactionRepositoryImpl(newTxDao)

        // Verify reloaded pig retains ₹70 balance
        val reloadedPig = reloadedPigRepo.getOrCreateDefaultPig()
        assertThat(reloadedPig.id).isEqualTo(pig.id)
        assertThat(reloadedPig.balance).isEqualTo(Money.fromRupees(70))

        // Verify reloaded transactions retain both entries
        val reloadedTxs = reloadedTxRepo.getTransactionsForPig(pig.id).first()
        assertThat(reloadedTxs).hasSize(2)
        assertThat(reloadedTxs[0].amount).isEqualTo(Money.RS_20)
        assertThat(reloadedTxs[1].amount).isEqualTo(Money.RS_50)
    }
}
