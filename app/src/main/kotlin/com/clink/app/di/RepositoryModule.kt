package com.clink.app.di

import com.clink.app.data.preferences.UserPreferencesRepository as UserPreferencesRepositoryImpl
import com.clink.app.data.repository.FakePaymentRepository
import com.clink.app.data.repository.GoalRepositoryImpl
import com.clink.app.data.repository.PigRepositoryImpl
import com.clink.app.data.repository.TransactionRepositoryImpl
import com.clink.app.domain.repository.GoalRepository
import com.clink.app.domain.repository.PaymentRepository
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.repository.TransactionRepository
import com.clink.app.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPigRepository(impl: PigRepositoryImpl): PigRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(impl: FakePaymentRepository): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}

