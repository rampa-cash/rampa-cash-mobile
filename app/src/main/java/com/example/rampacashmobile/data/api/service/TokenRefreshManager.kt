package com.example.rampacashmobile.data.api.service

import com.example.rampacashmobile.data.api.TokenManager
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages token refresh operations with actual API calls
 * This service can make API calls to refresh tokens
 */
@Singleton
class TokenRefreshManager @Inject constructor(
    private val tokenManager: TokenManager
) {

    companion object {
        private const val TAG = "TokenRefreshManager"
    }

    /**
     * Check if token refresh is possible (simplified version)
     * In a real implementation, this would make an API call to refresh the token
     */
    suspend fun refreshToken(): Result<String> {
        return try {
            Timber.d(TAG, "🔄 Checking if token refresh is possible...")
            
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                Timber.e(TAG, "❌ No refresh token available")
                return Result.failure(DomainError.AuthenticationError("No refresh token available"))
            }

            // For now, we'll just clear the tokens and require re-authentication
            // In a production app, you would implement actual token refresh here
            Timber.w(TAG, "⚠️ Token refresh not fully implemented, clearing tokens for re-authentication")
            tokenManager.clearTokens()
            
            Result.failure(DomainError.AuthenticationError("Token refresh requires re-authentication"))
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Exception during token refresh: ${e.message}")
            Result.failure(DomainError.NetworkError("Token refresh failed: ${e.message}"))
        }
    }

    /**
     * Check if token refresh is possible
     */
    fun canRefreshToken(): Boolean {
        val refreshToken = tokenManager.getRefreshToken()
        return !refreshToken.isNullOrBlank()
    }

    /**
     * Synchronous token refresh check - for use in interceptors
     * This doesn't actually refresh the token, just checks if it should be refreshed
     */
    fun shouldRefreshTokenSync(): Boolean {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrBlank()) return false
        
        // Check if token is expired or close to expiry
        return tokenManager.isTokenExpired()
    }

    /**
     * Clear all tokens and force re-authentication
     */
    fun clearTokensAndForceReauth() {
        Timber.d(TAG, "🚪 Clearing tokens and forcing re-authentication")
        tokenManager.clearTokens()
    }
}
