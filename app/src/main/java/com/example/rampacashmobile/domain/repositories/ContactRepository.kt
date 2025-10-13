package com.example.rampacashmobile.domain.repositories

import com.example.rampacashmobile.domain.entities.Contact
import com.example.rampacashmobile.domain.valueobjects.UserId
import com.example.rampacashmobile.domain.valueobjects.Email
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.domain.common.Result

/**
 * Contact repository interface defining data access operations for Contact entities
 * 
 * This interface is part of the domain layer and defines the contract
 * for contact data access without specifying implementation details
 */
interface ContactRepository {
    
    /**
     * Find a contact by its unique ID
     * 
     * @param id The contact ID
     * @return Result containing the contact if found, or error if not found
     */
    suspend fun findById(id: String): Result<Contact>

    /**
     * Find all contacts for a specific user
     * 
     * @param userId The user ID
     * @return Result containing list of contacts
     */
    suspend fun findByUserId(userId: UserId): Result<List<Contact>>

    /**
     * Find contacts for a specific user with pagination
     * 
     * @param userId The user ID
     * @param limit The maximum number of contacts to return
     * @param offset The number of contacts to skip
     * @return Result containing list of contacts
     */
    suspend fun findByUserId(userId: UserId, limit: Int, offset: Int): Result<List<Contact>>

    /**
     * Find favorite contacts for a specific user
     * 
     * @param userId The user ID
     * @return Result containing list of favorite contacts
     */
    suspend fun findFavoritesByUserId(userId: UserId): Result<List<Contact>>

    /**
     * Find contacts by name (search functionality)
     * 
     * @param userId The user ID
     * @param name The name to search for
     * @return Result containing list of matching contacts
     */
    suspend fun findByName(userId: UserId, name: String): Result<List<Contact>>

    /**
     * Find contact by email
     * 
     * @param userId The user ID
     * @param email The email to search for
     * @return Result containing the contact if found
     */
    suspend fun findByEmail(userId: UserId, email: Email): Result<Contact>

    /**
     * Find contact by phone number
     * 
     * @param userId The user ID
     * @param phoneNumber The phone number to search for
     * @return Result containing the contact if found
     */
    suspend fun findByPhoneNumber(userId: UserId, phoneNumber: String): Result<Contact>

    /**
     * Find contact by wallet address
     * 
     * @param userId The user ID
     * @param walletAddress The wallet address to search for
     * @return Result containing the contact if found
     */
    suspend fun findByWalletAddress(userId: UserId, walletAddress: WalletAddress): Result<Contact>

    /**
     * Save a new contact
     * 
     * @param contact The contact to save
     * @return Result indicating success or failure
     */
    suspend fun save(contact: Contact): Result<Unit>

    /**
     * Update an existing contact
     * 
     * @param contact The contact to update
     * @return Result indicating success or failure
     */
    suspend fun update(contact: Contact): Result<Unit>

    /**
     * Delete a contact by its ID
     * 
     * @param id The contact ID
     * @return Result indicating success or failure
     */
    suspend fun delete(id: String): Result<Unit>

    /**
     * Check if a contact exists by email
     * 
     * @param userId The user ID
     * @param email The email to check
     * @return Result containing true if exists, false otherwise
     */
    suspend fun existsByEmail(userId: UserId, email: Email): Result<Boolean>

    /**
     * Check if a contact exists by phone number
     * 
     * @param userId The user ID
     * @param phoneNumber The phone number to check
     * @return Result containing true if exists, false otherwise
     */
    suspend fun existsByPhoneNumber(userId: UserId, phoneNumber: String): Result<Boolean>

    /**
     * Check if a contact exists by wallet address
     * 
     * @param userId The user ID
     * @param walletAddress The wallet address to check
     * @return Result containing true if exists, false otherwise
     */
    suspend fun existsByWalletAddress(userId: UserId, walletAddress: WalletAddress): Result<Boolean>

    /**
     * Get contact count for a user
     * 
     * @param userId The user ID
     * @return Result containing the contact count
     */
    suspend fun getCountByUserId(userId: UserId): Result<Int>

    /**
     * Get favorite contact count for a user
     * 
     * @param userId The user ID
     * @return Result containing the favorite contact count
     */
    suspend fun getFavoriteCountByUserId(userId: UserId): Result<Int>

    /**
     * Search contacts by query string
     * 
     * @param userId The user ID
     * @param query The search query
     * @return Result containing list of matching contacts
     */
    suspend fun search(userId: UserId, query: String): Result<List<Contact>>
}
