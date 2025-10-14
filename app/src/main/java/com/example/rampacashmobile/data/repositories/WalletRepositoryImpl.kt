package com.example.rampacashmobile.data.repositories

import com.example.rampacashmobile.domain.entities.Wallet
import com.example.rampacashmobile.domain.repositories.WalletRepository
import com.example.rampacashmobile.domain.valueobjects.UserId
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor() : WalletRepository {
    
    private val wallets = mutableMapOf<String, Wallet>()
    
    override suspend fun findById(id: String): Result<Wallet> {
        return try {
            val wallet = wallets[id]
            if (wallet != null) {
                Result.success(wallet)
            } else {
                Result.failure(DomainError.NotFound("Wallet with id $id not found"))
            }
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find wallet: ${e.message}"))
        }
    }
    
    override suspend fun findByAddress(address: WalletAddress): Result<Wallet> {
        return try {
            val wallet = wallets.values.find { it.address == address }
            if (wallet != null) {
                Result.success(wallet)
            } else {
                Result.failure(DomainError.NotFound("Wallet with address ${address.value} not found"))
            }
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find wallet by address: ${e.message}"))
        }
    }
    
    override suspend fun findByUserId(userId: UserId): Result<List<Wallet>> {
        return try {
            val userWallets = wallets.values.filter { it.userId == userId }
            Result.success(userWallets)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find wallets for user: ${e.message}"))
        }
    }
    
    override suspend fun findActiveByUserId(userId: UserId): Result<List<Wallet>> {
        return try {
            val activeWallets = wallets.values.filter { it.userId == userId && it.isActive }
            Result.success(activeWallets)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find active wallets for user: ${e.message}"))
        }
    }
    
    override suspend fun save(wallet: Wallet): Result<Unit> {
        return try {
            wallets[wallet.id] = wallet
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to save wallet: ${e.message}"))
        }
    }
    
    override suspend fun update(wallet: Wallet): Result<Unit> {
        return try {
            wallets[wallet.id] = wallet
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to update wallet: ${e.message}"))
        }
    }
    
    override suspend fun delete(id: String): Result<Unit> {
        return try {
            wallets.remove(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to delete wallet: ${e.message}"))
        }
    }
    
    override suspend fun existsByAddress(address: WalletAddress): Result<Boolean> {
        return try {
            val exists = wallets.values.any { it.address == address }
            Result.success(exists)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to check wallet existence: ${e.message}"))
        }
    }
    
    override suspend fun existsByUserId(userId: UserId): Result<Boolean> {
        return try {
            val exists = wallets.values.any { it.userId == userId }
            Result.success(exists)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to check wallet existence: ${e.message}"))
        }
    }
    
    override suspend fun findPrimaryByUserId(userId: UserId): Result<Wallet> {
        return try {
            val primaryWallet = wallets.values.find { it.userId == userId && it.isActive }
            if (primaryWallet != null) {
                Result.success(primaryWallet)
            } else {
                Result.failure(DomainError.NotFound("No primary wallet found for user"))
            }
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find primary wallet: ${e.message}"))
        }
    }
    
    override suspend fun setPrimary(userId: UserId, walletId: String): Result<Unit> {
        return try {
            // In a real implementation, you would update the primary wallet flag
            // For now, just return success
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to set primary wallet: ${e.message}"))
        }
    }
}
