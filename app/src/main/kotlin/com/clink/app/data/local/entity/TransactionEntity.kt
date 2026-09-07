package com.clink.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.model.TransactionStatus
import com.clink.app.domain.model.TransactionType

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = PigEntity::class,
            parentColumns = ["id"],
            childColumns = ["pigId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["pigId"]),
        Index(value = ["timestamp"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val pigId: Long,
    val amountPaise: Long,
    val type: String,
    val status: String,
    val note: String,
    val timestamp: Long
) {
    fun toDomain(): Transaction = Transaction(
        id = id,
        pigId = pigId,
        amount = Money(amountPaise),
        type = TransactionType.valueOf(type),
        status = TransactionStatus.valueOf(status),
        note = note,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(domain: Transaction): TransactionEntity = TransactionEntity(
            id = domain.id,
            pigId = domain.pigId,
            amountPaise = domain.amount.paise,
            type = domain.type.name,
            status = domain.status.name,
            note = domain.note,
            timestamp = domain.timestamp
        )
    }
}
