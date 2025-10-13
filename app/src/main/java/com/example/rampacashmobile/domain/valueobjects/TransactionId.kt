package com.example.rampacashmobile.domain.valueobjects

import java.util.UUID

/**
 * TransactionId value object representing a unique transaction identifier
 * 
 * This value object enforces business rules:
 * - ID must be a valid UUID format
 * - ID cannot be empty or null
 * - Provides safe operations for transaction identification
 */
@JvmInline
value class TransactionId private constructor(
    val value: String
) {
    init {
        require(value.isNotBlank()) { "Transaction ID cannot be blank" }
        require(isValidUuid(value)) { "Invalid UUID format for transaction ID" }
    }

    companion object {
        fun of(id: String): TransactionId {
            return TransactionId(id.trim())
        }

        fun generate(): TransactionId {
            return TransactionId(UUID.randomUUID().toString())
        }

        fun of(uuid: UUID): TransactionId {
            return TransactionId(uuid.toString())
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
 * Extension function to convert String to TransactionId
 */
fun String.toTransactionId(): TransactionId = TransactionId.of(this)

/**
 * Extension function to convert UUID to TransactionId
 */
fun UUID.toTransactionId(): TransactionId = TransactionId.of(this)
