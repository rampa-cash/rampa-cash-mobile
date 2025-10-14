package com.example.rampacashmobile.data.api.service

import com.example.rampacashmobile.data.api.model.Web3AuthValidateRequest
import com.example.rampacashmobile.data.api.model.Web3AuthValidateResponse
import com.example.rampacashmobile.data.api.model.UserProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Web3Auth API service for authentication
 */
interface Web3AuthApiService {
    
    /**
     * Validate Web3Auth JWT token and exchange for API token
     */
    @POST("auth/web3auth/validate")
    suspend fun validateWeb3AuthToken(
        @Body request: Web3AuthValidateRequest
    ): Web3AuthValidateResponse
    
    /**
     * Get user profile
     */
    @GET("auth/me")
    suspend fun getUserProfile(): UserProfileResponse
    
    /**
     * Logout user
     */
    @POST("auth/logout")
    suspend fun logout(): Map<String, String>
}
