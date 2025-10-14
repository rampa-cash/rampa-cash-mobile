package com.example.rampacashmobile.data.api.service

import com.example.rampacashmobile.data.api.model.*
import retrofit2.http.*

/**
 * VISA Card API service
 */
interface VISACardApiService {
    
    /**
     * Get VISA card details
     */
    @GET("visa-card")
    suspend fun getVISACard(): VISACardResponse
    
    /**
     * Create VISA card
     */
    @POST("visa-card")
    suspend fun createVISACard(
        @Body request: CreateVISACardRequest
    ): VISACardResponse
    
    /**
     * Get all VISA cards
     */
    @GET("visa-card/all")
    suspend fun getAllVISACards(): List<VISACardResponse>
    
    /**
     * Get VISA cards by status
     */
    @GET("visa-card/by-status/{status}")
    suspend fun getVISACardsByStatus(
        @Path("status") status: String
    ): List<VISACardResponse>
    
    /**
     * Get expired VISA cards
     */
    @GET("visa-card/expired")
    suspend fun getExpiredVISACards(): List<VISACardResponse>
    
    /**
     * Get VISA card statistics
     */
    @GET("visa-card/stats")
    suspend fun getVISACardStats(): VISACardStatsResponse
    
    /**
     * Get specific VISA card
     */
    @GET("visa-card/{id}")
    suspend fun getVISACard(
        @Path("id") id: String
    ): VISACardResponse
    
    /**
     * Update VISA card
     */
    @PUT("visa-card/{id}")
    suspend fun updateVISACard(
        @Path("id") id: String,
        @Body request: UpdateVISACardRequest
    ): VISACardResponse
    
    /**
     * Activate VISA card
     */
    @POST("visa-card/{id}/activate")
    suspend fun activateVISACard(
        @Path("id") id: String
    ): Map<String, Any>
    
    /**
     * Suspend VISA card
     */
    @POST("visa-card/{id}/suspend")
    suspend fun suspendVISACard(
        @Path("id") id: String
    ): Map<String, Any>
    
    /**
     * Reactivate VISA card
     */
    @POST("visa-card/{id}/reactivate")
    suspend fun reactivateVISACard(
        @Path("id") id: String
    ): Map<String, Any>
    
    /**
     * Cancel VISA card
     */
    @POST("visa-card/{id}/cancel")
    suspend fun cancelVISACard(
        @Path("id") id: String
    ): Map<String, Any>
    
    /**
     * Update VISA card balance
     */
    @POST("visa-card/{id}/update-balance")
    suspend fun updateVISACardBalance(
        @Path("id") id: String,
        @Body request: UpdateVISACardBalanceRequest
    ): Map<String, Any>
    
    /**
     * Check VISA card spending limits
     */
    @POST("visa-card/{id}/check-spending-limits")
    suspend fun checkVISACardSpendingLimits(
        @Path("id") id: String,
        @Body request: CheckVISACardSpendingLimitsRequest
    ): CheckVISACardSpendingLimitsResponse
}
