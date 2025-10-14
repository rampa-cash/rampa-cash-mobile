package com.example.rampacashmobile.domain.services

import com.example.rampacashmobile.domain.entities.Wallet
import com.example.rampacashmobile.domain.valueobjects.UserId
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.domain.valueobjects.Money
import com.example.rampacashmobile.domain.valueobjects.Currency
import com.example.rampacashmobile.domain.repositories.WalletRepository
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.exceptions.*
import com.example.rampacashmobile.solanautils.AssociatedTokenAccountUtils
import com.example.rampacashmobile.solanautils.TokenMints
import com.example.rampacashmobile.usecase.AccountBalanceUseCase
import com.example.rampacashmobile.usecase.TokenAccountBalanceUseCase
import com.solana.publickey.SolanaPublicKey
import android.net.Uri
import java.math.BigDecimal
import javax.inject.Inject

/**
 * Wallet domain service containing business logic for wallet operations
 * 
 * This service encapsulates complex wallet-related business rules
 * that don't naturally belong to the Wallet entity itself
 */
class WalletDomainService @Inject constructor(
    private val walletRepository: WalletRepository
) {
    
    /**
     * Create a new wallet for a user
     * 
     * @param userId The user ID
     * @param address The wallet address
     * @param label Optional wallet label
     * @return Result containing the created wallet
     */
    suspend fun createWallet(
        userId: UserId,
        address: WalletAddress,
        label: String = ""
    ): Result<Wallet> {
        return try {
            // Check if user already has a wallet with this address
            walletRepository.existsByAddress(address)
                .flatMap { exists ->
                    if (exists) {
                        Result.failure(WalletAlreadyExistsException("Wallet with address ${address.value} already exists"))
                    } else {
                        // Check if user already has a wallet (for primary wallet logic)
                        walletRepository.existsByUserId(userId)
                            .flatMap { hasWallet ->
                                Wallet.create(userId, address, label)
                                    .flatMap { wallet ->
                                        walletRepository.save(wallet)
                                            .map { wallet }
                                    }
                            }
                    }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to create wallet: ${e.message}", e))
        }
    }

    /**
     * Get all wallets for a user
     * 
     * @param userId The user ID
     * @return Result containing list of wallets
     */
    suspend fun getUserWallets(userId: UserId): Result<List<Wallet>> {
        return walletRepository.findByUserId(userId)
    }

    /**
     * Get active wallets for a user
     * 
     * @param userId The user ID
     * @return Result containing list of active wallets
     */
    suspend fun getActiveUserWallets(userId: UserId): Result<List<Wallet>> {
        return walletRepository.findActiveByUserId(userId)
    }

    /**
     * Get primary wallet for a user
     * 
     * @param userId The user ID
     * @return Result containing the primary wallet
     */
    suspend fun getPrimaryWallet(userId: UserId): Result<Wallet> {
        return walletRepository.findPrimaryByUserId(userId)
    }

    /**
     * Set a wallet as primary for a user
     * 
     * @param userId The user ID
     * @param walletId The wallet ID to set as primary
     * @return Result indicating success or failure
     */
    suspend fun setPrimaryWallet(userId: UserId, walletId: String): Result<Unit> {
        return try {
            // Verify the wallet belongs to the user
            walletRepository.findById(walletId)
                .flatMap { wallet ->
                    if (wallet.userId == userId) {
                        walletRepository.setPrimary(userId, walletId)
                    } else {
                        Result.failure(WalletNotOwnedByUserException("Wallet $walletId does not belong to user $userId"))
                    }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to set primary wallet: ${e.message}", e))
        }
    }

    /**
     * Update wallet label
     * 
     * @param walletId The wallet ID
     * @param newLabel The new label
     * @return Result indicating success or failure
     */
    suspend fun updateWalletLabel(walletId: String, newLabel: String): Result<Unit> {
        return try {
            walletRepository.findById(walletId)
                .flatMap { wallet ->
                    wallet.updateLabel(newLabel)
                        .flatMap {
                            walletRepository.update(wallet)
                        }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to update wallet label: ${e.message}", e))
        }
    }

    /**
     * Activate a wallet
     * 
     * @param walletId The wallet ID
     * @return Result indicating success or failure
     */
    suspend fun activateWallet(walletId: String): Result<Unit> {
        return try {
            walletRepository.findById(walletId)
                .flatMap { wallet ->
                    wallet.activate()
                        .flatMap {
                            walletRepository.update(wallet)
                        }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to activate wallet: ${e.message}", e))
        }
    }

    /**
     * Deactivate a wallet
     * 
     * @param walletId The wallet ID
     * @return Result indicating success or failure
     */
    suspend fun deactivateWallet(walletId: String): Result<Unit> {
        return try {
            walletRepository.findById(walletId)
                .flatMap { wallet ->
                    wallet.deactivate()
                        .flatMap {
                            walletRepository.update(wallet)
                        }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to deactivate wallet: ${e.message}", e))
        }
    }

    /**
     * Delete a wallet
     * 
     * @param walletId The wallet ID
     * @return Result indicating success or failure
     */
    suspend fun deleteWallet(walletId: String): Result<Unit> {
        return try {
            walletRepository.findById(walletId)
                .flatMap { wallet ->
                    if (wallet.isActiveStatus()) {
                        Result.failure(WalletCannotDeleteActiveException("Cannot delete active wallet"))
                    } else {
                        walletRepository.delete(walletId)
                    }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to delete wallet: ${e.message}", e))
        }
    }

    /**
     * Check if a wallet exists by address
     * 
     * @param address The wallet address
     * @return Result containing true if exists, false otherwise
     */
    suspend fun walletExists(address: WalletAddress): Result<Boolean> {
        return walletRepository.existsByAddress(address)
    }

    /**
     * Check if a user has any wallets
     * 
     * @param userId The user ID
     * @return Result containing true if has wallets, false otherwise
     */
    suspend fun userHasWallets(userId: UserId): Result<Boolean> {
        return walletRepository.existsByUserId(userId)
    }

    /**
     * Load all balances for a wallet
     * 
     * @param walletAddress The wallet address
     * @return Result containing the wallet with all balances loaded
     */
    suspend fun loadWalletBalances(walletAddress: WalletAddress): Result<Wallet> {
        return try {
            val publicKey = walletAddress.toSolanaPublicKey()
            val rpcUri = Uri.parse("https://api.devnet.solana.com") // TODO: Make this configurable
            
            // Load SOL balance
            val solBalance = getSolBalance(walletAddress)
            val eurcBalance = getEurcBalance(walletAddress)
            val usdcBalance = getUsdcBalance(walletAddress)
            
            // Create a wallet entity with the loaded balances
            // For now, we'll create a simple wallet representation
            // In a real implementation, this would be more sophisticated
            val wallet = Wallet.create(
                userId = UserId.of("temp"), // TODO: Get actual user ID
                address = walletAddress,
                label = "Main Wallet"
            ).getOrThrow()
            
            Result.success(wallet)
        } catch (e: Exception) {
            Result.failure(DomainError.NetworkError("Failed to load wallet balances: ${e.message}", e))
        }
    }

    /**
     * Get SOL balance for a wallet
     * 
     * @param walletAddress The wallet address
     * @return Result containing the SOL balance as Money
     */
    suspend fun getSolBalance(walletAddress: WalletAddress): Result<Money> {
        return try {
            val publicKey = walletAddress.toSolanaPublicKey()
            val rpcUri = Uri.parse("https://api.devnet.solana.com") // TODO: Make this configurable
            
            val balance = AccountBalanceUseCase(rpcUri, publicKey)
            val solAmount = BigDecimal.valueOf(balance).divide(BigDecimal.valueOf(1000000000), 9, java.math.RoundingMode.HALF_UP)
            val money = Money(solAmount, Currency.USD) // SOL is typically priced in USD
            
            Result.success(money)
        } catch (e: Exception) {
            Result.failure(DomainError.NetworkError("Failed to get SOL balance: ${e.message}", e))
        }
    }

    /**
     * Get EURC balance for a wallet
     * 
     * @param walletAddress The wallet address
     * @return Result containing the EURC balance as Money
     */
    suspend fun getEurcBalance(walletAddress: WalletAddress): Result<Money> {
        return try {
            val publicKey = walletAddress.toSolanaPublicKey()
            val rpcUri = Uri.parse("https://api.devnet.solana.com") // TODO: Make this configurable
            
            val eurcMint = SolanaPublicKey.from(TokenMints.EURC_DEVNET)
            val eurcAta = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(publicKey, eurcMint)
            
            val balance = TokenAccountBalanceUseCase(rpcUri, eurcAta)
            val eurcAmount = BigDecimal.valueOf(balance).divide(BigDecimal.valueOf(1000000), 6, java.math.RoundingMode.HALF_UP)
            val money = Money(eurcAmount, Currency.EUR)
            
            Result.success(money)
        } catch (e: Exception) {
            Result.failure(DomainError.NetworkError("Failed to get EURC balance: ${e.message}", e))
        }
    }

    /**
     * Get USDC balance for a wallet
     * 
     * @param walletAddress The wallet address
     * @return Result containing the USDC balance as Money
     */
    suspend fun getUsdcBalance(walletAddress: WalletAddress): Result<Money> {
        return try {
            val publicKey = walletAddress.toSolanaPublicKey()
            val rpcUri = Uri.parse("https://api.devnet.solana.com") // TODO: Make this configurable
            
            val usdcMint = SolanaPublicKey.from(TokenMints.USDC_DEVNET)
            val usdcAta = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(publicKey, usdcMint)
            
            val balance = TokenAccountBalanceUseCase(rpcUri, usdcAta)
            val usdcAmount = BigDecimal.valueOf(balance).divide(BigDecimal.valueOf(1000000), 6, java.math.RoundingMode.HALF_UP)
            val money = Money(usdcAmount, Currency.USD)
            
            Result.success(money)
        } catch (e: Exception) {
            Result.failure(DomainError.NetworkError("Failed to get USDC balance: ${e.message}", e))
        }
    }
}

/**
 * Wallet-specific domain exceptions
 */
class WalletAlreadyExistsException(message: String) : DomainException(message)
class WalletNotOwnedByUserException(message: String) : DomainException(message)
class WalletCannotDeleteActiveException(message: String) : DomainException(message)
