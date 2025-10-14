package com.example.rampacashmobile.data.api.model

import kotlinx.serialization.Serializable

/**
 * Add contact request
 */
@Serializable
data class AddContactRequest(
    val email: String? = null,
    val phone: String? = null,
    val displayName: String
)

/**
 * Update contact request
 */
@Serializable
data class UpdateContactRequest(
    val displayName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val walletAddress: String? = null
)

/**
 * Contact response
 */
@Serializable
data class ContactResponse(
    val id: String,
    val contactUserId: String? = null,
    val displayName: String,
    val email: String? = null,
    val phone: String? = null,
    val walletAddress: String? = null,
    val isAppUser: Boolean,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Contact statistics response
 */
@Serializable
data class ContactStatsResponse(
    val totalContacts: Int,
    val appUserContacts: Int,
    val nonAppUserContacts: Int
)

/**
 * Contact sync response
 */
@Serializable
data class ContactSyncResponse(
    val message: String,
    val syncedCount: Int,
    val syncedContacts: List<SyncedContactResponse>
)

/**
 * Synced contact response
 */
@Serializable
data class SyncedContactResponse(
    val id: String,
    val displayName: String,
    val isAppUser: Boolean,
    val walletAddress: String? = null
)
