package com.example.rampacashmobile.data.api.model

import kotlinx.serialization.Serializable

/**
 * Base API response wrapper
 */
@Serializable
data class ApiResponse<T>(
    val data: T? = null,
    val message: String? = null,
    val success: Boolean = true
)

/**
 * Error response from API
 */
@Serializable
data class ErrorResponse(
    val statusCode: Int,
    val message: String,
    val error: String,
    val timestamp: String,
    val path: String
)

/**
 * Pagination response
 */
@Serializable
data class PaginationResponse(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)

/**
 * Paginated API response
 */
@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val pagination: PaginationResponse
)
