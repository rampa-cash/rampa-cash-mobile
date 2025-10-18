package com.example.rampacashmobile.data.api.model

import kotlinx.serialization.Serializable

/**
 * Web3Auth validation request
 */
@Serializable
data class Web3AuthValidateRequest(
    val token: String
)

/**
 * Web3Auth validation response
 */
@Serializable
data class Web3AuthValidateResponse(
    val user: UserApiModel,
    val accessToken: String,
    val expiresIn: Int,
    val userId: String,
    val email: String? = null,
    val phone: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val language: String,
    val authProvider: String,
    val isActive: Boolean,
    val verificationStatus: String? = null,
    val status: String? = null,
    val verificationCompletedAt: String? = null,
    val canPerformFinancialOperations: Boolean? = null,
    val canBrowseApp: Boolean? = null,
    val shouldShowProfileCompletion: Boolean? = null
)

/**
 * User API model for backend communication
 */
@Serializable
data class UserApiModel(
    val id: String? = null,
    val email: String? = null, // Made optional for incomplete profiles
    val phone: String? = null,
    val firstName: String? = null, // Made optional for incomplete profiles
    val lastName: String? = null, // Made optional for incomplete profiles
    val language: String,
    val authProvider: String,
    val authProviderId: String? = null,
    val isActive: Boolean,
    val status: String,
    val verificationStatus: String? = null, // Added verification status
    val verificationCompletedAt: String? = null, // Added verification completion timestamp
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val lastLoginAt: String? = null
)

/**
 * User profile response
 */
@Serializable
data class UserProfileResponse(
    val id: String,
    val email: String? = null, // Made optional for incomplete profiles
    val firstName: String? = null, // Made optional for incomplete profiles
    val lastName: String? = null, // Made optional for incomplete profiles
    val language: String,
    val authProvider: String,
    val isActive: Boolean,
    val status: String,
    val verificationStatus: String? = null, // Added verification status
    val verificationCompletedAt: String? = null, // Added verification completion timestamp
    val createdAt: String,
    val lastLoginAt: String? = null
)

/**
 * Verification status response
 */
@Serializable
data class VerificationStatusResponse(
    val verificationStatus: String,
    val missingFields: List<String>,
    val isVerified: Boolean
)

/**
 * Missing fields response
 */
@Serializable
data class MissingFieldsResponse(
    val missingFields: List<String>,
    val isComplete: Boolean
)

/**
 * Complete profile request
 */
@Serializable
data class CompleteProfileRequest(
    val email: String? = null,
    val phone: String? = null,
    val firstName: String? = null,
    val lastName: String? = null
)

/**
 * Complete profile response
 */
@Serializable
data class CompleteProfileResponse(
    val user: UserApiModel,
    val message: String
)
