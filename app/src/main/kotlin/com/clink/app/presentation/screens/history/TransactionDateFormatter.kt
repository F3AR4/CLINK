package com.clink.app.presentation.screens.history

import com.clink.app.domain.model.Transaction
import com.clink.app.domain.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Dedicated date and time formatter for transaction history.
 * Provides contextual relative dates (Today, Yesterday, etc.),
 * grouping headers, and accessibility descriptions.
 * Supports passing custom `now` and `ZoneId` for deterministic unit testing.
 */
object TransactionDateFormatter {

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    private val sameYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, h:mm a", Locale.getDefault())
    private val differentYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.getDefault())

    private val headerSameYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
    private val headerDifferentYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

    /**
     * Formats the transaction timestamp into a human-readable string.
     * Examples:
     * - "Today, 3:42 PM"
     * - "Yesterday, 7:30 PM"
     * - "10 Sep, 6:45 PM"
     * - "15 Nov 2025, 2:15 PM"
     */
    fun formatTransactionTime(
        timestamp: Long,
        now: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val txInstant = Instant.ofEpochMilli(timestamp)
        val txZdt = txInstant.atZone(zoneId)
        val txDate = txZdt.toLocalDate()

        val nowDate = Instant.ofEpochMilli(now).atZone(zoneId).toLocalDate()

        return when {
            txDate.isEqual(nowDate) -> "Today, ${txZdt.format(timeFormatter)}"
            txDate.isEqual(nowDate.minusDays(1)) -> "Yesterday, ${txZdt.format(timeFormatter)}"
            txDate.year == nowDate.year -> txZdt.format(sameYearFormatter)
            else -> txZdt.format(differentYearFormatter)
        }
    }

    /**
     * Returns a group header label for grouping transactions by day.
     * Examples:
     * - "TODAY"
     * - "YESTERDAY"
     * - "10 SEP"
     * - "15 NOV 2025"
     */
    fun formatDateGroupHeader(
        timestamp: Long,
        now: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val txInstant = Instant.ofEpochMilli(timestamp)
        val txZdt = txInstant.atZone(zoneId)
        val txDate = txZdt.toLocalDate()

        val nowDate = Instant.ofEpochMilli(now).atZone(zoneId).toLocalDate()

        return when {
            txDate.isEqual(nowDate) -> "TODAY"
            txDate.isEqual(nowDate.minusDays(1)) -> "YESTERDAY"
            txDate.year == nowDate.year -> txZdt.format(headerSameYearFormatter).uppercase(Locale.getDefault())
            else -> txZdt.format(headerDifferentYearFormatter).uppercase(Locale.getDefault())
        }
    }

    /**
     * Returns an ISO local date string (e.g. "2026-09-12") for stable sorting and grouping keys.
     */
    fun formatDateKey(
        timestamp: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val txDate = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
        return txDate.toString()
    }

    /**
     * Builds an accessibility-friendly content description.
     * Example: "Saved ₹50, Coffee savings, Today at 3:42 PM"
     */
    fun formatAccessibilityDescription(
        transaction: Transaction,
        now: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val action = if (transaction.type == TransactionType.CREDIT) "Saved" else "Withdrew"
        val note = transaction.note.ifBlank { if (transaction.type == TransactionType.CREDIT) "Savings Added" else "Withdrawal" }
        val timeFormatted = formatTransactionTime(transaction.timestamp, now, zoneId).replace(", ", " at ")
        return "$action ${transaction.amount.formatDisplay()}, $note, $timeFormatted"
    }
}
