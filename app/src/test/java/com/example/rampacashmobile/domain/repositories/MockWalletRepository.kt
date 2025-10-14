package com.example.rampacashmobile.domain.repositories

import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.entities.Wallet
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.domain.valueobjects.WalletId
import javax.inject.Inject

class MockWalletRepository @Inject constructor() : WalletRepository {
    
    private val wallets = mutableMapOf<String, Wallet>()
    
    override suspend fun findById(id: WalletId): Result<Wallet> {
        return wallets[id.value]?.let { Result.success(it) }
            ?: Result.failure(com.example.rampacashmobile.domain.common.DomainError.NotFound("Wallet not found"))
    }
    
    override suspend fun findByAddress(address: WalletAddress): Result<Wallet> {
        return wallets.values.find { it.address == address }?.let { Result.success(it) }
            ?: Result.failure(com.example.rampacashmobile.domain.common.DomainError.NotFound("Wallet not found"))
    }
    
    override suspend fun save(wallet: Wallet): Result<Unit> {
        wallets[wallet.id] = wallet
        return Result.success(Unit)
    }
    
    override suspend fun update(wallet: Wallet): Result<Unit> {
        wallets[wallet.id] = wallet
        return Result.success(Unit)
    }
    
    override suspend fun delete(id: WalletId): Result<Unit> {
        wallets.remove(id.value)
        return Result.success(Unit)
    }
    
    fun addWallet(wallet: Wallet) {
        wallets[wallet.id] = wallet
    }
    
    fun clear() {
        wallets.clear()
    }
}
