package com.example.rampacashmobile.data.api.model

import kotlinx.serialization.Serializable

/**
 * Create inquiry request
 */
@Serializable
data class CreateInquiryRequest(
    val name: String,
    val email: String,
    val inquiry: String? = null,
    val type: String = "GENERAL"
)

/**
 * Create waitlist inquiry request
 */
@Serializable
data class CreateWaitlistInquiryRequest(
    val name: String,
    val email: String,
    val inquiry: String? = null
)

/**
 * Inquiry response
 */
@Serializable
data class InquiryResponse(
    val id: Int,
    val name: String,
    val email: String,
    val inquiry: String? = null,
    val type: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)
