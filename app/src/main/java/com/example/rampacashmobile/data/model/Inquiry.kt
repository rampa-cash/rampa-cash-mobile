package com.example.rampacashmobile.data.model

import kotlinx.serialization.Serializable

/**
 * Inquiry data model matching backend Inquiry entity
 * Represents user inquiries and waitlist registrations
 */
@Serializable
data class Inquiry(
    val id: Int? = null, // SERIAL from backend
    val name: String = "",
    val email: String = "",
    val inquiry: String? = null, // Optional user inquiry text
    val type: InquiryType = InquiryType.WAITLIST,
    val createdAt: String? = null, // ISO timestamp from backend
    val updatedAt: String? = null // ISO timestamp from backend
) {
    val isWaitlist: Boolean
        get() = type == InquiryType.WAITLIST

    val isGeneral: Boolean
        get() = type == InquiryType.GENERAL

    val hasInquiryText: Boolean
        get() = inquiry?.isNotBlank() == true

    val displayName: String
        get() = name.ifBlank { email }

    val initials: String
        get() = name.split(" ")
            .take(2)
            .joinToString("") { it.firstOrNull()?.uppercaseChar()?.toString() ?: "" }
            .take(2)

    companion object {
        fun createWaitlistRegistration(
            name: String,
            email: String
        ): Inquiry = Inquiry(
            name = name,
            email = email,
            type = InquiryType.WAITLIST
        )

        fun createGeneralInquiry(
            name: String,
            email: String,
            inquiry: String
        ): Inquiry = Inquiry(
            name = name,
            email = email,
            inquiry = inquiry,
            type = InquiryType.GENERAL
        )
    }
}

@Serializable
enum class InquiryType(val value: String) {
    WAITLIST("WAITLIST"),
    GENERAL("GENERAL");

    companion object {
        fun fromValue(value: String): InquiryType = values().find { it.value == value } ?: WAITLIST
    }
}
