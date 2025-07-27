package com.example.rampacashmobile.usecase

import android.net.Uri
import com.example.rampacashmobile.networking.KtorHttpDriver
import com.solana.networking.Rpc20Driver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonArray
import com.solana.publickey.SolanaPublicKey
import com.solana.rpccore.JsonRpc20Request
import kotlinx.serialization.json.add

object TokenAccountBalanceUseCase {
    private val TAG = TokenAccountBalanceUseCase::class.simpleName

    /**
     * Get the token balance for a specific token account (ATA)
     * @param rpcUri Solana RPC endpoint
     * @param tokenAccount The Associated Token Account address
     * @return Token balance in smallest units (e.g., 1000000 = 1.0 for 6-decimal token)
     */
    suspend operator fun invoke(rpcUri: Uri, tokenAccount: SolanaPublicKey): Long =
        withContext(Dispatchers.IO) {
            val rpc = Rpc20Driver(rpcUri.toString(), KtorHttpDriver())
            val requestId = UUID.randomUUID().toString()
            val request = createTokenBalanceRequest(tokenAccount, requestId)
            val response = rpc.makeRequest(request, TokenBalanceResponse.serializer())

            response.error?.let { error ->
                val errorMessage =
                    "Could not fetch token balance for account [${tokenAccount.base58()}]: ${error.code}, ${error.message}"

                // Handle common errors
                when {
                    error.message.contains("Account not found") -> {
                        throw TokenAccountNotFoundException("Token account does not exist: ${tokenAccount.base58()}")
                    }

                    error.message.contains("Invalid account") -> {
                        throw InvalidTokenAccountException("Invalid token account: ${tokenAccount.base58()}")
                    }

                    else -> {
                        throw TokenBalanceException(errorMessage)
                    }
                }
            }

            val tokenAmount = response.result?.value?.amount?.toLongOrNull() ?: 0L
            val decimals = response.result?.value?.decimals ?: 0

            return@withContext tokenAmount
        }

    private fun createTokenBalanceRequest(tokenAccount: SolanaPublicKey, requestId: String = "1") =
        JsonRpc20Request(
            method = "getTokenAccountBalance", params = buildJsonArray {
                add(tokenAccount.base58())
            }, requestId
        )

    @Serializable
    data class TokenBalanceResponse(
        val value: TokenBalanceValue
    )

    @Serializable
    data class TokenBalanceValue(
        val amount: String,      // Token amount as string (to handle large numbers)
        val decimals: Int,       // Number of decimal places
        val uiAmount: Double?    // Human-readable amount (can be null)
    )

    open class TokenBalanceException(message: String? = null, cause: Throwable? = null) :
        RuntimeException(message, cause)

    class TokenAccountNotFoundException(message: String? = null, cause: Throwable? = null) :
        TokenBalanceException(message, cause)

    class InvalidTokenAccountException(message: String? = null, cause: Throwable? = null) :
        TokenBalanceException(message, cause)
} 