package com.example.rampacashmobile.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.entities.Contact
import com.example.rampacashmobile.domain.services.ContactDomainService
import com.example.rampacashmobile.domain.valueobjects.Email
import com.example.rampacashmobile.domain.valueobjects.UserId
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for contact-related operations
 * Uses DDD domain services for business logic
 */
@HiltViewModel
class ContactViewModel @Inject constructor(
    private val contactDomainService: ContactDomainService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "ContactViewModel"
    }

    // Contact state
    private val _contactState = MutableStateFlow(ContactState())
    val contactState: StateFlow<ContactState> = _contactState

    /**
     * Load all contacts for a user
     */
    fun loadContacts(userId: UserId) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _contactState.update { it.copy(isLoading = true, error = null) }
                
                val result = contactDomainService.getUserContacts(userId)
                
                when (result) {
                    is Result.Success -> {
                        _contactState.update { 
                            it.copy(
                                isLoading = false,
                                contacts = result.data,
                                error = null
                            )
                        }
                    }
                    is Result.Failure -> {
                        _contactState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.error
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load contacts", e)
                _contactState.update { 
                    it.copy(
                        isLoading = false,
                        error = com.example.rampacashmobile.domain.common.DomainError.NetworkError(
                            "Failed to load contacts: ${e.message}",
                            e
                        )
                    )
                }
            }
        }
    }

    /**
     * Create a new contact
     */
    fun createContact(
        userId: UserId,
        name: String,
        email: String? = null,
        phoneNumber: String? = null,
        walletAddress: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _contactState.update { it.copy(isCreating = true, error = null) }
                
                val emailValue = email?.let { Email.of(it) }
                val walletAddressValue = walletAddress?.let { WalletAddress.of(it) }
                
                val result = contactDomainService.createContact(
                    userId = userId,
                    name = name,
                    email = emailValue,
                    phoneNumber = phoneNumber,
                    walletAddress = walletAddressValue
                )
                
                when (result) {
                    is Result.Success -> {
                        // Reload contacts to include the new one
                        loadContacts(userId)
                        _contactState.update { 
                            it.copy(
                                isCreating = false,
                                error = null
                            )
                        }
                    }
                    is Result.Failure -> {
                        _contactState.update { 
                            it.copy(
                                isCreating = false,
                                error = result.error
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create contact", e)
                _contactState.update { 
                    it.copy(
                        isCreating = false,
                        error = com.example.rampacashmobile.domain.common.DomainError.NetworkError(
                            "Failed to create contact: ${e.message}",
                            e
                        )
                    )
                }
            }
        }
    }

    /**
     * Update an existing contact's name
     */
    fun updateContactName(contactId: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _contactState.update { it.copy(isUpdating = true, error = null) }
                
                val result = contactDomainService.updateContactName(contactId, newName)
                
                when (result) {
                    is Result.Success -> {
                        _contactState.update { 
                            it.copy(
                                isUpdating = false,
                                error = null
                            )
                        }
                    }
                    is Result.Failure -> {
                        _contactState.update { 
                            it.copy(
                                isUpdating = false,
                                error = result.error
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update contact name", e)
                _contactState.update { 
                    it.copy(
                        isUpdating = false,
                        error = com.example.rampacashmobile.domain.common.DomainError.NetworkError(
                            "Failed to update contact name: ${e.message}",
                            e
                        )
                    )
                }
            }
        }
    }

    /**
     * Delete a contact
     */
    fun deleteContact(contactId: String, userId: UserId) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _contactState.update { it.copy(isDeleting = true, error = null) }
                
                val result = contactDomainService.deleteContact(contactId)
                
                when (result) {
                    is Result.Success -> {
                        // Reload contacts to reflect changes
                        loadContacts(userId)
                        _contactState.update { 
                            it.copy(
                                isDeleting = false,
                                error = null
                            )
                        }
                    }
                    is Result.Failure -> {
                        _contactState.update { 
                            it.copy(
                                isDeleting = false,
                                error = result.error
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete contact", e)
                _contactState.update { 
                    it.copy(
                        isDeleting = false,
                        error = com.example.rampacashmobile.domain.common.DomainError.NetworkError(
                            "Failed to delete contact: ${e.message}",
                            e
                        )
                    )
                }
            }
        }
    }

    /**
     * Search contacts by name
     */
    fun searchContacts(query: String) {
        val currentContacts = _contactState.value.contacts
        val filteredContacts = if (query.isBlank()) {
            currentContacts
        } else {
            currentContacts.filter { contact ->
                contact.name.contains(query, ignoreCase = true) ||
                contact.email?.value?.contains(query, ignoreCase = true) == true ||
                contact.phoneNumber?.contains(query, ignoreCase = true) == true
            }
        }
        
        _contactState.update { 
            it.copy(
                searchQuery = query,
                filteredContacts = filteredContacts
            )
        }
    }

    /**
     * Clear search
     */
    fun clearSearch() {
        _contactState.update { 
            it.copy(
                searchQuery = "",
                filteredContacts = it.contacts
            )
        }
    }

    /**
     * Clear any error state
     */
    fun clearError() {
        _contactState.update { it.copy(error = null) }
    }
}

/**
 * State class for contact-related UI state
 * Aligned with our domain Contact entity
 */
data class ContactState(
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,
    val contacts: List<Contact> = emptyList(),
    val filteredContacts: List<Contact> = emptyList(),
    val searchQuery: String = "",
    val error: com.example.rampacashmobile.domain.common.DomainError? = null
)
