package com.example.rampacashmobile.domain.common

/**
 * Result type for error handling in domain layer
 * 
 * This sealed class provides a functional approach to error handling
 * without throwing exceptions, making the code more predictable and testable
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val error: DomainError) : Result<Nothing>()

    /**
     * Returns true if this is a Success result
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns true if this is a Failure result
     */
    val isFailure: Boolean get() = this is Failure

    /**
     * Returns the data if this is a Success, null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    /**
     * Returns the data if this is a Success, or the default value otherwise
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Failure -> defaultValue
    }

    /**
     * Returns the data if this is a Success, or throws the error otherwise
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Failure -> throw RuntimeException(error.message, error.cause)
    }

    /**
     * Transforms the success value using the given function
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    /**
     * Transforms the success value using the given function that returns a Result
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Failure -> this
    }

    /**
     * Executes the given function if this is a Success
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> = also {
        if (it is Success) action(it.data)
    }

    /**
     * Executes the given function if this is a Failure
     */
    inline fun onFailure(action: (DomainError) -> Unit): Result<T> = also {
        if (it is Failure) action(it.error)
    }

    /**
     * Executes the appropriate function based on the result type
     */
    inline fun fold(
        onSuccess: (T) -> Unit,
        onFailure: (DomainError) -> Unit
    ): Unit = when (this) {
        is Success -> onSuccess(data)
        is Failure -> onFailure(error)
    }

    companion object {
        /**
         * Creates a Success result with the given data
         */
        fun <T> success(data: T): Result<T> = Success(data)

        /**
         * Creates a Failure result with the given error
         */
        fun <T> failure(error: DomainError): Result<T> = Failure(error)

        /**
         * Creates a Failure result with the given message
         */
        fun <T> failure(message: String, cause: Throwable? = null): Result<T> = 
            Failure(DomainError.ValidationError(message, cause))

        /**
         * Creates a Failure result with the given exception
         */
        fun <T> failure(exception: Exception): Result<T> = 
            Failure(DomainError.ValidationError(exception.message ?: "Unknown error", exception))
    }
}

/**
 * Extension function to convert a nullable value to a Result
 */
fun <T> T?.toResult(errorMessage: String = "Value is null"): Result<T> = 
    if (this != null) Result.success(this) else Result.failure(errorMessage)

/**
 * Extension function to convert a nullable value to a Result with custom error
 */
fun <T> T?.toResult(error: DomainError): Result<T> = 
    if (this != null) Result.success(this) else Result.failure(error)

/**
 * Extension function to convert a Boolean to a Result
 */
fun Boolean.toResult(errorMessage: String = "Condition failed"): Result<Unit> = 
    if (this) Result.success(Unit) else Result.failure(errorMessage)

/**
 * Extension function to convert a Boolean to a Result with custom error
 */
fun Boolean.toResult(error: DomainError): Result<Unit> = 
    if (this) Result.success(Unit) else Result.failure(error)
