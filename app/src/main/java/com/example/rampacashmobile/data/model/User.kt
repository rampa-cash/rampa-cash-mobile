package com.example.rampacashmobile.data.model

import kotlinx.serialization.Serializable

/**
 * User data model matching backend User entity
 * This will be used to interface with backend APIs
 */
@Serializable
data class User(
    val id: String? = null, // UUID from backend
    val email: String = "",
    val phone: String? = null, // Optional, for contact discovery
    val firstName: String = "",
    val lastName: String = "",
    val language: Language = Language.EN,
    val authProvider: AuthProvider = AuthProvider.WEB3AUTH,
    val authProviderId: String = "", // External provider ID
    val isActive: Boolean = true,
    val status: UserStatus = UserStatus.ACTIVE,
    val createdAt: String? = null, // ISO timestamp from backend
    val updatedAt: String? = null, // ISO timestamp from backend
    val lastLoginAt: String? = null // ISO timestamp from backend
) {
    val fullName: String
        get() = "$firstName $lastName".trim()

    val displayName: String
        get() = if (fullName.isNotBlank()) fullName else email.takeIf { it.isNotBlank() } ?: phone ?: ""

    val initials: String
        get() = "${firstName.firstOrNull()?.uppercaseChar() ?: ""}${lastName.firstOrNull()?.uppercaseChar() ?: ""}"

    val isEmailVerified: Boolean
        get() = email.isNotBlank() && email.contains("@")

    val isPhoneVerified: Boolean
        get() = phone?.isNotBlank() == true
}

@Serializable
enum class Language(val code: String) {
    EN("en"),
    ES("es");

    companion object {
        fun fromCode(code: String): Language = values().find { it.code == code } ?: EN
    }
}

@Serializable
enum class AuthProvider(val value: String) {
    GOOGLE("google"),
    APPLE("apple"),
    WEB3AUTH("web3auth"),
    PHANTOM("phantom"),
    SOLFLARE("solflare");

    companion object {
        fun fromValue(value: String): AuthProvider = values().find { it.value == value } ?: WEB3AUTH
    }
}

@Serializable
enum class UserStatus(val value: String) {
    ACTIVE("active"),
    SUSPENDED("suspended");

    companion object {
        fun fromValue(value: String): UserStatus = values().find { it.value == value } ?: ACTIVE
    }
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
