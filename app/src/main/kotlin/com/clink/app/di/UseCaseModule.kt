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

    @Provides
    @Singleton
    fun provideGetTransactionsUseCase(
        transactionRepository: TransactionRepository
    ): com.clink.app.domain.usecase.GetTransactionsUseCase {
        return com.clink.app.domain.usecase.GetTransactionsUseCase(transactionRepository)
    }

    @Provides
    @Singleton
    fun provideCreateGoalUseCase(
        goalRepository: com.clink.app.domain.repository.GoalRepository,
        pigRepository: PigRepository
    ): com.clink.app.domain.usecase.CreateGoalUseCase {
        return com.clink.app.domain.usecase.CreateGoalUseCase(goalRepository, pigRepository)
    }

    @Provides
    @Singleton
    fun provideObserveGoalsUseCase(
        goalRepository: com.clink.app.domain.repository.GoalRepository,
        pigRepository: PigRepository
    ): com.clink.app.domain.usecase.ObserveGoalsUseCase {
        return com.clink.app.domain.usecase.ObserveGoalsUseCase(goalRepository, pigRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteGoalUseCase(
        goalRepository: com.clink.app.domain.repository.GoalRepository
    ): com.clink.app.domain.usecase.DeleteGoalUseCase {
        return com.clink.app.domain.usecase.DeleteGoalUseCase(goalRepository)
    }

    @Provides
    @Singleton
    fun provideGetSelectedPigUseCase(
        pigRepository: PigRepository,
        userPreferencesRepository: UserPreferencesRepository
    ): com.clink.app.domain.usecase.GetSelectedPigUseCase {
        return com.clink.app.domain.usecase.GetSelectedPigUseCase(pigRepository, userPreferencesRepository)
    }

    @Provides
    @Singleton
    fun provideSelectPigUseCase(
        pigRepository: PigRepository,
        userPreferencesRepository: UserPreferencesRepository
    ): com.clink.app.domain.usecase.SelectPigUseCase {
        return com.clink.app.domain.usecase.SelectPigUseCase(pigRepository, userPreferencesRepository)
    }

    @Provides
    @Singleton
    fun provideCreatePigUseCase(
        pigRepository: PigRepository,
        userPreferencesRepository: UserPreferencesRepository
    ): com.clink.app.domain.usecase.CreatePigUseCase {
        return com.clink.app.domain.usecase.CreatePigUseCase(pigRepository, userPreferencesRepository)
    }

    @Provides
    @Singleton
    fun provideUpdatePigUseCase(
        pigRepository: PigRepository
    ): com.clink.app.domain.usecase.UpdatePigUseCase {
        return com.clink.app.domain.usecase.UpdatePigUseCase(pigRepository)
    }

    @Provides
    @Singleton
    fun provideDeletePigUseCase(
        pigRepository: PigRepository,
        userPreferencesRepository: UserPreferencesRepository
    ): com.clink.app.domain.usecase.DeletePigUseCase {
        return com.clink.app.domain.usecase.DeletePigUseCase(pigRepository, userPreferencesRepository)
    }
}



