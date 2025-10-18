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
    suspend fun validateWeb3AuthToken(web3AuthResponse: Web3AuthResponse): com.example.rampacashmobile.domain.common.Result<Web3AuthValidateResponse> {
        Timber.d(TAG, "🚀 ENTERING validateWeb3AuthToken method")
        return try {
            Timber.d(TAG, "🔐 Validating Web3Auth token with backend...")
            Timber.d(TAG, "📊 Web3Auth response: userInfo=${web3AuthResponse.userInfo?.name}, privKey=${web3AuthResponse.privKey?.take(10)}...")
            
            // Extract JWT token from Web3Auth response
            // Web3Auth provides the JWT token through userInfo.idToken
            val web3AuthJwt = web3AuthResponse.userInfo?.idToken
            Timber.d(TAG, "🔑 Extracted JWT token: ${web3AuthJwt?.take(20)}...")
            
            if (web3AuthJwt.isNullOrBlank()) {
                Timber.e(TAG, "❌ No JWT token found in Web3Auth response")
                return Result.failure(DomainError.AuthenticationError("No JWT token received from Web3Auth"))
            }
            
            // Check if this is a phone number login by examining the JWT token
            val isPhoneLogin = isPhoneNumberLogin(web3AuthJwt)
            Timber.d(TAG, "📱 Login type detected: ${if (isPhoneLogin) "Phone Number" else "Email/Google"}")
            
            // Create validation request
            val request = Web3AuthValidateRequest(token = web3AuthJwt)
            Timber.d(TAG, "📤 Sending validation request to backend...")
            
            // Call backend validation endpoint
            val response = apiClient.web3AuthApiService.validateWeb3AuthToken(request)
            Timber.d(TAG, "📥 Backend response received: success=${response.isSuccessful}")
            Timber.d(TAG, "📥 Backend response code: ${response.code()}")
            Timber.d(TAG, "📥 Backend response message: ${response.message()}")
            Timber.d(TAG, "📥 Backend response headers: ${response.headers()}")
            Timber.d(TAG, "📥 Backend response body (raw): ${response.body()}")
            Timber.d(TAG, "📥 Backend response error body: ${response.errorBody()?.string()}")
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                Timber.d(TAG, "✅ Backend validation successful")
                Timber.d(TAG, "👤 User: ${responseBody.user.email}")
                Timber.d(TAG, "🔑 API Token: ${responseBody.accessToken.take(20)}...")
                Timber.d(TAG, "🔍 Full response body: $responseBody")
                Timber.d(TAG, "🔍 Parsed user object: ${responseBody.user}")
                Timber.d(TAG, "🔍 User fields - ID: ${responseBody.user.id}, Email: ${responseBody.user.email}, FirstName: ${responseBody.user.firstName}, LastName: ${responseBody.user.lastName}")
                Timber.d(TAG, "🔍 User fields - Language: ${responseBody.user.language}, AuthProvider: ${responseBody.user.authProvider}, IsActive: ${responseBody.user.isActive}, Status: ${responseBody.user.status}")
                Timber.d(TAG, "🔍 User fields - VerificationStatus: ${responseBody.user.verificationStatus}, VerificationCompletedAt: ${responseBody.user.verificationCompletedAt}")
                Timber.d(TAG, "🔍 Response fields - AccessToken: ${responseBody.accessToken.take(50)}..., ExpiresIn: ${responseBody.expiresIn}")
                
                // Store the API JWT token
                apiClient.setAuthToken(responseBody.accessToken, responseBody.expiresIn)
                
                Timber.d(TAG, "✅ Web3Auth token validated and stored successfully")
                val result = com.example.rampacashmobile.domain.common.Result.success(responseBody)
                Timber.d(TAG, "🔍 Result created: ${result::class.simpleName}")
                result
            } else {
                // If backend validation fails, return authentication error
                Timber.e(TAG, "❌ Backend validation failed: ${response.code()} - ${response.message()}")
                Timber.e(TAG, "❌ Response body: ${response.body()}")
                val errorResult = com.example.rampacashmobile.domain.common.Result.failure<Web3AuthValidateResponse>(DomainError.AuthenticationError("Backend validation failed: ${response.message()}"))
                Timber.e(TAG, "🔍 Error result created: ${errorResult::class.simpleName}")
                return errorResult
            }
            
        } catch (e: Exception) {
            Timber.e(TAG, "🚨 EXCEPTION in validateWeb3AuthToken: ${e::class.simpleName}")
            Timber.e(e, "❌ Failed to validate Web3Auth token: ${e.message}")
            Timber.e(TAG, "🔍 Exception stack trace: ${e.stackTraceToString()}")
            val errorResult = com.example.rampacashmobile.domain.common.Result.failure<Web3AuthValidateResponse>(DomainError.NetworkError("Failed to validate Web3Auth token: ${e.message}"))
            Timber.e(TAG, "🔍 Exception result created: ${errorResult::class.simpleName}")
            errorResult
        }
    }
    
    /**
     * Check if the JWT token represents a phone number login
     */
    private fun isPhoneNumberLogin(jwtToken: String): Boolean {
        return try {
            // Decode JWT token to check the payload
            val parts = jwtToken.split(".")
            if (parts.size != 3) return false
            
            val payload = parts[1]
            val decoded = String(android.util.Base64.decode(payload, android.util.Base64.URL_SAFE))
            
            // Check if the token contains phone number patterns
            val containsPhonePattern = decoded.contains("\"verifierId\":\"+") || 
                                    decoded.contains("\"userId\":\"+") ||
                                    decoded.contains("\"name\":\"+") ||
                                    decoded.contains("web3auth-auth0-sms-passwordless")
            
            Timber.d(TAG, "🔍 JWT payload analysis: containsPhonePattern=$containsPhonePattern")
            Timber.d(TAG, "📄 JWT payload: $decoded")
            
            containsPhonePattern
        } catch (e: Exception) {
            Timber.e(TAG, "❌ Failed to decode JWT token: ${e.message}")
            false
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
        return tokenManager.isAuthenticated()
    }
    
    /**
     * Get stored access token
     */
    fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }
}
