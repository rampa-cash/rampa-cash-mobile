package com.example.rampacashmobile.data.api.service

import com.example.rampacashmobile.data.api.model.*
import retrofit2.http.*

/**
 * Wallet API service
 */
interface WalletApiService {
    
    /**
     * Create new wallet
     */
    @POST("wallet")
    suspend fun createWallet(
        @Body request: CreateWalletRequest
    ): WalletResponse
    
    /**
     * Get wallet details
     */
    @GET("wallet")
    suspend fun getWallet(): WalletResponse
    
    /**
     * Update wallet
     */
    @PUT("wallet")
    suspend fun updateWallet(
        @Body request: UpdateWalletRequest
    ): WalletResponse
    
    /**
     * Disconnect wallet
     */
    @DELETE("wallet")
    suspend fun disconnectWallet(): Map<String, String>
    
    /**
     * Get specific wallet balance
     */
    @POST("wallet/balance")
    suspend fun getWalletBalance(
        @Body request: GetWalletBalanceRequest
    ): TokenBalanceResponse
    
    /**
     * Get all wallet balances
     */
    @GET("wallet/balances")
    suspend fun getAllWalletBalances(): WalletBalanceResponse
    
    /**
     * Connect existing wallet
     */
    @POST("wallet/connect")
    suspend fun connectWallet(
        @Body request: ConnectWalletRequest
    ): WalletResponse
    
    /**
     * Transfer funds
     */
    @POST("wallet/transfer")
    suspend fun transferFunds(
        @Body request: TransferRequest
    ): Map<String, Any>
    
    /**
     * Suspend wallet
     */
    @POST("wallet/suspend")
    suspend fun suspendWallet(): Map<String, Any>
    
    /**
     * Activate wallet
     */
    @POST("wallet/activate")
    suspend fun activateWallet(): Map<String, Any>
}
