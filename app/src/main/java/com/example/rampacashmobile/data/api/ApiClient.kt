package com.example.rampacashmobile.data.api

import com.example.rampacashmobile.BuildConfig
import com.example.rampacashmobile.data.api.service.*
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API Client for backend communication
 * Handles Retrofit configuration, JWT token injection, and error handling
 */
@Singleton
class ApiClient @Inject constructor(
    private val tokenManager: TokenManager
) {
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()
        val token = getAuthToken()
        
        val newRequest = if (token != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        
        chain.proceed(newRequest)
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
    
    // API Services
    val web3AuthApiService: Web3AuthApiService = retrofit.create(Web3AuthApiService::class.java)
    val walletApiService: WalletApiService = retrofit.create(WalletApiService::class.java)
    val transactionApiService: TransactionApiService = retrofit.create(TransactionApiService::class.java)
    val contactApiService: ContactApiService = retrofit.create(ContactApiService::class.java)
    val visaCardApiService: VISACardApiService = retrofit.create(VISACardApiService::class.java)
    val onOffRampApiService: OnOffRampApiService = retrofit.create(OnOffRampApiService::class.java)
    val inquiryApiService: InquiryApiService = retrofit.create(InquiryApiService::class.java)
    
    /**
     * Get authentication token from secure storage
     */
    private fun getAuthToken(): String? {
        return tokenManager.getAccessToken()
    }
    
    /**
     * Set authentication token
     */
    fun setAuthToken(token: String, expiresIn: Int? = null) {
        tokenManager.updateAccessToken(token, expiresIn)
    }
    
    /**
     * Clear authentication token
     */
    fun clearAuthToken() {
        tokenManager.clearTokens()
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return tokenManager.isAuthenticated()
    }
}
