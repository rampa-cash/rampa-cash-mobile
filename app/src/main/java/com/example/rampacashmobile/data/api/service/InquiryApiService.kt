package com.example.rampacashmobile.data.api.service

import com.example.rampacashmobile.data.api.model.*
import retrofit2.http.*

/**
 * Inquiry API service
 */
interface InquiryApiService {
    
    /**
     * Create a new inquiry
     */
    @POST("inquiry")
    suspend fun createInquiry(
        @Body request: CreateInquiryRequest
    ): InquiryResponse
    
    /**
     * Get all inquiries
     */
    @GET("inquiry")
    suspend fun getInquiries(): List<InquiryResponse>
    
    /**
     * Get all waitlist inquiries
     */
    @GET("inquiry/waitlist")
    suspend fun getWaitlistInquiries(): List<InquiryResponse>
    
    /**
     * Add inquiry to waitlist
     */
    @POST("inquiry/waitlist")
    suspend fun addToWaitlist(
        @Body request: CreateWaitlistInquiryRequest
    ): InquiryResponse
    
    /**
     * Get inquiry by ID
     */
    @GET("inquiry/{id}")
    suspend fun getInquiry(
        @Path("id") id: Int
    ): InquiryResponse
    
    /**
     * Delete inquiry by ID
     */
    @DELETE("inquiry/{id}")
    suspend fun deleteInquiry(
        @Path("id") id: Int
    ): Unit
}
