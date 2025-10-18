package com.example.rampacashmobile.data.api.service

import com.example.rampacashmobile.data.api.model.CompleteProfileRequest
import com.example.rampacashmobile.data.api.model.CompleteProfileResponse
import com.example.rampacashmobile.data.api.model.MissingFieldsResponse
import com.example.rampacashmobile.data.api.model.VerificationStatusResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit interface for user verification API endpoints
 * 
 * Handles profile completion, verification status, and missing fields
 */
interface UserVerificationApiService {
    
    /**
     * Complete user profile with missing information
     * POST /user/complete-profile
     */
    @POST("user/complete-profile")
    suspend fun completeProfile(@Body request: CompleteProfileRequest): Response<CompleteProfileResponse>
    
    /**
     * Get user verification status and missing fields
     * GET /user/verification-status
     */
    @GET("user/verification-status")
    suspend fun getVerificationStatus(): Response<VerificationStatusResponse>
    
    /**
     * Get missing fields for profile completion
     * GET /user/missing-fields
     */
    @GET("user/missing-fields")
    suspend fun getMissingFields(): Response<MissingFieldsResponse>
}
