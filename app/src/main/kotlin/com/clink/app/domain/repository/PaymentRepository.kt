package com.clink.app.domain.repository

import com.clink.app.domain.model.Money

/**
 * Architectural Abstraction for Payment Simulation / Processing.
 * In Phase 1 Foundation, this is strictly an abstraction.
 * NO real UPI, NO payment gateway keys, NO real monetary transactions.
 */
interface PaymentRepository {
    suspend fun processDeposit(pigId: Long, amount: Money, paymentMethod: PaymentMethod): PaymentResult
    suspend fun processWithdrawal(pigId: Long, amount: Money): PaymentResult
}

enum class PaymentMethod {
    SIMULATED_UPI,
    SIMULATED_CASH,
    SIMULATED_NET_BANKING
}

sealed interface PaymentResult {
    data class Success(val transactionReference: String, val amount: Money) : PaymentResult
    data class Failure(val errorCode: String, val errorMessage: String) : PaymentResult
}
