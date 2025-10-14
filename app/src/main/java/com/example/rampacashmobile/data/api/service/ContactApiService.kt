package com.example.rampacashmobile.data.api.service

import com.example.rampacashmobile.data.api.model.*
import retrofit2.http.*

/**
 * Contact API service
 */
interface ContactApiService {
    
    /**
     * Get user contacts
     */
    @GET("contacts")
    suspend fun getContacts(): List<ContactResponse>
    
    /**
     * Add new contact
     */
    @POST("contacts")
    suspend fun addContact(
        @Body request: AddContactRequest
    ): ContactResponse
    
    /**
     * Get app user contacts
     */
    @GET("contacts/app-users")
    suspend fun getAppUserContacts(): List<ContactResponse>
    
    /**
     * Get non-app user contacts
     */
    @GET("contacts/non-app-users")
    suspend fun getNonAppUserContacts(): List<ContactResponse>
    
    /**
     * Search contacts
     */
    @GET("contacts/search")
    suspend fun searchContacts(
        @Query("q") query: String
    ): List<ContactResponse>
    
    /**
     * Get contact statistics
     */
    @GET("contacts/stats")
    suspend fun getContactStats(): ContactStatsResponse
    
    /**
     * Sync contacts with app users
     */
    @GET("contacts/sync")
    suspend fun syncContacts(): ContactSyncResponse
    
    /**
     * Get contact by email
     */
    @GET("contacts/by-email/{email}")
    suspend fun getContactByEmail(
        @Path("email") email: String
    ): ContactResponse
    
    /**
     * Get contact by phone
     */
    @GET("contacts/by-phone/{phone}")
    suspend fun getContactByPhone(
        @Path("phone") phone: String
    ): ContactResponse
    
    /**
     * Get contact by wallet address
     */
    @GET("contacts/by-wallet/{walletAddress}")
    suspend fun getContactByWalletAddress(
        @Path("walletAddress") walletAddress: String
    ): ContactResponse
    
    /**
     * Get specific contact
     */
    @GET("contacts/{contactId}")
    suspend fun getContact(
        @Path("contactId") contactId: String
    ): ContactResponse
    
    /**
     * Update contact
     */
    @PUT("contacts/{contactId}")
    suspend fun updateContact(
        @Path("contactId") contactId: String,
        @Body request: UpdateContactRequest
    ): ContactResponse
    
    /**
     * Remove contact
     */
    @DELETE("contacts/{contactId}")
    suspend fun removeContact(
        @Path("contactId") contactId: String
    ): Map<String, String>
}
