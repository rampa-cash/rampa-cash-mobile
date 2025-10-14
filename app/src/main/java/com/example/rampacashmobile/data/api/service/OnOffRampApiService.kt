package com.example.rampacashmobile.data.api.service

import com.example.rampacashmobile.data.api.model.*
import retrofit2.http.*

/**
 * On/Off Ramp API service
 */
interface OnOffRampApiService {
    
    // On-Ramp endpoints
    /**
     * Initiate fiat to crypto conversion
     */
    @POST("onramp/initiate")
    suspend fun initiateOnRamp(
        @Body request: OnRampRequest
    ): OnRampResponse
    
    /**
     * Get all on-ramps
     */
    @GET("onramp")
    suspend fun getOnRamps(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): List<OnRampResponse>
    
    /**
     * Get pending on-ramps
     */
    @GET("onramp/pending")
    suspend fun getPendingOnRamps(): List<OnRampResponse>
    
    /**
     * Get on-ramp statistics
     */
    @GET("onramp/stats/summary")
    suspend fun getOnRampStats(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): OnRampStatsResponse
    
    /**
     * Get specific on-ramp
     */
    @GET("onramp/{id}")
    suspend fun getOnRamp(
        @Path("id") id: String
    ): OnRampResponse
    
    /**
     * Process on-ramp
     */
    @POST("onramp/{id}")
    suspend fun processOnRamp(
        @Path("id") id: String,
        @Body request: ProcessOnRampRequest
    ): Map<String, Any>
    
    /**
     * Process on-ramp (alternative endpoint)
     */
    @POST("onramp/{id}/process")
    suspend fun processOnRampAlternative(
        @Path("id") id: String,
        @Body request: ProcessOnRampRequest
    ): Map<String, Any>
    
    /**
     * Fail on-ramp
     */
    @POST("onramp/{id}/fail")
    suspend fun failOnRamp(
        @Path("id") id: String,
        @Body request: FailOnRampRequest
    ): Map<String, Any>
    
    /**
     * Get on-ramp by provider
     */
    @GET("onramp/providers/{provider}/transaction/{providerTransactionId}")
    suspend fun getOnRampByProvider(
        @Path("provider") provider: String,
        @Path("providerTransactionId") providerTransactionId: String
    ): OnRampResponse
    
    // Off-Ramp endpoints
    /**
     * Initiate crypto to fiat conversion
     */
    @POST("offramp/initiate")
    suspend fun initiateOffRamp(
        @Body request: OffRampRequest
    ): OffRampResponse
    
    /**
     * Get all off-ramps
     */
    @GET("offramp")
    suspend fun getOffRamps(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): List<OffRampResponse>
    
    /**
     * Get pending off-ramps
     */
    @GET("offramp/pending")
    suspend fun getPendingOffRamps(): List<OffRampResponse>
    
    /**
     * Get off-ramp statistics
     */
    @GET("offramp/stats/summary")
    suspend fun getOffRampStats(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): OffRampStatsResponse
    
    /**
     * Get specific off-ramp
     */
    @GET("offramp/{id}")
    suspend fun getOffRamp(
        @Path("id") id: String
    ): OffRampResponse
    
    /**
     * Process off-ramp
     */
    @POST("offramp/{id}")
    suspend fun processOffRamp(
        @Path("id") id: String,
        @Body request: ProcessOffRampRequest
    ): Map<String, Any>
    
    /**
     * Process off-ramp (alternative endpoint)
     */
    @POST("offramp/{id}/process")
    suspend fun processOffRampAlternative(
        @Path("id") id: String,
        @Body request: ProcessOffRampRequest
    ): Map<String, Any>
    
    /**
     * Fail off-ramp
     */
    @POST("offramp/{id}/fail")
    suspend fun failOffRamp(
        @Path("id") id: String,
        @Body request: FailOffRampRequest
    ): Map<String, Any>
    
    /**
     * Get off-ramp by provider
     */
    @GET("offramp/providers/{provider}/transaction/{providerTransactionId}")
    suspend fun getOffRampByProvider(
        @Path("provider") provider: String,
        @Path("providerTransactionId") providerTransactionId: String
    ): OffRampResponse
}
