package com.clink.app.presentation.screens.history

import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.model.TransactionType
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class TransactionDateFormatterTest {

    private val testZoneId: ZoneId = ZoneOffset.UTC

    // Fixed base date: 2026-09-12 15:30:00 UTC
    private val nowLdt = LocalDateTime.of(2026, 9, 12, 15, 30, 0)
    private val nowMillis = nowLdt.atZone(testZoneId).toInstant().toEpochMilli()

    @Test
    fun `formatTransactionTime formats today correctly`() {
        val todayLdt = LocalDateTime.of(2026, 9, 12, 9, 15, 0)
        val todayMillis = todayLdt.atZone(testZoneId).toInstant().toEpochMilli()

        val formatted = TransactionDateFormatter.formatTransactionTime(
            timestamp = todayMillis,
            now = nowMillis,
            zoneId = testZoneId
        )

        assertThat(formatted).isEqualTo("Today, 9:15 AM")
    }

    @Test
    fun `formatTransactionTime formats yesterday correctly`() {
        val yesterdayLdt = LocalDateTime.of(2026, 9, 11, 19, 45, 0)
        val yesterdayMillis = yesterdayLdt.atZone(testZoneId).toInstant().toEpochMilli()

        val formatted = TransactionDateFormatter.formatTransactionTime(
            timestamp = yesterdayMillis,
            now = nowMillis,
            zoneId = testZoneId
        )

        assertThat(formatted).isEqualTo("Yesterday, 7:45 PM")
    }

    @Test
    fun `formatTransactionTime formats earlier date in same year correctly`() {
        val sameYearLdt = LocalDateTime.of(2026, 7, 4, 14, 20, 0)
        val sameYearMillis = sameYearLdt.atZone(testZoneId).toInstant().toEpochMilli()

        val formatted = TransactionDateFormatter.formatTransactionTime(
            timestamp = sameYearMillis,
            now = nowMillis,
            zoneId = testZoneId
        )

        assertThat(formatted).isEqualTo("4 Jul, 2:20 PM")
    }

    @Test
    fun `formatTransactionTime formats date in different year correctly`() {
        val diffYearLdt = LocalDateTime.of(2025, 12, 25, 10, 0, 0)
        val diffYearMillis = diffYearLdt.atZone(testZoneId).toInstant().toEpochMilli()

        val formatted = TransactionDateFormatter.formatTransactionTime(
            timestamp = diffYearMillis,
            now = nowMillis,
            zoneId = testZoneId
        )

        assertThat(formatted).isEqualTo("25 Dec 2025, 10:00 AM")
    }

    @Test
    fun `formatDateGroupHeader returns TODAY for today`() {
        val header = TransactionDateFormatter.formatDateGroupHeader(
            timestamp = nowMillis,
            now = nowMillis,
            zoneId = testZoneId
        )
        assertThat(header).isEqualTo("TODAY")
    }

    @Test
    fun `formatDateGroupHeader returns YESTERDAY for yesterday`() {
        val yesterdayLdt = LocalDateTime.of(2026, 9, 11, 10, 0, 0)
        val yesterdayMillis = yesterdayLdt.atZone(testZoneId).toInstant().toEpochMilli()

        val header = TransactionDateFormatter.formatDateGroupHeader(
            timestamp = yesterdayMillis,
            now = nowMillis,
            zoneId = testZoneId
        )
        assertThat(header).isEqualTo("YESTERDAY")
    }

    @Test
    fun `formatDateGroupHeader returns uppercase month and day for same year`() {
        val sameYearLdt = LocalDateTime.of(2026, 5, 20, 10, 0, 0)
        val sameYearMillis = sameYearLdt.atZone(testZoneId).toInstant().toEpochMilli()

        val header = TransactionDateFormatter.formatDateGroupHeader(
            timestamp = sameYearMillis,
            now = nowMillis,
            zoneId = testZoneId
        )
        assertThat(header).isEqualTo("20 MAY")
    }

    @Test
    fun `formatDateGroupHeader returns uppercase month day and year for different year`() {
        val diffYearLdt = LocalDateTime.of(2024, 11, 5, 10, 0, 0)
        val diffYearMillis = diffYearLdt.atZone(testZoneId).toInstant().toEpochMilli()

        val header = TransactionDateFormatter.formatDateGroupHeader(
            timestamp = diffYearMillis,
            now = nowMillis,
            zoneId = testZoneId
        )
        assertThat(header).isEqualTo("5 NOV 2024")
    }

    @Test
    fun `formatAccessibilityDescription generates clear accessible string`() {
        val tx = Transaction(
            id = 1L,
            pigId = 1L,
            amount = Money.fromRupees(50),
            type = TransactionType.CREDIT,
            note = "Coffee savings",
            timestamp = nowMillis
        )

        val desc = TransactionDateFormatter.formatAccessibilityDescription(
            transaction = tx,
            now = nowMillis,
            zoneId = testZoneId
        )

        assertThat(desc).isEqualTo("Saved ₹50, Coffee savings, Today at 3:30 PM")
    }

    @Test
    fun `formatAccessibilityDescription falls back when note is blank`() {
        val tx = Transaction(
            id = 2L,
            pigId = 1L,
            amount = Money.fromRupees(20),
            type = TransactionType.CREDIT,
            note = "",
            timestamp = nowMillis
        )

        val desc = TransactionDateFormatter.formatAccessibilityDescription(
            transaction = tx,
            now = nowMillis,
            zoneId = testZoneId
        )

        assertThat(desc).isEqualTo("Saved ₹20, Savings Added, Today at 3:30 PM")
    }
}
