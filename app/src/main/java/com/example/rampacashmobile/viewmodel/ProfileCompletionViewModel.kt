package com.example.rampacashmobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.example.rampacashmobile.ui.screens.ProfileCompletionUiState
import com.example.rampacashmobile.data.api.service.UserVerificationService
import com.example.rampacashmobile.data.api.model.CompleteProfileRequest

/**
 * ViewModel for profile completion screen
 * 
 * Handles user profile completion logic including validation,
 * API calls, and state management.
 */
@HiltViewModel
class ProfileCompletionViewModel @Inject constructor(
    private val userVerificationService: UserVerificationService
) : ViewModel() {

    companion object {
        private const val TAG = "ProfileCompletionViewModel"
    }

    private val _uiState = MutableStateFlow(ProfileCompletionUiState())
    val uiState: StateFlow<ProfileCompletionUiState> = _uiState

    /**
     * Update email field
     */
    fun updateEmail(email: String) {
        _uiState.update { 
            it.copy(
                email = email,
                emailError = validateEmail(email)
            )
        }
    }

    /**
     * Update first name field
     */
    fun updateFirstName(firstName: String) {
        _uiState.update { 
            it.copy(
                firstName = firstName,
                firstNameError = validateFirstName(firstName)
            )
        }
    }

    /**
     * Update last name field
     */
    fun updateLastName(lastName: String) {
        _uiState.update { 
            it.copy(
                lastName = lastName,
                lastNameError = validateLastName(lastName)
            )
        }
    }

    /**
     * Update phone field
     */
    fun updatePhone(phone: String) {
        _uiState.update { 
            it.copy(
                phone = phone,
                phoneError = validatePhone(phone)
            )
        }
    }

    /**
     * Complete user profile
     */
    fun completeProfile() {
        val currentState = _uiState.value
        
        // Validate all required fields
        val emailError = validateEmail(currentState.email)
        val firstNameError = validateFirstName(currentState.firstName)
        val lastNameError = validateLastName(currentState.lastName)
        val phoneError = validatePhone(currentState.phone)
        
        if (emailError != null || firstNameError != null || lastNameError != null || phoneError != null) {
            _uiState.update { 
                it.copy(
                    emailError = emailError,
                    firstNameError = firstNameError,
                    lastNameError = lastNameError,
                    phoneError = phoneError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                Timber.d(TAG, "🔄 Completing user profile...")
                
                val result = userVerificationService.completeProfile(
                    CompleteProfileRequest(
                        email = currentState.email,
                        phone = currentState.phone.takeIf { it.isNotBlank() },
                        firstName = currentState.firstName,
                        lastName = currentState.lastName
                    )
                )
                
                when (result) {
                    is com.example.rampacashmobile.domain.common.Result.Success -> {
                        Timber.d(TAG, "✅ Profile completed successfully")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isProfileCompleted = true
                            )
                        }
                    }
                    is com.example.rampacashmobile.domain.common.Result.Failure -> {
                        Timber.e(TAG, "❌ Profile completion failed: ${result.error.message}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "Failed to complete profile: ${result.error.message}"
                            )
                        }
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(TAG, "❌ Exception during profile completion: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to complete profile: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Validate email address
     */
    private fun validateEmail(email: String): String? {
        if (email.isBlank()) {
            return "Email is required"
        }
        
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!emailRegex.matches(email)) {
            return "Please enter a valid email address"
        }
        
        return null
    }

    /**
     * Validate first name
     */
    private fun validateFirstName(firstName: String): String? {
        if (firstName.isBlank()) {
            return "First name is required"
        }
        
        if (firstName.length < 2) {
            return "First name must be at least 2 characters"
        }
        
        if (firstName.length > 50) {
            return "First name must be less than 50 characters"
        }
        
        return null
    }

    /**
     * Validate last name
     */
    private fun validateLastName(lastName: String): String? {
        if (lastName.isBlank()) {
            return "Last name is required"
        }
        
        if (lastName.length < 2) {
            return "Last name must be at least 2 characters"
        }
        
        if (lastName.length > 50) {
            return "Last name must be less than 50 characters"
        }
        
        return null
    }

    /**
     * Validate phone number (optional)
     */
    private fun validatePhone(phone: String): String? {
        if (phone.isBlank()) {
            return null // Phone is optional
        }
        
        val phoneRegex = "^\\+?[1-9]\\d{1,14}$".toRegex()
        if (!phoneRegex.matches(phone)) {
            return "Please enter a valid phone number"
        }
        
        return null
    }
}
