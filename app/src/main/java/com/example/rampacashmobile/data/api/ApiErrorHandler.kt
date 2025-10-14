package com.example.rampacashmobile.data.api

import com.example.rampacashmobile.data.api.model.ErrorResponse
import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.common.Result
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.UnknownHostException

/**
 * API Error Handler for handling HTTP errors and network issues
 */
class ApiErrorHandler {
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
    
    /**
     * Handle API errors and convert to domain errors
     */
    fun handleError(throwable: Throwable): Result.Failure {
        return when (throwable) {
            is HttpException -> handleHttpException(throwable)
            is UnknownHostException -> Result.Failure(DomainError.NetworkError("No internet connection"))
            is IOException -> Result.Failure(DomainError.NetworkError("Network error: ${throwable.message}"))
            else -> {
                Timber.e(throwable, "Unexpected error")
                Result.Failure(DomainError.UnknownError("Unexpected error: ${throwable.message}"))
            }
        }
    }
    
    private fun handleHttpException(httpException: HttpException): Result.Failure {
        val errorBody = try {
            httpException.response()?.errorBody()?.string()
        } catch (e: Exception) {
            null
        }
        
        val errorResponse = try {
            errorBody?.let { json.decodeFromString<ErrorResponse>(it) }
        } catch (e: Exception) {
            null
        }
        
        return when (httpException.code()) {
            400 -> Result.Failure(DomainError.ValidationError(errorResponse?.message ?: "Bad request"))
            401 -> Result.Failure(DomainError.AuthenticationError(errorResponse?.message ?: "Unauthorized"))
            403 -> Result.Failure(DomainError.AuthorizationError(errorResponse?.message ?: "Forbidden"))
            404 -> Result.Failure(DomainError.NotFoundError(errorResponse?.message ?: "Not found"))
            409 -> Result.Failure(DomainError.BusinessRuleViolation(errorResponse?.message ?: "Conflict"))
            422 -> Result.Failure(DomainError.ValidationError(errorResponse?.message ?: "Validation error"))
            429 -> Result.Failure(DomainError.RateLimitError(errorResponse?.message ?: "Too many requests"))
            500 -> Result.Failure(DomainError.ServerError(errorResponse?.message ?: "Internal server error"))
            502, 503, 504 -> Result.Failure(DomainError.ServerError(errorResponse?.message ?: "Service unavailable"))
            else -> {
                Timber.e("Unhandled HTTP error: ${httpException.code()}")
                Result.Failure(DomainError.UnknownError("HTTP ${httpException.code()}: ${errorResponse?.message ?: "Unknown error"}"))
            }
        }
    }
}
