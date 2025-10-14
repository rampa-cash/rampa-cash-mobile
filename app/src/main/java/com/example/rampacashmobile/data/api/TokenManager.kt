package com.example.rampacashmobile.data.api

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JWT Token Manager for secure token storage and management
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "api_tokens"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Store JWT tokens securely
     */
    fun storeTokens(accessToken: String, refreshToken: String? = null, expiresIn: Int? = null) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            expiresIn?.let { 
                val expiryTime = System.currentTimeMillis() + (it * 1000L)
                putLong(KEY_TOKEN_EXPIRY, expiryTime)
            }
            apply()
        }
    }
    
    /**
     * Get access token
     */
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    /**
     * Get refresh token
     */
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    /**
     * Check if token is expired
     */
    fun isTokenExpired(): Boolean {
        val expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0L)
        return expiryTime > 0 && System.currentTimeMillis() >= expiryTime
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        val token = getAccessToken()
        return !token.isNullOrBlank() && !isTokenExpired()
    }
    
    /**
     * Clear all tokens
     */
    fun clearTokens() {
        prefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_TOKEN_EXPIRY)
            apply()
        }
    }
    
    /**
     * Update access token
     */
    fun updateAccessToken(accessToken: String, expiresIn: Int? = null) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            expiresIn?.let { 
                val expiryTime = System.currentTimeMillis() + (it * 1000L)
                putLong(KEY_TOKEN_EXPIRY, expiryTime)
            }
            apply()
        }
    }
}
