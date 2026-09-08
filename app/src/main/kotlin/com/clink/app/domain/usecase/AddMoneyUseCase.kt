package com.clink.app.domain.usecase

import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.model.TransactionStatus
import com.clink.app.domain.model.TransactionType
import com.clink.app.domain.repository.PaymentMethod
import com.clink.app.domain.repository.PaymentRepository
import com.clink.app.domain.repository.PaymentResult
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.repository.TransactionRepository

/**
 * UseCase coordinating the business logic for adding micro-savings into a Pig.
 * - Validates input amount (must be > 0 paise)
 * - Validates existence of Pig
 * - Validates arithmetic overflow protection
 * - Invokes payment simulation
 * - Atomically persists Pig balance update and credit transaction in Room
 * - Returns domain Result with completed Transaction
 */
class AddMoneyUseCase(
    private val pigRepository: PigRepository,
    private val transactionRepository: TransactionRepository,
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(
        pigId: Long,
        amount: Money,
        note: String = "Added savings",
        method: PaymentMethod = PaymentMethod.SIMULATED_UPI
    ): Result<Transaction> {
        // 1. Validation: Reject zero or negative amounts
        if (amount.paise <= 0) {
            return Result.failure(IllegalArgumentException("Savings amount must be greater than zero paise"))
        }

        // 2. Validate Pig exists
        val pig = pigRepository.getPigByIdOnce(pigId)
            ?: return Result.failure(IllegalArgumentException("Pig with ID $pigId not found"))

        // 3. Overflow validation: ensure balance + amount does not overflow Long.MAX_VALUE
        try {
            pig.balance + amount
        } catch (e: ArithmeticException) {
            return Result.failure(e)
        }

        // 4. Simulate payment processing
        return when (val paymentResult = paymentRepository.processDeposit(pigId, amount, method)) {
            is PaymentResult.Success -> {
                try {
                    val transaction = pigRepository.addSavings(
                        pigId = pigId,
                        amount = amount,
                        note = note
                    )
                    Result.success(transaction)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            is PaymentResult.Failure -> {
                val failedTransaction = Transaction(
                    pigId = pigId,
                    amount = amount,
                    type = TransactionType.CREDIT,
                    status = TransactionStatus.FAILED,
                    note = "Failed: ${paymentResult.errorMessage}",
                    timestamp = System.currentTimeMillis()
                )
                transactionRepository.recordTransaction(failedTransaction)
                Result.failure(IllegalStateException("Deposit failed: ${paymentResult.errorMessage}"))
            }
        }
    }
}
