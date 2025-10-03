package com.example.rampacashmobile.data.model

import kotlinx.serialization.Serializable

/**
 * User data model for storing user information
 * This will be used to interface with backend APIs
 */
@Serializable
data class User(
    val id: String? = null, // Backend will provide this
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String? = null,
    val walletAddress: String = "",
    val authProvider: String = "", // "sms", "google", "apple", "wallet"
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val createdAt: String? = null, // ISO timestamp from backend
    val updatedAt: String? = null  // ISO timestamp from backend
) {
    val fullName: String
        get() = "$firstName $lastName".trim()

    val displayName: String
        get() = if (fullName.isNotBlank()) fullName else email.takeIf { it.isNotBlank() } ?: phoneNumber

    val initials: String
        get() = "${firstName.firstOrNull()?.uppercaseChar() ?: ""}${lastName.firstOrNull()?.uppercaseChar() ?: ""}"
}

/**
 * User onboarding step data
 */
@Serializable
data class OnboardingData(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val authProvider: String = "",
    val currentStep: OnboardingStep = OnboardingStep.USER_INFO
)

@Serializable
enum class OnboardingStep {
    USER_INFO,      // Collect name and missing contact info
    VERIFICATION,   // Verify email/phone if needed
    COMPLETED       // Onboarding finished
}
