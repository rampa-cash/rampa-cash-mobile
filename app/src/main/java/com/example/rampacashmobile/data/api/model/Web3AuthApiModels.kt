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
    val email: String,
    val firstName: String,
    val lastName: String,
    val language: String,
    val authProvider: String,
    val isActive: Boolean
)

/**
 * User API model for backend communication
 */
@Serializable
data class UserApiModel(
    val id: String? = null,
    val email: String,
    val phone: String? = null,
    val firstName: String,
    val lastName: String,
    val language: String,
    val authProvider: String,
    val authProviderId: String,
    val isActive: Boolean,
    val status: String,
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
    val email: String,
    val firstName: String,
    val lastName: String,
    val language: String,
    val authProvider: String,
    val isActive: Boolean,
    val status: String,
    val createdAt: String,
    val lastLoginAt: String? = null
)
