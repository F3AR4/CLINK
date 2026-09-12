package com.clink.app.data.repository

import com.clink.app.data.local.ClinkDatabase
import com.clink.app.data.local.dao.PigDao
import com.clink.app.data.local.dao.TransactionDao
import com.clink.app.data.local.entity.PigEntity
import com.clink.app.data.local.entity.TransactionEntity
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.TransactionStatus
import com.clink.app.domain.model.TransactionType
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

/**
 * Verifies atomic consistency, rollback behavior, validation invariants,
 * and deterministic ordering for the CLINK Transaction Engine.
 */
class TransactionAtomicityTest {

    private val pigTable = mutableMapOf<Long, PigEntity>()
    private val transactionTable = mutableListOf<TransactionEntity>()
    private var nextTxId = 1L

    private lateinit var mockDatabase: ClinkDatabase
    private lateinit var mockPigDao: PigDao
    private lateinit var mockTxDao: TransactionDao

    @Before
    fun setUp() {
        pigTable.clear()
        transactionTable.clear()
        nextTxId = 1L

        mockDatabase = mockk(relaxed = true)
        mockPigDao = mockk(relaxed = true)
        mockTxDao = mockk(relaxed = true)

        // Seed initial pig with ₹0 balance
        pigTable[1L] = PigEntity(
            id = 1L,
            name = "Primary Pig",
            balancePaise = 0L,
            targetAmountPaise = 500000L,
            iconName = "default_pig",
            colorHex = "#E85D75",
            createdAt = 1000L,
            updatedAt = 1000L
        )

        coEvery { mockPigDao.getPigByIdOnce(1L) } answers { pigTable[1L] }
        coEvery { mockPigDao.getPigByIdOnce(999L) } answers { null }

        coEvery { mockPigDao.updateBalance(any(), any(), any()) } answers {
            val id = firstArg<Long>()
            val newBalance = secondArg<Long>()
            val updatedTimestamp = thirdArg<Long>()
            val existing = pigTable[id]
            if (existing != null) {
                pigTable[id] = existing.copy(
                    balancePaise = newBalance,
                    updatedAt = updatedTimestamp
                )
            }
        }

        coEvery { mockTxDao.insertTransaction(any()) } answers {
            val entity = firstArg<TransactionEntity>()
            val id = nextTxId++
            val saved = entity.copy(id = id)
            transactionTable.add(saved)
            id
        }

        coEvery { mockTxDao.getTransactionsForPig(1L) } answers {
            // Sort by timestamp DESC, id DESC as defined in DAO query
            val sorted = transactionTable
                .filter { it.pigId == 1L }
                .sortedWith(compareByDescending<TransactionEntity> { it.timestamp }.thenByDescending { it.id })
            flowOf(sorted)
        }
    }

    private fun createRepository(
        pigDao: PigDao = mockPigDao,
        txDao: TransactionDao = mockTxDao,
        customRunner: (suspend (suspend () -> Any?) -> Any?)? = null
    ): PigRepositoryImpl {
        return PigRepositoryImpl(
            database = mockDatabase,
            pigDao = pigDao,
            transactionDao = txDao
        ).apply {
            transactionRunner = customRunner ?: { block ->
                // Simulate SQLite atomic transaction: snapshot state, rollback on exception
                val pigSnapshot = pigTable.mapValues { it.value.copy() }.toMutableMap()
                val txSnapshot = transactionTable.map { it.copy() }.toMutableList()
                try {
                    block()
                } catch (e: Throwable) {
                    // Atomic rollback
                    pigTable.clear()
                    pigTable.putAll(pigSnapshot)
                    transactionTable.clear()
                    transactionTable.addAll(txSnapshot)
                    throw e
                }
            }
        }
    }

    @Test
    fun `successful addSavings atomically updates balance and inserts single transaction`() = runTest {
        val repo = createRepository()

        val tx = repo.addSavings(pigId = 1L, amount = Money.RS_20, note = "Test deposit")

        assertThat(tx.pigId).isEqualTo(1L)
        assertThat(tx.amount).isEqualTo(Money.RS_20)
        assertThat(tx.type).isEqualTo(TransactionType.CREDIT)
        assertThat(tx.status).isEqualTo(TransactionStatus.COMPLETED)
        assertThat(tx.note).isEqualTo("Test deposit")

        // Authoritative balance is updated to ₹20
        assertThat(pigTable[1L]?.balancePaise).isEqualTo(2000L)
        // Exactly one transaction record is stored
        assertThat(transactionTable).hasSize(1)
        assertThat(transactionTable[0].amountPaise).isEqualTo(2000L)
        assertThat(transactionTable[0].note).isEqualTo("Test deposit")
    }

    @Test
    fun `single committed timestamp is shared between balance update and transaction record`() = runTest {
        val repo = createRepository()

        val tx = repo.addSavings(pigId = 1L, amount = Money.RS_10, note = "Timestamp check")

        val updatedPig = pigTable[1L]!!
        val recordedTx = transactionTable[0]

        assertThat(updatedPig.updatedAt).isEqualTo(recordedTx.timestamp)
        assertThat(tx.timestamp).isEqualTo(recordedTx.timestamp)
    }

    @Test
    fun `atomic rollback occurs when transaction insertion fails`() = runTest {
        // Setup txDao to fail during insertion
        coEvery { mockTxDao.insertTransaction(any()) } throws IllegalStateException("Simulated disk error during insert")

        val repo = createRepository()

        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                repo.addSavings(pigId = 1L, amount = Money.RS_50, note = "Will fail")
            }
        }

        // Verify state is completely rolled back: balance remains ₹0, transactions empty
        assertThat(pigTable[1L]?.balancePaise).isEqualTo(0L)
        assertThat(transactionTable).isEmpty()
    }

    @Test
    fun `zero or negative amount is rejected and does not modify database state`() = runTest {
        val repo = createRepository()

        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking {
                repo.addSavings(pigId = 1L, amount = Money.ZERO, note = "Zero")
            }
        }

        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking {
                repo.addSavings(pigId = 1L, amount = Money(-500L), note = "Negative")
            }
        }

        // Database remains pristine
        assertThat(pigTable[1L]?.balancePaise).isEqualTo(0L)
        assertThat(transactionTable).isEmpty()
    }

    @Test
    fun `saving to non-existent pig throws exception without recording transaction`() = runTest {
        val repo = createRepository()

        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking {
                repo.addSavings(pigId = 999L, amount = Money.RS_10, note = "Ghost pig")
            }
        }

        assertThat(transactionTable).isEmpty()
    }

    @Test
    fun `blank or whitespace note falls back to Clink savings default`() = runTest {
        val repo = createRepository()

        val txBlank = repo.addSavings(pigId = 1L, amount = Money.RS_10, note = "   ")
        assertThat(txBlank.note).isEqualTo("Clink savings")

        val txEmpty = repo.addSavings(pigId = 1L, amount = Money.RS_20, note = "")
        assertThat(txEmpty.note).isEqualTo("Clink savings")

        val txCustom = repo.addSavings(pigId = 1L, amount = Money.RS_50, note = "Custom note")
        assertThat(txCustom.note).isEqualTo("Custom note")
    }

    @Test
    fun `deterministic query ordering resolves timestamp collision with id DESC`() = runTest {
        val repo = createRepository()
        val fixedTimestamp = 1700000000000L

        // Insert multiple transactions with the EXACT SAME timestamp
        transactionTable.add(
            TransactionEntity(
                id = 1L,
                pigId = 1L,
                amountPaise = 1000L,
                type = "CREDIT",
                status = "COMPLETED",
                note = "First in same ms",
                timestamp = fixedTimestamp
            )
        )
        transactionTable.add(
            TransactionEntity(
                id = 2L,
                pigId = 1L,
                amountPaise = 2000L,
                type = "CREDIT",
                status = "COMPLETED",
                note = "Second in same ms",
                timestamp = fixedTimestamp
            )
        )
        transactionTable.add(
            TransactionEntity(
                id = 3L,
                pigId = 1L,
                amountPaise = 5000L,
                type = "CREDIT",
                status = "COMPLETED",
                note = "Third in same ms",
                timestamp = fixedTimestamp
            )
        )

        val txRepo = TransactionRepositoryImpl(mockTxDao)
        val results = txRepo.observeTransactions(1L).first()

        assertThat(results).hasSize(3)
        // With timestamp collision, id DESC must place id=3 first, then id=2, then id=1
        assertThat(results[0].id).isEqualTo(3L)
        assertThat(results[1].id).isEqualTo(2L)
        assertThat(results[2].id).isEqualTo(1L)
    }
}
