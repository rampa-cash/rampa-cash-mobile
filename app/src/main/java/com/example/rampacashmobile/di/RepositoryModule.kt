package com.example.rampacashmobile.di

import com.example.rampacashmobile.data.repositories.WalletRepositoryImpl
import com.example.rampacashmobile.data.repositories.TransactionRepositoryImpl
import com.example.rampacashmobile.data.repositories.ContactRepositoryImpl
import com.example.rampacashmobile.data.repository.FakeOffRampRepository
import com.example.rampacashmobile.domain.repositories.WalletRepository
import com.example.rampacashmobile.domain.repositories.TransactionRepository
import com.example.rampacashmobile.domain.repositories.ContactRepository
import com.example.rampacashmobile.data.repository.OffRampRepository
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
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository
    
    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository
    
    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository
    
    @Binds
    @Singleton
    abstract fun bindOffRampRepository(impl: FakeOffRampRepository): OffRampRepository
}