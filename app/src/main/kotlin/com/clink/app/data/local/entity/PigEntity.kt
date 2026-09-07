package com.clink.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig

@Entity(tableName = "pigs")
data class PigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val balancePaise: Long,
    val targetAmountPaise: Long?,
    val iconName: String,
    val colorHex: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): Pig = Pig(
        id = id,
        name = name,
        balance = Money(balancePaise),
        targetAmount = targetAmountPaise?.let { Money(it) },
        iconName = iconName,
        colorHex = colorHex,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(domain: Pig): PigEntity = PigEntity(
            id = domain.id,
            name = domain.name,
            balancePaise = domain.balance.paise,
            targetAmountPaise = domain.targetAmount?.paise,
            iconName = domain.iconName,
            colorHex = domain.colorHex,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
