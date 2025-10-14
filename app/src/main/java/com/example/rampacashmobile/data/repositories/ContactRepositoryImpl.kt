package com.example.rampacashmobile.data.repositories

import com.example.rampacashmobile.domain.entities.Contact
import com.example.rampacashmobile.domain.repositories.ContactRepository
import com.example.rampacashmobile.domain.valueobjects.UserId
import com.example.rampacashmobile.domain.valueobjects.Email
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor() : ContactRepository {
    
    private val contacts = mutableMapOf<String, Contact>()
    
    override suspend fun findById(id: String): Result<Contact> {
        return try {
            val contact = contacts[id]
            if (contact != null) {
                Result.success(contact)
            } else {
                Result.failure(DomainError.NotFound("Contact with id $id not found"))
            }
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find contact: ${e.message}"))
        }
    }
    
    override suspend fun findByUserId(userId: UserId): Result<List<Contact>> {
        return try {
            val userContacts = contacts.values.filter { it.userId == userId }
            Result.success(userContacts)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find contacts for user: ${e.message}"))
        }
    }
    
    override suspend fun findByUserId(userId: UserId, limit: Int, offset: Int): Result<List<Contact>> {
        return try {
            val userContacts = contacts.values
                .filter { it.userId == userId }
                .sortedBy { it.name }
                .drop(offset)
                .take(limit)
            Result.success(userContacts)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find contacts for user: ${e.message}"))
        }
    }
    
    override suspend fun findFavoritesByUserId(userId: UserId): Result<List<Contact>> {
        return try {
            val favoriteContacts = contacts.values.filter { it.userId == userId && it.isFavorite }
            Result.success(favoriteContacts)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find favorite contacts: ${e.message}"))
        }
    }
    
    override suspend fun findByName(userId: UserId, name: String): Result<List<Contact>> {
        return try {
            val matchingContacts = contacts.values.filter { 
                it.userId == userId && it.name.contains(name, ignoreCase = true) 
            }
            Result.success(matchingContacts)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find contacts by name: ${e.message}"))
        }
    }
    
    override suspend fun findByEmail(userId: UserId, email: Email): Result<Contact> {
        return try {
            val contact = contacts.values.find { it.userId == userId && it.email == email }
            if (contact != null) {
                Result.success(contact)
            } else {
                Result.failure(DomainError.NotFound("Contact with email ${email.value} not found"))
            }
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find contact by email: ${e.message}"))
        }
    }
    
    override suspend fun findByPhoneNumber(userId: UserId, phoneNumber: String): Result<Contact> {
        return try {
            val contact = contacts.values.find { it.userId == userId && it.phoneNumber == phoneNumber }
            if (contact != null) {
                Result.success(contact)
            } else {
                Result.failure(DomainError.NotFound("Contact with phone number $phoneNumber not found"))
            }
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find contact by phone number: ${e.message}"))
        }
    }
    
    override suspend fun findByWalletAddress(userId: UserId, walletAddress: WalletAddress): Result<Contact> {
        return try {
            val contact = contacts.values.find { it.userId == userId && it.walletAddress == walletAddress }
            if (contact != null) {
                Result.success(contact)
            } else {
                Result.failure(DomainError.NotFound("Contact with wallet address ${walletAddress.value} not found"))
            }
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find contact by wallet address: ${e.message}"))
        }
    }
    
    override suspend fun save(contact: Contact): Result<Unit> {
        return try {
            contacts[contact.id] = contact
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to save contact: ${e.message}"))
        }
    }
    
    override suspend fun update(contact: Contact): Result<Unit> {
        return try {
            contacts[contact.id] = contact
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to update contact: ${e.message}"))
        }
    }
    
    override suspend fun delete(id: String): Result<Unit> {
        return try {
            contacts.remove(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to delete contact: ${e.message}"))
        }
    }
    
    override suspend fun existsByEmail(userId: UserId, email: Email): Result<Boolean> {
        return try {
            val exists = contacts.values.any { it.userId == userId && it.email == email }
            Result.success(exists)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to check contact existence by email: ${e.message}"))
        }
    }
    
    override suspend fun existsByPhoneNumber(userId: UserId, phoneNumber: String): Result<Boolean> {
        return try {
            val exists = contacts.values.any { it.userId == userId && it.phoneNumber == phoneNumber }
            Result.success(exists)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to check contact existence by phone: ${e.message}"))
        }
    }
    
    override suspend fun existsByWalletAddress(userId: UserId, walletAddress: WalletAddress): Result<Boolean> {
        return try {
            val exists = contacts.values.any { it.userId == userId && it.walletAddress == walletAddress }
            Result.success(exists)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to check contact existence by wallet address: ${e.message}"))
        }
    }
    
    override suspend fun getCountByUserId(userId: UserId): Result<Int> {
        return try {
            val count = contacts.values.count { it.userId == userId }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to get contact count: ${e.message}"))
        }
    }
    
    override suspend fun getFavoriteCountByUserId(userId: UserId): Result<Int> {
        return try {
            val count = contacts.values.count { it.userId == userId && it.isFavorite }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to get favorite contact count: ${e.message}"))
        }
    }
    
    override suspend fun search(userId: UserId, query: String): Result<List<Contact>> {
        return try {
            val matchingContacts = contacts.values.filter { 
                it.userId == userId && 
                (it.name.contains(query, ignoreCase = true) || 
                 it.email?.value?.contains(query, ignoreCase = true) == true ||
                 it.phoneNumber?.contains(query, ignoreCase = true) == true)
            }
            Result.success(matchingContacts)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to search contacts: ${e.message}"))
        }
    }
}
