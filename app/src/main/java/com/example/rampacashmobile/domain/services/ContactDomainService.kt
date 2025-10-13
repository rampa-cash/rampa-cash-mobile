package com.example.rampacashmobile.domain.services

import com.example.rampacashmobile.domain.entities.Contact
import com.example.rampacashmobile.domain.valueobjects.*
import com.example.rampacashmobile.domain.repositories.ContactRepository
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.exceptions.*
import javax.inject.Inject

/**
 * Contact domain service containing business logic for contact operations
 * 
 * This service encapsulates complex contact-related business rules
 * that don't naturally belong to the Contact entity itself
 */
class ContactDomainService @Inject constructor(
    private val contactRepository: ContactRepository
) {
    
    /**
     * Create a new contact
     * 
     * @param userId The user ID
     * @param name The contact name
     * @param email Optional email
     * @param phoneNumber Optional phone number
     * @param walletAddress Optional wallet address
     * @return Result containing the created contact
     */
    suspend fun createContact(
        userId: UserId,
        name: String,
        email: Email? = null,
        phoneNumber: String? = null,
        walletAddress: WalletAddress? = null
    ): Result<Contact> {
        return try {
            // Check if contact already exists with same email
            val emailCheck = email?.let { email ->
                contactRepository.existsByEmail(userId, email)
                    .flatMap { exists ->
                        if (exists) {
                            Result.failure(ContactAlreadyExistsException("Contact with email ${email.value} already exists"))
                        } else {
                            Result.success(Unit)
                        }
                    }
            } ?: Result.success(Unit)
            
            emailCheck.flatMap {
                // Check if contact already exists with same phone number
                phoneNumber?.let { phone ->
                    contactRepository.existsByPhoneNumber(userId, phone)
                        .flatMap { exists ->
                            if (exists) {
                                Result.failure(ContactAlreadyExistsException("Contact with phone number $phone already exists"))
                            } else {
                                Result.success(Unit)
                            }
                        }
                } ?: Result.success(Unit)
            }.flatMap {
                // Check if contact already exists with same wallet address
                walletAddress?.let { wallet ->
                    contactRepository.existsByWalletAddress(userId, wallet)
                        .flatMap { exists ->
                            if (exists) {
                                Result.failure(ContactAlreadyExistsException("Contact with wallet address ${wallet.value} already exists"))
                            } else {
                                Result.success(Unit)
                            }
                        }
                } ?: Result.success(Unit)
            }.flatMap {
                // Create contact
                Contact.create(userId, name, email, phoneNumber, walletAddress)
                    .flatMap { contact ->
                        contactRepository.save(contact).map { contact }
                    }
            }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to create contact: ${e.message}", e))
        }
    }

    /**
     * Get all contacts for a user
     * 
     * @param userId The user ID
     * @param limit Optional limit
     * @param offset Optional offset
     * @return Result containing list of contacts
     */
    suspend fun getUserContacts(
        userId: UserId,
        limit: Int? = null,
        offset: Int? = null
    ): Result<List<Contact>> {
        return if (limit != null && offset != null) {
            contactRepository.findByUserId(userId, limit, offset)
        } else {
            contactRepository.findByUserId(userId)
        }
    }

    /**
     * Get favorite contacts for a user
     * 
     * @param userId The user ID
     * @return Result containing list of favorite contacts
     */
    suspend fun getFavoriteContacts(userId: UserId): Result<List<Contact>> {
        return contactRepository.findFavoritesByUserId(userId)
    }

    /**
     * Search contacts by name
     * 
     * @param userId The user ID
     * @param query The search query
     * @return Result containing list of matching contacts
     */
    suspend fun searchContacts(userId: UserId, query: String): Result<List<Contact>> {
        return contactRepository.search(userId, query)
    }

    /**
     * Get contact by ID
     * 
     * @param contactId The contact ID
     * @return Result containing the contact
     */
    suspend fun getContact(contactId: String): Result<Contact> {
        return contactRepository.findById(contactId)
    }

    /**
     * Update contact name
     * 
     * @param contactId The contact ID
     * @param newName The new name
     * @return Result indicating success or failure
     */
    suspend fun updateContactName(contactId: String, newName: String): Result<Unit> {
        return try {
            contactRepository.findById(contactId)
                .flatMap { contact ->
                    contact.updateName(newName)
                        .flatMap {
                            contactRepository.update(contact)
                        }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to update contact name: ${e.message}", e))
        }
    }

    /**
     * Update contact email
     * 
     * @param contactId The contact ID
     * @param newEmail The new email
     * @return Result indicating success or failure
     */
    suspend fun updateContactEmail(contactId: String, newEmail: Email?): Result<Unit> {
        return try {
            contactRepository.findById(contactId)
                .flatMap { contact ->
                    // Check if email already exists for another contact
                    newEmail?.let { email ->
                        contactRepository.existsByEmail(contact.userId, email)
                            .flatMap { exists ->
                                if (exists) {
                                    Result.failure(ContactAlreadyExistsException("Contact with email ${email.value} already exists"))
                                } else {
                                    Result.success(Unit)
                                }
                            }
                    } ?: Result.success(Unit)
                }
                .flatMap {
                    contactRepository.findById(contactId)
                        .flatMap { contact ->
                            contact.updateEmail(newEmail)
                                .flatMap {
                                    contactRepository.update(contact)
                                }
                        }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to update contact email: ${e.message}", e))
        }
    }

    /**
     * Update contact phone number
     * 
     * @param contactId The contact ID
     * @param newPhoneNumber The new phone number
     * @return Result indicating success or failure
     */
    suspend fun updateContactPhoneNumber(contactId: String, newPhoneNumber: String?): Result<Unit> {
        return try {
            contactRepository.findById(contactId)
                .flatMap { contact ->
                    // Check if phone number already exists for another contact
                    newPhoneNumber?.let { phone ->
                        contactRepository.existsByPhoneNumber(contact.userId, phone)
                            .flatMap { exists ->
                                if (exists) {
                                    Result.failure(ContactAlreadyExistsException("Contact with phone number $phone already exists"))
                                } else {
                                    Result.success(Unit)
                                }
                            }
                    } ?: Result.success(Unit)
                }
                .flatMap {
                    contactRepository.findById(contactId)
                        .flatMap { contact ->
                            contact.updatePhoneNumber(newPhoneNumber)
                                .flatMap {
                                    contactRepository.update(contact)
                                }
                        }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to update contact phone number: ${e.message}", e))
        }
    }

    /**
     * Update contact wallet address
     * 
     * @param contactId The contact ID
     * @param newWalletAddress The new wallet address
     * @return Result indicating success or failure
     */
    suspend fun updateContactWalletAddress(contactId: String, newWalletAddress: WalletAddress?): Result<Unit> {
        return try {
            contactRepository.findById(contactId)
                .flatMap { contact ->
                    // Check if wallet address already exists for another contact
                    newWalletAddress?.let { wallet ->
                        contactRepository.existsByWalletAddress(contact.userId, wallet)
                            .flatMap { exists ->
                                if (exists) {
                                    Result.failure(ContactAlreadyExistsException("Contact with wallet address ${wallet.value} already exists"))
                                } else {
                                    Result.success(Unit)
                                }
                            }
                    } ?: Result.success(Unit)
                }
                .flatMap {
                    contactRepository.findById(contactId)
                        .flatMap { contact ->
                            contact.updateWalletAddress(newWalletAddress)
                                .flatMap {
                                    contactRepository.update(contact)
                                }
                        }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to update contact wallet address: ${e.message}", e))
        }
    }

    /**
     * Add contact to favorites
     * 
     * @param contactId The contact ID
     * @return Result indicating success or failure
     */
    suspend fun addToFavorites(contactId: String): Result<Unit> {
        return try {
            contactRepository.findById(contactId)
                .flatMap { contact ->
                    contact.addToFavorites()
                        .flatMap {
                            contactRepository.update(contact)
                        }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to add contact to favorites: ${e.message}", e))
        }
    }

    /**
     * Remove contact from favorites
     * 
     * @param contactId The contact ID
     * @return Result indicating success or failure
     */
    suspend fun removeFromFavorites(contactId: String): Result<Unit> {
        return try {
            contactRepository.findById(contactId)
                .flatMap { contact ->
                    contact.removeFromFavorites()
                        .flatMap {
                            contactRepository.update(contact)
                        }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to remove contact from favorites: ${e.message}", e))
        }
    }

    /**
     * Delete a contact
     * 
     * @param contactId The contact ID
     * @return Result indicating success or failure
     */
    suspend fun deleteContact(contactId: String): Result<Unit> {
        return try {
            contactRepository.findById(contactId)
                .flatMap { contact ->
                    contactRepository.delete(contactId)
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to delete contact: ${e.message}", e))
        }
    }

    /**
     * Get contact count for a user
     * 
     * @param userId The user ID
     * @return Result containing the contact count
     */
    suspend fun getContactCount(userId: UserId): Result<Int> {
        return contactRepository.getCountByUserId(userId)
    }

    /**
     * Get favorite contact count for a user
     * 
     * @param userId The user ID
     * @return Result containing the favorite contact count
     */
    suspend fun getFavoriteContactCount(userId: UserId): Result<Int> {
        return contactRepository.getFavoriteCountByUserId(userId)
    }
}
