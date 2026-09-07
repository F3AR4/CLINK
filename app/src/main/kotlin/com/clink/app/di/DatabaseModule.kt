package com.clink.app.di

import android.content.Context
import androidx.room.Room
import com.clink.app.data.local.ClinkDatabase
import com.clink.app.data.local.dao.GoalDao
import com.clink.app.data.local.dao.PigDao
import com.clink.app.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideClinkDatabase(
        @ApplicationContext context: Context
    ): ClinkDatabase {
        return Room.databaseBuilder(
            context,
            ClinkDatabase::class.java,
            ClinkDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun providePigDao(database: ClinkDatabase): PigDao = database.pigDao()

    @Provides
    fun provideTransactionDao(database: ClinkDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideGoalDao(database: ClinkDatabase): GoalDao = database.goalDao()
}
