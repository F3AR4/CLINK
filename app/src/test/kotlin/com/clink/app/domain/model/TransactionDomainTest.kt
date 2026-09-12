package com.clink.app.domain.model

import com.clink.app.data.local.entity.TransactionEntity
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Domain model and entity mapping tests for Transaction.
 * Verifies Money representation, type invariants, and bidirectional mapping.
 */
class TransactionDomainTest {

    @Test
    fun `transaction domain model preserves money amount in paise without float loss`() {
        val amount = Money.fromRupees(20) // 2000 paise
        val tx = Transaction(
            id = 42L,
            pigId = 1L,
            amount = amount,
            type = TransactionType.CREDIT,
            status = TransactionStatus.COMPLETED,
            note = "Direct savings",
            timestamp = 1700000000000L
        )

        assertThat(tx.id).isEqualTo(42L)
        assertThat(tx.pigId).isEqualTo(1L)
        assertThat(tx.amount.paise).isEqualTo(2000L)
        assertThat(tx.amount.formatDisplay()).isEqualTo("₹20")
        assertThat(tx.type).isEqualTo(TransactionType.CREDIT)
        assertThat(tx.status).isEqualTo(TransactionStatus.COMPLETED)
        assertThat(tx.note).isEqualTo("Direct savings")
        assertThat(tx.timestamp).isEqualTo(1700000000000L)
    }

    @Test
    fun `entity mapping from domain preserves all fields exactly`() {
        val domainTx = Transaction(
            id = 101L,
            pigId = 5L,
            amount = Money(7550L), // ₹75.50
            type = TransactionType.CREDIT,
            status = TransactionStatus.COMPLETED,
            note = "Coffee savings",
            timestamp = 1712345678000L
        )

        val entity = TransactionEntity.fromDomain(domainTx)

        assertThat(entity.id).isEqualTo(101L)
        assertThat(entity.pigId).isEqualTo(5L)
        assertThat(entity.amountPaise).isEqualTo(7550L)
        assertThat(entity.type).isEqualTo("CREDIT")
        assertThat(entity.status).isEqualTo("COMPLETED")
        assertThat(entity.note).isEqualTo("Coffee savings")
        assertThat(entity.timestamp).isEqualTo(1712345678000L)
    }

    @Test
    fun `entity mapping to domain preserves money and type fidelity`() {
        val entity = TransactionEntity(
            id = 202L,
            pigId = 3L,
            amountPaise = 50000L, // ₹500
            type = "CREDIT",
            status = "COMPLETED",
            note = "Milestone deposit",
            timestamp = 1719999999000L
        )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(202L)
        assertThat(domain.pigId).isEqualTo(3L)
        assertThat(domain.amount).isEqualTo(Money.fromRupees(500))
        assertThat(domain.type).isEqualTo(TransactionType.CREDIT)
        assertThat(domain.status).isEqualTo(TransactionStatus.COMPLETED)
        assertThat(domain.note).isEqualTo("Milestone deposit")
        assertThat(domain.timestamp).isEqualTo(1719999999000L)
    }
}
