package com.example.rampacashmobile.viewmodel

import android.content.Context
import timber.log.Timber
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.domain.valueobjects.UserId
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.utils.ErrorHandler
import com.example.rampacashmobile.data.repository.UserRepository
import com.web3auth.core.types.Provider
import com.web3auth.core.types.Web3AuthResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for user onboarding operations
 * Handles onboarding flow, data management, and user creation
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    companion object {
        private const val TAG = "OnboardingViewModel"
    }

    // Onboarding state
    private val _onboardingState = MutableStateFlow(OnboardingState())
    val onboardingState: StateFlow<OnboardingState> = _onboardingState

    // User data access
    val currentUser = userRepository.currentUser
    val onboardingData = userRepository.onboardingData

    /**
     * Set flag for onboarding navigation
     */
    fun setNeedsOnboardingNavigation(authProvider: String, existingEmail: String, existingPhone: String) {
        _onboardingState.update { 
            it.copy(
                needsOnboardingNavigation = true,
                authProvider = authProvider,
                existingEmail = existingEmail,
                existingPhone = existingPhone
            )
        }
    }

    /**
     * Clear onboarding navigation flag
     */
    fun clearOnboardingNavigation() {
        _onboardingState.update { 
            it.copy(
                needsOnboardingNavigation = false,
                authProvider = "",
                existingEmail = "",
                existingPhone = ""
            )
        }
    }

    /**
     * Extract user info from Web3Auth response for onboarding
     */
    fun extractUserInfoFromAuth(response: Web3AuthResponse, provider: Provider): Pair<String, String> {
        val userInfo = response.userInfo
        val email = userInfo?.email ?: ""
        val name = userInfo?.name ?: ""

        return when (provider) {
            Provider.GOOGLE, Provider.APPLE -> {
                // Email is primary, extract name parts
                val nameParts = name.split(" ", limit = 2)
                val firstName = nameParts.getOrNull(0) ?: ""
                val lastName = nameParts.getOrNull(1) ?: ""

                // Update onboarding data with pre-filled info
                val onboardingData = com.example.rampacashmobile.data.model.OnboardingData(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    authProvider = provider.toString().lowercase()
                )
                userRepository.updateOnboardingData(onboardingData)

                Pair(email, "")
            }
            Provider.SMS_PASSWORDLESS -> {
                // Phone is primary
                Pair("", "") // Phone number should be available from login process
            }
            else -> Pair("", "")
        }
    }

    /**
     * Check if user needs onboarding
     */
    fun needsOnboarding(): Boolean {
        return !userRepository.isOnboardingCompleted()
    }

    /**
     * Complete user onboarding with collected information
     */
    fun completeUserOnboarding(
        onboardingData: com.example.rampacashmobile.data.model.OnboardingData,
        walletAddressString: String,
        authProvider: String
    ) {
        viewModelScope.launch {
            try {
                _onboardingState.update { it.copy(isCompleting = true, error = null) }

                userRepository.updateOnboardingData(onboardingData)

                // Use value objects for type safety
                val walletAddress = WalletAddress.of(walletAddressString)
                val userId = UserId.generate() // Generate a new user ID

                // Complete onboarding and create user
                userRepository.completeOnboarding(walletAddress.value, authProvider)

                _onboardingState.update { 
                    it.copy(
                        isCompleting = false,
                        isCompleted = true,
                        error = null
                    )
                }

                Timber.d(TAG, "✅ User onboarding completed successfully")
            } catch (e: Exception) {
                val error = ErrorHandler.mapNetworkException(e, "Failed to complete user onboarding")
                ErrorHandler.logError(error, TAG)
                _onboardingState.update { 
                    it.copy(
                        isCompleting = false,
                        error = ErrorHandler.getUserFriendlyMessage(error)
                    )
                }
            }
        }
    }

    /**
     * Get auth provider from current state
     */
    fun getAuthProviderFromState(
        isWeb3AuthLoggedIn: Boolean,
        canTransact: Boolean
    ): String {
        return when {
            isWeb3AuthLoggedIn -> "web3auth"
            canTransact -> "wallet"
            else -> "unknown"
        }
    }

    /**
     * Clear error state
     */
    override fun clearError() {
        logErrorClearing("OnboardingViewModel")
        clearErrorInState(_onboardingState) { it.copy(error = null) }
    }

    /**
     * Reset onboarding state
     */
    fun resetOnboarding() {
        _onboardingState.update { OnboardingState() }
    }
}

/**
 * Onboarding state data class
 */
data class OnboardingState(
    val needsOnboardingNavigation: Boolean = false,
    val authProvider: String = "",
    val existingEmail: String = "",
    val existingPhone: String = "",
    val isCompleting: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
)
