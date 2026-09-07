package com.clink.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.clink.app.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE pigId = :pigId ORDER BY createdAt DESC")
    fun getGoalsForPig(pigId: Long): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("UPDATE goals SET savedPaise = :savedPaise, isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateSavedAmount(id: Long, savedPaise: Long, isCompleted: Boolean)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)
}
