package com.example.rampacashmobile.domain.repositories

import com.example.rampacashmobile.domain.entities.User
import com.example.rampacashmobile.domain.valueobjects.UserId
import com.example.rampacashmobile.domain.valueobjects.Email
import com.example.rampacashmobile.domain.common.Result

/**
 * User repository interface defining data access operations for User entities
 * 
 * This interface is part of the domain layer and defines the contract
 * for user data access without specifying implementation details
 */
interface UserRepository {
    
    /**
     * Find a user by their unique ID
     * 
     * @param id The user ID
     * @return Result containing the user if found, or error if not found
     */
    suspend fun findById(id: UserId): Result<User>

    /**
     * Find a user by their email address
     * 
     * @param email The user's email
     * @return Result containing the user if found, or error if not found
     */
    suspend fun findByEmail(email: Email): Result<User>

    /**
     * Find a user by their authentication provider and provider ID
     * 
     * @param authProvider The authentication provider (google, apple, sms, wallet)
     * @param authProviderId The provider-specific user ID
     * @return Result containing the user if found, or error if not found
     */
    suspend fun findByAuthProvider(authProvider: String, authProviderId: String): Result<User>

    /**
     * Save a new user
     * 
     * @param user The user to save
     * @return Result indicating success or failure
     */
    suspend fun save(user: User): Result<Unit>

    /**
     * Update an existing user
     * 
     * @param user The user to update
     * @return Result indicating success or failure
     */
    suspend fun update(user: User): Result<Unit>

    /**
     * Delete a user by their ID
     * 
     * @param id The user ID
     * @return Result indicating success or failure
     */
    suspend fun delete(id: UserId): Result<Unit>

    /**
     * Check if a user exists by email
     * 
     * @param email The email to check
     * @return Result containing true if exists, false otherwise
     */
    suspend fun existsByEmail(email: Email): Result<Boolean>

    /**
     * Check if a user exists by authentication provider
     * 
     * @param authProvider The authentication provider
     * @param authProviderId The provider-specific user ID
     * @return Result containing true if exists, false otherwise
     */
    suspend fun existsByAuthProvider(authProvider: String, authProviderId: String): Result<Boolean>

    /**
     * Find all users with a specific status
     * 
     * @param status The user status to filter by
     * @return Result containing list of users
     */
    suspend fun findByStatus(status: String): Result<List<User>>

    /**
     * Find all users created within a date range
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return Result containing list of users
     */
    suspend fun findByCreatedAtBetween(startDate: String, endDate: String): Result<List<User>>
}
