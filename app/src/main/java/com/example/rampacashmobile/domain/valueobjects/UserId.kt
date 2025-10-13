package com.example.rampacashmobile.domain.valueobjects

import java.util.UUID

/**
 * UserId value object representing a unique user identifier
 * 
 * This value object enforces business rules:
 * - ID must be a valid UUID format
 * - ID cannot be empty or null
 * - Provides safe operations for user identification
 */
@JvmInline
value class UserId private constructor(
    val value: String
) {
    init {
        require(value.isNotBlank()) { "User ID cannot be blank" }
        require(isValidUuid(value)) { "Invalid UUID format for user ID" }
    }

    companion object {
        fun of(id: String): UserId {
            return UserId(id.trim())
        }

        fun generate(): UserId {
            return UserId(UUID.randomUUID().toString())
        }

        fun of(uuid: UUID): UserId {
            return UserId(uuid.toString())
        }

        private fun isValidUuid(value: String): Boolean {
            return try {
                UUID.fromString(value)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }
    }

    fun toUuid(): UUID {
        return try {
            UUID.fromString(value)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid UUID: $value", e)
        }
    }

    fun toShortFormat(): String {
        return value.take(8)
    }

    override fun toString(): String = value
}

/**
 * Extension function to convert String to UserId
 */
fun String.toUserId(): UserId = UserId.of(this)

/**
 * Extension function to convert UUID to UserId
 */
fun UUID.toUserId(): UserId = UserId.of(this)
