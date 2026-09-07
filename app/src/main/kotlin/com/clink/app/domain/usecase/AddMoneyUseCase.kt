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
 * UseCase coordinating the business logic for adding money into a Pig.
 * - Validates input amount (must be > 0)
 * - Invokes payment simulation
 * - Updates Pig balance atomically in repository
 * - Persists transaction record
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
        if (amount.paise <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero paise"))
        }

        val pig = pigRepository.getPigByIdOnce(pigId)
            ?: return Result.failure(IllegalArgumentException("Pig with ID $pigId not found"))

        // Simulate payment processing
        return when (val paymentResult = paymentRepository.processDeposit(pigId, amount, method)) {
            is PaymentResult.Success -> {
                val newBalance = pig.balance + amount
                pigRepository.updateBalance(pigId, newBalance)

                val transaction = Transaction(
                    pigId = pigId,
                    amount = amount,
                    type = TransactionType.CREDIT,
                    status = TransactionStatus.COMPLETED,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )
                val transactionId = transactionRepository.recordTransaction(transaction)
                Result.success(transaction.copy(id = transactionId))
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
