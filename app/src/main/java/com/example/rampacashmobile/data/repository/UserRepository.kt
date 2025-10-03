package com.example.rampacashmobile.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.rampacashmobile.data.model.User
import com.example.rampacashmobile.data.model.OnboardingData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing user data
 * Handles local storage and will interface with backend APIs
 */
@Singleton
class UserRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _onboardingData = MutableStateFlow(OnboardingData())
    val onboardingData: StateFlow<OnboardingData> = _onboardingData.asStateFlow()

    companion object {
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_ONBOARDING_DATA = "onboarding_data"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    init {
        loadUserFromStorage()
        loadOnboardingData()
    }

    /**
     * Load user data from local storage
     */
    private fun loadUserFromStorage() {
        val userData = preferences.getString(KEY_USER_DATA, null)
        if (userData != null) {
            try {
                val user = json.decodeFromString<User>(userData)
                _currentUser.value = user
            } catch (e: Exception) {
                // Handle serialization error
                _currentUser.value = null
            }
        }
    }

    /**
     * Load onboarding data from local storage
     */
    private fun loadOnboardingData() {
        val onboardingDataJson = preferences.getString(KEY_ONBOARDING_DATA, null)
        if (onboardingDataJson != null) {
            try {
                val data = json.decodeFromString<OnboardingData>(onboardingDataJson)
                _onboardingData.value = data
            } catch (e: Exception) {
                _onboardingData.value = OnboardingData()
            }
        }
    }

    /**
     * Save user data locally and sync with backend
     */
    suspend fun saveUser(user: User) {
        // Save locally first
        val userData = json.encodeToString(user)
        preferences.edit()
            .putString(KEY_USER_DATA, userData)
            .apply()

        _currentUser.value = user

        // TODO: Sync with backend API
        // syncUserWithBackend(user)
    }

    /**
     * Update onboarding data
     */
    fun updateOnboardingData(data: OnboardingData) {
        val onboardingDataJson = json.encodeToString(data)
        preferences.edit()
            .putString(KEY_ONBOARDING_DATA, onboardingDataJson)
            .apply()

        _onboardingData.value = data
    }

    /**
     * Complete onboarding and create user
     */
    suspend fun completeOnboarding(walletAddress: String, authProvider: String) {
        val onboarding = _onboardingData.value
        val user = User(
            firstName = onboarding.firstName,
            lastName = onboarding.lastName,
            email = onboarding.email,
            phoneNumber = onboarding.phoneNumber,
            walletAddress = walletAddress,
            authProvider = authProvider,
            isEmailVerified = authProvider == "google" || authProvider == "apple",
            isPhoneVerified = authProvider == "sms"
        )

        saveUser(user)
        markOnboardingCompleted()
    }

    /**
     * Mark onboarding as completed
     */
    private fun markOnboardingCompleted() {
        preferences.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .apply()
    }

    /**
     * Check if onboarding is completed
     */
    fun isOnboardingCompleted(): Boolean {
        return preferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * Clear user data (for logout)
     */
    fun clearUserData() {
        preferences.edit()
            .remove(KEY_USER_DATA)
            .remove(KEY_ONBOARDING_DATA)
            .remove(KEY_ONBOARDING_COMPLETED)
            .apply()

        _currentUser.value = null
        _onboardingData.value = OnboardingData()
    }

    // TODO: Backend API methods - implement when backend is ready

    /**
     * Create user in backend
     */
    private suspend fun createUserInBackend(user: User): Result<User> {
        // TODO: Implement API call
        // return apiService.createUser(user)
        return Result.success(user)
    }

    /**
     * Update user in backend
     */
    private suspend fun updateUserInBackend(user: User): Result<User> {
        // TODO: Implement API call
        // return apiService.updateUser(user.id!!, user)
        return Result.success(user)
    }

    /**
     * Fetch user from backend
     */
    private suspend fun fetchUserFromBackend(userId: String): Result<User> {
        // TODO: Implement API call
        // return apiService.getUser(userId)
        return Result.failure(NotImplementedError("Backend not implemented yet"))
    }

    /**
     * Sync user data with backend
     */
    private suspend fun syncUserWithBackend(user: User) {
        try {
            if (user.id == null) {
                // Create new user
                createUserInBackend(user)
            } else {
                // Update existing user
                updateUserInBackend(user)
            }
        } catch (e: Exception) {
            // Handle sync errors - for now just log
            // In production, implement retry logic
        }
    }
}
