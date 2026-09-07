package com.clink.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.clink.app.data.local.dao.GoalDao
import com.clink.app.data.local.dao.PigDao
import com.clink.app.data.local.dao.TransactionDao
import com.clink.app.data.local.entity.GoalEntity
import com.clink.app.data.local.entity.PigEntity
import com.clink.app.data.local.entity.TransactionEntity

/**
 * Main Room database for the CLINK application.
 * Contains entities for Pigs, Transactions, and Goals.
 */
@Database(
    entities = [
        PigEntity::class,
        TransactionEntity::class,
        GoalEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ClinkDatabase : RoomDatabase() {
    abstract fun pigDao(): PigDao
    abstract fun transactionDao(): TransactionDao
    abstract fun goalDao(): GoalDao

    companion object {
        const val DATABASE_NAME = "clink.db"
    }
}
