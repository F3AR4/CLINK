package com.clink.app.di

import com.clink.app.domain.repository.PaymentRepository
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.repository.TransactionRepository
import com.clink.app.domain.repository.UserPreferencesRepository
import com.clink.app.domain.usecase.AddMoneyUseCase
import com.clink.app.domain.usecase.CompleteOnboardingUseCase
import com.clink.app.domain.usecase.GetOnboardingStateUseCase
import com.clink.app.domain.usecase.GetPigSummaryUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideAddMoneyUseCase(
        pigRepository: PigRepository,
        transactionRepository: TransactionRepository,
        paymentRepository: PaymentRepository
    ): AddMoneyUseCase {
        return AddMoneyUseCase(pigRepository, transactionRepository, paymentRepository)
    }

    @Provides
    @Singleton
    fun provideGetPigSummaryUseCase(
        pigRepository: PigRepository,
        transactionRepository: TransactionRepository
    ): GetPigSummaryUseCase {
        return GetPigSummaryUseCase(pigRepository, transactionRepository)
    }

    @Provides
    @Singleton
    fun provideGetOnboardingStateUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): GetOnboardingStateUseCase {
        return GetOnboardingStateUseCase(userPreferencesRepository)
    }

    @Provides
    @Singleton
    fun provideCompleteOnboardingUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): CompleteOnboardingUseCase {
        return CompleteOnboardingUseCase(userPreferencesRepository)
    }

    @Provides
    @Singleton
    fun provideGetPrimaryPigUseCase(
        pigRepository: PigRepository
    ): com.clink.app.domain.usecase.GetPrimaryPigUseCase {
        return com.clink.app.domain.usecase.GetPrimaryPigUseCase(pigRepository)
    }
}

