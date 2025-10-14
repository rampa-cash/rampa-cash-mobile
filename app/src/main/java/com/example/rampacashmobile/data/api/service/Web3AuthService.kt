package com.example.rampacashmobile.data.api.service

import com.example.rampacashmobile.data.api.ApiClient
import com.example.rampacashmobile.data.api.TokenManager
import com.example.rampacashmobile.data.api.model.Web3AuthValidateRequest
import com.example.rampacashmobile.data.api.model.Web3AuthValidateResponse
import com.example.rampacashmobile.data.api.model.UserProfileResponse
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import com.web3auth.core.types.Web3AuthResponse
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Web3Auth service for handling authentication with backend API
 */
@Singleton
class Web3AuthService @Inject constructor(
    private val apiClient: ApiClient,
    private val tokenManager: TokenManager
) {
    
    companion object {
        private const val TAG = "Web3AuthService"
    }
    
    /**
     * Validate Web3Auth JWT token and exchange for API token
     */
    suspend fun validateWeb3AuthToken(web3AuthResponse: Web3AuthResponse): Result<Web3AuthValidateResponse> {
        return try {
            Timber.d(TAG, "🔐 Validating Web3Auth token with backend...")
            Timber.d(TAG, "📊 Web3Auth response: userInfo=${web3AuthResponse.userInfo?.name}, privKey=${web3AuthResponse.privKey?.take(10)}...")
            
            // Extract JWT token from Web3Auth response
            // Web3Auth typically provides the JWT token through userInfo
            val web3AuthJwt = web3AuthResponse.userInfo?.idToken
            Timber.d(TAG, "🔑 Extracted JWT token: ${web3AuthJwt?.take(20)}...")
            
            if (web3AuthJwt.isNullOrBlank()) {
                Timber.e(TAG, "❌ No JWT token found in Web3Auth response")
                return Result.failure(DomainError.AuthenticationError("No JWT token received from Web3Auth"))
            }
            
            // Create validation request
            val request = Web3AuthValidateRequest(token = web3AuthJwt)
            Timber.d(TAG, "📤 Sending validation request to backend...")
            
            // Call backend validation endpoint
            val response = apiClient.web3AuthApiService.validateWeb3AuthToken(request)
            Timber.d(TAG, "📥 Backend response received: success=${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                Timber.d(TAG, "✅ Backend validation successful")
                Timber.d(TAG, "👤 User: ${responseBody.user.email}")
                Timber.d(TAG, "🔑 API Token: ${responseBody.accessToken.take(20)}...")
                
                // Store the API JWT token
                apiClient.setAuthToken(responseBody.accessToken, responseBody.expiresIn)
                
                Timber.d(TAG, "✅ Web3Auth token validated and stored successfully")
                Result.success(responseBody)
            } else {
                Timber.e(TAG, "❌ Backend validation failed: ${response.code()} - ${response.message()}")
                Result.failure(DomainError.AuthenticationError("Backend validation failed: ${response.message()}"))
            }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to validate Web3Auth token: ${e.message}")
            Result.failure(DomainError.NetworkError("Failed to validate Web3Auth token: ${e.message}"))
        }
    }
    
    /**
     * Get user profile from backend
     */
    suspend fun getUserProfile(): Result<UserProfileResponse> {
        return try {
            Timber.d(TAG, "👤 Getting user profile from backend...")
            
            val response = apiClient.web3AuthApiService.getUserProfile()
            
            if (response.isSuccessful && response.body() != null) {
                val userProfile = response.body()!!
                Timber.d(TAG, "✅ User profile retrieved successfully")
                Result.success(userProfile)
            } else {
                Timber.e(TAG, "❌ Failed to get user profile: ${response.code()} - ${response.message()}")
                Result.failure(DomainError.NetworkError("Failed to get user profile: ${response.message()}"))
            }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to get user profile: ${e.message}")
            Result.failure(DomainError.NetworkError("Failed to get user profile: ${e.message}"))
        }
    }
    
    /**
     * Logout user from backend
     */
    suspend fun logout(): Result<Unit> {
        return try {
            Timber.d(TAG, "🚪 Logging out from backend...")
            
            apiClient.web3AuthApiService.logout()
            
            // Clear stored tokens
            apiClient.clearAuthToken()
            
            Timber.d(TAG, "✅ Logout successful")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to logout: ${e.message}")
            // Even if backend logout fails, clear local tokens
            apiClient.clearAuthToken()
            Result.failure(DomainError.NetworkError("Failed to logout: ${e.message}"))
        }
    }
    
    /**
     * Check if user is authenticated with backend
     */
    fun isAuthenticated(): Boolean {
        return apiClient.isAuthenticated()
    }
    
    /**
     * Get stored access token
     */
    fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }
}
