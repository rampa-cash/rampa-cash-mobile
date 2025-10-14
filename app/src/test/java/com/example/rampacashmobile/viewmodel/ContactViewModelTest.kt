package com.example.rampacashmobile.viewmodel

import com.example.rampacashmobile.domain.services.ContactDomainService
import com.example.rampacashmobile.domain.valueobjects.*
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.entities.Contact
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ContactViewModelTest {

    private lateinit var contactDomainService: ContactDomainService
    private lateinit var contactViewModel: ContactViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        contactDomainService = mockk()
        contactViewModel = ContactViewModel(contactDomainService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadContacts with valid user should update state`() = runTest {
        // Given
        val userId = UserId.of("user_123")
        val contacts = listOf(createTestContact())
        
        every { contactDomainService.getUserContacts(userId) } returns Result.success(contacts)

        // When
        contactViewModel.loadContacts(userId)

        // Then
        val state = contactViewModel.contactState.value
        assertEquals(1, state.contacts.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadContacts with error should update error state`() = runTest {
        // Given
        val userId = UserId.of("user_123")
        val error = DomainError.NetworkError("Network error")
        
        every { contactDomainService.getUserContacts(userId) } returns Result.failure(error)

        // When
        contactViewModel.loadContacts(userId)

        // Then
        val state = contactViewModel.contactState.value
        assertEquals(error, state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `addContact with valid data should succeed`() = runTest {
        // Given
        val userId = UserId.of("user_123")
        val name = "John Doe"
        val email = Email.of("john@example.com")
        val phoneNumber = "+1234567890"
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val contact = createTestContact()
        
        every { contactDomainService.addContact(userId, name, email, phoneNumber, walletAddress) } returns Result.success(contact)

        // When
        contactViewModel.addContact(userId, name, email, phoneNumber, walletAddress)

        // Then
        verify { contactDomainService.addContact(userId, name, email, phoneNumber, walletAddress) }
    }

    @Test
    fun `addContact with error should update error state`() = runTest {
        // Given
        val userId = UserId.of("user_123")
        val name = "John Doe"
        val email = Email.of("john@example.com")
        val phoneNumber = "+1234567890"
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val error = DomainError.ValidationError("Invalid contact data")
        
        every { contactDomainService.addContact(userId, name, email, phoneNumber, walletAddress) } returns Result.failure(error)

        // When
        contactViewModel.addContact(userId, name, email, phoneNumber, walletAddress)

        // Then
        val state = contactViewModel.contactState.value
        assertEquals(error, state.error)
    }

    @Test
    fun `updateContact with valid data should succeed`() = runTest {
        // Given
        val contactId = ContactId.of("contact_123")
        val name = "John Updated"
        val email = Email.of("john.updated@example.com")
        val phoneNumber = "+1234567890"
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val contact = createTestContact()
        
        every { contactDomainService.updateContact(contactId, name, email, phoneNumber, walletAddress) } returns Result.success(contact)

        // When
        contactViewModel.updateContact(contactId, name, email, phoneNumber, walletAddress)

        // Then
        verify { contactDomainService.updateContact(contactId, name, email, phoneNumber, walletAddress) }
    }

    @Test
    fun `updateContact with error should update error state`() = runTest {
        // Given
        val contactId = ContactId.of("contact_123")
        val name = "John Updated"
        val email = Email.of("john.updated@example.com")
        val phoneNumber = "+1234567890"
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val error = DomainError.NotFound("Contact not found")
        
        every { contactDomainService.updateContact(contactId, name, email, phoneNumber, walletAddress) } returns Result.failure(error)

        // When
        contactViewModel.updateContact(contactId, name, email, phoneNumber, walletAddress)

        // Then
        val state = contactViewModel.contactState.value
        assertEquals(error, state.error)
    }

    @Test
    fun `deleteContact with valid ID should succeed`() = runTest {
        // Given
        val contactId = ContactId.of("contact_123")
        
        every { contactDomainService.deleteContact(contactId) } returns Result.success(Unit)

        // When
        contactViewModel.deleteContact(contactId)

        // Then
        verify { contactDomainService.deleteContact(contactId) }
    }

    @Test
    fun `deleteContact with error should update error state`() = runTest {
        // Given
        val contactId = ContactId.of("contact_123")
        val error = DomainError.NotFound("Contact not found")
        
        every { contactDomainService.deleteContact(contactId) } returns Result.failure(error)

        // When
        contactViewModel.deleteContact(contactId)

        // Then
        val state = contactViewModel.contactState.value
        assertEquals(error, state.error)
    }

    @Test
    fun `searchContacts with valid query should update state`() = runTest {
        // Given
        val userId = UserId.of("user_123")
        val query = "John"
        val contacts = listOf(createTestContact())
        
        every { contactDomainService.searchContacts(userId, query) } returns Result.success(contacts)

        // When
        contactViewModel.searchContacts(userId, query)

        // Then
        val state = contactViewModel.contactState.value
        assertEquals(1, state.contacts.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `searchContacts with error should update error state`() = runTest {
        // Given
        val userId = UserId.of("user_123")
        val query = "John"
        val error = DomainError.NetworkError("Network error")
        
        every { contactDomainService.searchContacts(userId, query) } returns Result.failure(error)

        // When
        contactViewModel.searchContacts(userId, query)

        // Then
        val state = contactViewModel.contactState.value
        assertEquals(error, state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        // Given
        val userId = UserId.of("user_123")
        val error = DomainError.NetworkError("Network error")
        
        every { contactDomainService.getUserContacts(userId) } returns Result.failure(error)
        contactViewModel.loadContacts(userId)

        // Verify error is set
        assertNotNull(contactViewModel.contactState.value.error)

        // When
        contactViewModel.clearError()

        // Then
        assertNull(contactViewModel.contactState.value.error)
    }

    @Test
    fun `loadContacts should set loading state correctly`() = runTest {
        // Given
        val userId = UserId.of("user_123")
        
        every { contactDomainService.getUserContacts(userId) } returns Result.success(emptyList())

        // When
        contactViewModel.loadContacts(userId)

        // Then
        val state = contactViewModel.contactState.value
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadContacts with exception should handle error`() = runTest {
        // Given
        val userId = UserId.of("user_123")
        
        every { contactDomainService.getUserContacts(userId) } throws Exception("Network error")

        // When
        contactViewModel.loadContacts(userId)

        // Then
        val state = contactViewModel.contactState.value
        assertTrue(state.error is DomainError.NetworkError)
        assertFalse(state.isLoading)
    }

    private fun createTestContact(): Contact {
        val id = ContactId.of("contact_123")
        val userId = UserId.of("user_123")
        val name = "John Doe"
        val email = Email.of("john@example.com")
        val phoneNumber = "+1234567890"
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val createdAt = java.time.LocalDateTime.now()
        val updatedAt = java.time.LocalDateTime.now()

        return Contact.restore(id, userId, name, email, phoneNumber, walletAddress, createdAt, updatedAt).data
    }
}
