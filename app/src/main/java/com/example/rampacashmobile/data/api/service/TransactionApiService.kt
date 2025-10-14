package com.example.rampacashmobile.data.api.service

import com.example.rampacashmobile.data.api.model.*
import retrofit2.http.*

/**
 * Transaction API service
 */
interface TransactionApiService {
    
    /**
     * Get transaction history
     */
    @GET("transactions")
    suspend fun getTransactionHistory(
        @Query("status") status: String? = null,
        @Query("tokenType") tokenType: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): List<TransactionResponse>
    
    /**
     * Create new transaction
     */
    @POST("transactions")
    suspend fun createTransaction(
        @Body request: CreateTransactionRequest
    ): TransactionResponse
    
    /**
     * Get sent transactions
     */
    @GET("transactions/sent")
    suspend fun getSentTransactions(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): List<TransactionResponse>
    
    /**
     * Get received transactions
     */
    @GET("transactions/received")
    suspend fun getReceivedTransactions(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): List<TransactionResponse>
    
    /**
     * Get pending transactions
     */
    @GET("transactions/pending")
    suspend fun getPendingTransactions(): List<TransactionResponse>
    
    /**
     * Get transaction statistics
     */
    @GET("transactions/stats/summary")
    suspend fun getTransactionStats(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): TransactionStatsResponse
    
    /**
     * Get transaction details
     */
    @GET("transactions/{transactionId}")
    suspend fun getTransactionDetails(
        @Path("transactionId") transactionId: String
    ): TransactionResponse
    
    /**
     * Confirm transaction
     */
    @POST("transactions/{transactionId}")
    suspend fun confirmTransaction(
        @Path("transactionId") transactionId: String,
        @Body request: ConfirmTransactionRequest
    ): Map<String, Any>
    
    /**
     * Confirm transaction (alternative endpoint)
     */
    @POST("transactions/{transactionId}/confirm")
    suspend fun confirmTransactionAlternative(
        @Path("transactionId") transactionId: String,
        @Body request: ConfirmTransactionRequest
    ): Map<String, Any>
    
    /**
     * Cancel transaction
     */
    @POST("transactions/{transactionId}/cancel")
    suspend fun cancelTransaction(
        @Path("transactionId") transactionId: String
    ): Map<String, Any>
}
