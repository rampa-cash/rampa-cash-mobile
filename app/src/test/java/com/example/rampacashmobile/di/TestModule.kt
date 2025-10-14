package com.example.rampacashmobile.di

import com.example.rampacashmobile.domain.repositories.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestModule {
    
    @Provides
    @Singleton
    fun provideWalletRepository(): WalletRepository = MockWalletRepository()
    
    @Provides
    @Singleton
    fun provideTransactionRepository(): TransactionRepository = MockTransactionRepository()
    
    @Provides
    @Singleton
    fun provideContactRepository(): ContactRepository = MockContactRepository()
}
