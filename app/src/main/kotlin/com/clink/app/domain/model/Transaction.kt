package com.clink.app.domain.model

/**
 * Domain model representing a financial transaction (credit or debit) into a Pig.
 */
data class Transaction(
    val id: Long = 0L,
    val pigId: Long,
    val amount: Money,
    val type: TransactionType,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class TransactionType {
    CREDIT,
    DEBIT
}

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED
}
