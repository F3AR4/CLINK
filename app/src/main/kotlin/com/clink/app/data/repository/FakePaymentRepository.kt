package com.clink.app.data.repository

import com.clink.app.domain.model.Money
import com.clink.app.domain.repository.PaymentMethod
import com.clink.app.domain.repository.PaymentRepository
import com.clink.app.domain.repository.PaymentResult
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 1 Fake Payment Implementation.
 * Strictly simulated for offline architecture testing and verification.
 * NO real network calls, NO banking APIs, NO external payment secrets.
 */
@Singleton
class FakePaymentRepository @Inject constructor() : PaymentRepository {

    // Configurable for unit tests
    var shouldFailNext: Boolean = false
    var failureReason: String = "Simulated payment failure"

    override suspend fun processDeposit(
        pigId: Long,
        amount: Money,
        paymentMethod: PaymentMethod
    ): PaymentResult {
        if (shouldFailNext) {
            shouldFailNext = false
            return PaymentResult.Failure(
                errorCode = "SIMULATED_ERR_001",
                errorMessage = failureReason
            )
        }

        val ref = "MOCK-DEP-${UUID.randomUUID().toString().take(8).uppercase()}"
        return PaymentResult.Success(
            transactionReference = ref,
            amount = amount
        )
    }

    override suspend fun processWithdrawal(pigId: Long, amount: Money): PaymentResult {
        if (shouldFailNext) {
            shouldFailNext = false
            return PaymentResult.Failure(
                errorCode = "SIMULATED_ERR_002",
                errorMessage = failureReason
            )
        }

        val ref = "MOCK-WDR-${UUID.randomUUID().toString().take(8).uppercase()}"
        return PaymentResult.Success(
            transactionReference = ref,
            amount = amount
        )
    }
}
