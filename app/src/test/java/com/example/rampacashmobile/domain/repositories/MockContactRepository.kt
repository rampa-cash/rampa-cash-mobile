package com.example.rampacashmobile.domain.repositories

import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.entities.Contact
import com.example.rampacashmobile.domain.valueobjects.ContactId
import com.example.rampacashmobile.domain.valueobjects.UserId
import javax.inject.Inject

class MockContactRepository @Inject constructor() : ContactRepository {
    
    private val contacts = mutableMapOf<String, Contact>()
    
    override suspend fun findById(id: ContactId): Result<Contact> {
        return contacts[id.value]?.let { Result.success(it) }
            ?: Result.failure(com.example.rampacashmobile.domain.common.DomainError.NotFound("Contact not found"))
    }
    
    override suspend fun findByUserId(userId: UserId): Result<List<Contact>> {
        val userContacts = contacts.values.filter { it.userId == userId }
        return Result.success(userContacts)
    }
    
    override suspend fun searchByUserId(userId: UserId, query: String): Result<List<Contact>> {
        val userContacts = contacts.values.filter { 
            it.userId == userId && 
            (it.name.contains(query, ignoreCase = true) || 
             it.email.value.contains(query, ignoreCase = true) ||
             it.phoneNumber.contains(query, ignoreCase = true))
        }
        return Result.success(userContacts)
    }
    
    override suspend fun save(contact: Contact): Result<Unit> {
        contacts[contact.id.value] = contact
        return Result.success(Unit)
    }
    
    override suspend fun update(contact: Contact): Result<Unit> {
        contacts[contact.id.value] = contact
        return Result.success(Unit)
    }
    
    override suspend fun delete(id: ContactId): Result<Unit> {
        contacts.remove(id.value)
        return Result.success(Unit)
    }
    
    fun addContact(contact: Contact) {
        contacts[contact.id.value] = contact
    }
    
    fun clear() {
        contacts.clear()
    }
}
