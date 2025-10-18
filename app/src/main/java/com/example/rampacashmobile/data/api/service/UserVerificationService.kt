package com.example.rampacashmobile.data.api.service

import com.example.rampacashmobile.data.api.ApiClient
import com.example.rampacashmobile.data.api.model.CompleteProfileRequest
import com.example.rampacashmobile.data.api.model.CompleteProfileResponse
import com.example.rampacashmobile.data.api.model.MissingFieldsResponse
import com.example.rampacashmobile.data.api.model.VerificationStatusResponse
import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.common.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for user verification and profile completion operations
 * 
 * Handles API calls for:
 * - Profile completion
 * - Verification status checking
 * - Missing fields retrieval
 */
@Singleton
class UserVerificationService @Inject constructor(
    private val apiClient: ApiClient
) {
    companion object {
        private const val TAG = "UserVerificationService"
    }

    /**
     * Complete user profile with missing information
     */
    suspend fun completeProfile(request: CompleteProfileRequest): Result<CompleteProfileResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d(TAG, "🔄 Completing user profile...")
                
                val response = apiClient.userVerificationApiService.completeProfile(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    Timber.d(TAG, "✅ Profile completed successfully")
                    Timber.d(TAG, "👤 User: ${responseBody.user.email ?: "No email"}")
                    Timber.d(TAG, "📝 Message: ${responseBody.message}")
                    
                    Result.success(responseBody)
                } else {
                    Timber.e(TAG, "❌ Profile completion failed: ${response.code()} - ${response.message()}")
                    Result.failure(DomainError.NetworkError("Profile completion failed: ${response.message()}"))
                }
                
            } catch (e: Exception) {
                Timber.e(TAG, "❌ Exception during profile completion: ${e.message}", e)
                Result.failure(DomainError.NetworkError("Failed to complete profile: ${e.message}"))
            }
        }
    }

    /**
     * Get user verification status and missing fields
     */
    suspend fun getVerificationStatus(): Result<VerificationStatusResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d(TAG, "🔄 Getting verification status...")
                
                val response = apiClient.userVerificationApiService.getVerificationStatus()
                
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    Timber.d(TAG, "✅ Verification status retrieved successfully")
                    Timber.d(TAG, "🔍 Status: ${responseBody.verificationStatus}")
                    Timber.d(TAG, "📋 Missing fields: ${responseBody.missingFields}")
                    Timber.d(TAG, "✅ Is verified: ${responseBody.isVerified}")
                    
                    Result.success(responseBody)
                } else {
                    Timber.e(TAG, "❌ Failed to get verification status: ${response.code()} - ${response.message()}")
                    Result.failure(DomainError.NetworkError("Failed to get verification status: ${response.message()}"))
                }
                
            } catch (e: Exception) {
                Timber.e(TAG, "❌ Exception during verification status retrieval: ${e.message}", e)
                Result.failure(DomainError.NetworkError("Failed to get verification status: ${e.message}"))
            }
        }
    }

    /**
     * Get missing fields for profile completion
     */
    suspend fun getMissingFields(): Result<MissingFieldsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d(TAG, "🔄 Getting missing fields...")
                
                val response = apiClient.userVerificationApiService.getMissingFields()
                
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    Timber.d(TAG, "✅ Missing fields retrieved successfully")
                    Timber.d(TAG, "📋 Missing fields: ${responseBody.missingFields}")
                    Timber.d(TAG, "✅ Is complete: ${responseBody.isComplete}")
                    
                    Result.success(responseBody)
                } else {
                    Timber.e(TAG, "❌ Failed to get missing fields: ${response.code()} - ${response.message()}")
                    Result.failure(DomainError.NetworkError("Failed to get missing fields: ${response.message()}"))
                }
                
            } catch (e: Exception) {
                Timber.e(TAG, "❌ Exception during missing fields retrieval: ${e.message}", e)
                Result.failure(DomainError.NetworkError("Failed to get missing fields: ${e.message}"))
            }
        }
    }
}
