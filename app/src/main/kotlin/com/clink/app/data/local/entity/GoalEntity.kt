package com.clink.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.clink.app.domain.model.Goal
import com.clink.app.domain.model.Money

@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = PigEntity::class,
            parentColumns = ["id"],
            childColumns = ["pigId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["pigId"])
    ]
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val pigId: Long,
    val title: String,
    val targetPaise: Long,
    val savedPaise: Long,
    val deadline: Long?,
    val isCompleted: Boolean,
    val createdAt: Long
) {
    fun toDomain(): Goal = Goal(
        id = id,
        pigId = pigId,
        title = title,
        targetAmount = Money(targetPaise),
        savedAmount = Money(savedPaise),
        deadline = deadline,
        isCompleted = isCompleted,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: Goal): GoalEntity = GoalEntity(
            id = domain.id,
            pigId = domain.pigId,
            title = domain.title,
            targetPaise = domain.targetAmount.paise,
            savedPaise = domain.savedAmount.paise,
            deadline = domain.deadline,
            isCompleted = domain.isCompleted,
            createdAt = domain.createdAt
        )
    }
}
