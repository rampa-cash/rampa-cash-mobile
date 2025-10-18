package com.example.rampacashmobile.data.api.service

import com.example.rampacashmobile.data.api.ApiClient
import com.example.rampacashmobile.data.api.TokenManager
import com.example.rampacashmobile.data.api.model.Web3AuthValidateRequest
import com.example.rampacashmobile.data.api.model.Web3AuthValidateResponse
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling JWT token refresh operations
 */
@Singleton
class TokenRefreshService @Inject constructor(
    private val tokenManager: TokenManager
) {

    companion object {
        private const val TAG = "TokenRefreshService"
        private const val REFRESH_THRESHOLD_SECONDS = 300 // Refresh 5 minutes before expiry
    }

    /**
     * Check if token needs refresh
     */
    fun shouldRefreshToken(): Boolean {
        return try {
            Timber.d(TAG, "🔄 Checking if token needs refresh...")
            
            if (!tokenManager.isAuthenticated()) {
                Timber.d(TAG, "❌ User not authenticated, cannot refresh token")
                return false
            }

            val token = tokenManager.getAccessToken()
            if (token.isNullOrBlank()) {
                Timber.d(TAG, "❌ No access token available")
                return false
            }

            // Check if token is expired or close to expiry
            val isExpired = tokenManager.isTokenExpired()
            val needsRefresh = isExpired || isTokenCloseToExpiry(token)
            
            Timber.d(TAG, "🕐 Token needs refresh: $needsRefresh")
            needsRefresh
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to check token refresh status: ${e.message}")
            false
        }
    }

    /**
     * Get current valid token
     */
    fun getCurrentToken(): String? {
        return if (tokenManager.isAuthenticated()) {
            tokenManager.getAccessToken()
        } else {
            null
        }
    }

    /**
     * Check if token is close to expiry
     */
    private fun isTokenCloseToExpiry(token: String): Boolean {
        return try {
            val payload = token.split(".")[1]
            val decoded = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
            val json = String(decoded)
            
            // Extract exp field from JWT payload
            val expRegex = "\"exp\":(\\d+)".toRegex()
            val matchResult = expRegex.find(json)
            val exp = matchResult?.groupValues?.get(1)?.toLongOrNull()
            
            exp?.let { expiry ->
                val currentTime = System.currentTimeMillis() / 1000
                val timeUntilExpiry = expiry - currentTime
                
                Timber.d(TAG, "🕐 Token expires in ${timeUntilExpiry} seconds")
                return timeUntilExpiry <= REFRESH_THRESHOLD_SECONDS
            } ?: false
        } catch (e: Exception) {
            Timber.w(TAG, "⚠️ Could not parse JWT token for expiry check: ${e.message}")
            false
        }
    }
}
