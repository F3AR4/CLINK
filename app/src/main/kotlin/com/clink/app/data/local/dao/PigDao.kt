package com.clink.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.clink.app.data.local.entity.PigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PigDao {
    @Query("SELECT * FROM pigs ORDER BY updatedAt DESC")
    fun getAllPigs(): Flow<List<PigEntity>>

    @Query("SELECT * FROM pigs WHERE id = :id")
    fun getPigById(id: Long): Flow<PigEntity?>

    @Query("SELECT * FROM pigs WHERE id = :id")
    suspend fun getPigByIdOnce(id: Long): PigEntity?

    @Query("SELECT COUNT(*) FROM pigs")
    suspend fun getPigCount(): Int

    @Query("SELECT * FROM pigs ORDER BY id ASC LIMIT 1")
    suspend fun getFirstPig(): PigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPig(pig: PigEntity): Long

    @Update
    suspend fun updatePig(pig: PigEntity)

    @Query("UPDATE pigs SET balancePaise = :balancePaise, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateBalance(id: Long, balancePaise: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM pigs WHERE id = :id")
    suspend fun deletePig(id: Long)
}
