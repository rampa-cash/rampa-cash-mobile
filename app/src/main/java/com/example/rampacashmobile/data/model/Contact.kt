package com.example.rampacashmobile.data.model

import kotlinx.serialization.Serializable

/**
 * Contact data model matching backend Contact entity
 * Represents a user's contact list for easy sending
 */
@Serializable
data class Contact(
    val id: String? = null, // UUID from backend
    val ownerId: String = "", // Foreign Key to User
    val contactUserId: String? = null, // Foreign Key to User, if contact is app user
    val email: String? = null, // For non-app users
    val phone: String? = null, // For non-app users
    val displayName: String = "",
    val walletAddress: String? = null, // If contact has wallet
    val isAppUser: Boolean = false,
    val createdAt: String? = null, // ISO timestamp from backend
    val updatedAt: String? = null // ISO timestamp from backend
) {
    val contactIdentifier: String
        get() = when {
            isAppUser && contactUserId != null -> contactUserId
            email?.isNotBlank() == true -> email
            phone?.isNotBlank() == true -> phone
            else -> displayName
        }

    val hasWallet: Boolean
        get() = walletAddress?.isNotBlank() == true

    val initials: String
        get() = displayName.split(" ")
            .take(2)
            .joinToString("") { it.firstOrNull()?.uppercaseChar()?.toString() ?: "" }
            .take(2)

    companion object {
        fun createAppUserContact(
            ownerId: String,
            contactUserId: String,
            displayName: String,
            walletAddress: String? = null
        ): Contact = Contact(
            ownerId = ownerId,
            contactUserId = contactUserId,
            displayName = displayName,
            walletAddress = walletAddress,
            isAppUser = true
        )

        fun createExternalContact(
            ownerId: String,
            displayName: String,
            email: String? = null,
            phone: String? = null,
            walletAddress: String? = null
        ): Contact = Contact(
            ownerId = ownerId,
            displayName = displayName,
            email = email,
            phone = phone,
            walletAddress = walletAddress,
            isAppUser = false
        )
    }
}
