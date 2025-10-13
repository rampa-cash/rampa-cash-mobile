package com.example.rampacashmobile.domain.valueobjects

/**
 * Email value object representing a valid email address
 * 
 * This value object enforces business rules:
 * - Email must be in valid format
 * - Email cannot be empty or null
 * - Provides safe operations for email handling
 */
@JvmInline
value class Email private constructor(
    val value: String
) {
    init {
        require(value.isNotBlank()) { "Email cannot be blank" }
        require(isValidEmail(value)) { "Invalid email format" }
    }

    companion object {
        fun of(email: String): Email {
            return Email(email.trim().lowercase())
        }

        private fun isValidEmail(email: String): Boolean {
            val emailRegex = Regex(
                "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
            )
            return emailRegex.matches(email)
        }
    }

    fun getDomain(): String {
        return value.substringAfter("@")
    }

    fun getLocalPart(): String {
        return value.substringBefore("@")
    }

    fun toDisplayFormat(): String {
        return value
    }

    override fun toString(): String = value
}

/**
 * Extension function to convert String to Email
 */
fun String.toEmail(): Email = Email.of(this)
