package com.example.rampacashmobile.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.math.BigDecimal

/**
 * VISA Card data model matching backend VISACard entity
 * Represents physical or virtual card linked to user's crypto balance
 */
@Serializable
data class VISACard(
    val id: String? = null, // UUID from backend
    val userId: String = "", // Foreign Key to User
    val cardNumber: String = "", // Masked for security
    val cardType: CardType = CardType.VIRTUAL,
    val status: CardStatus = CardStatus.PENDING,
    @Contextual val balance: BigDecimal = BigDecimal.ZERO, // Current available balance
    @Contextual val dailyLimit: BigDecimal = BigDecimal.ZERO, // Daily spending limit
    @Contextual val monthlyLimit: BigDecimal = BigDecimal.ZERO, // Monthly spending limit
    val createdAt: String? = null, // ISO timestamp from backend
    val activatedAt: String? = null, // ISO timestamp from backend
    val expiresAt: String? = null // ISO timestamp from backend
) {
    val maskedCardNumber: String
        get() = if (cardNumber.length >= 4) {
            "**** **** **** ${cardNumber.takeLast(4)}"
        } else {
            "**** **** **** ****"
        }

    val isActive: Boolean
        get() = status == CardStatus.ACTIVE

    val isExpired: Boolean
        get() = expiresAt?.let { 
            try {
                java.time.Instant.parse(it).isBefore(java.time.Instant.now())
            } catch (e: Exception) {
                false
            }
        } ?: false

    val canSpend: Boolean
        get() = isActive && !isExpired && balance > BigDecimal.ZERO

    val remainingDailyLimit: BigDecimal
        get() = dailyLimit - getTodaySpent()

    val remainingMonthlyLimit: BigDecimal
        get() = monthlyLimit - getThisMonthSpent()

    private fun getTodaySpent(): BigDecimal {
        // TODO: Implement actual spending calculation from transactions
        return BigDecimal.ZERO
    }

    private fun getThisMonthSpent(): BigDecimal {
        // TODO: Implement actual spending calculation from transactions
        return BigDecimal.ZERO
    }

    fun canSpendAmount(amount: BigDecimal): Boolean {
        return canSpend && 
               amount <= balance && 
               amount <= remainingDailyLimit && 
               amount <= remainingMonthlyLimit
    }
}

@Serializable
enum class CardType(val value: String) {
    PHYSICAL("physical"),
    VIRTUAL("virtual");

    companion object {
        fun fromValue(value: String): CardType = values().find { it.value == value } ?: VIRTUAL
    }
}

@Serializable
enum class CardStatus(val value: String) {
    PENDING("pending"),
    ACTIVE("active"),
    SUSPENDED("suspended"),
    CANCELLED("cancelled");

    companion object {
        fun fromValue(value: String): CardStatus = values().find { it.value == value } ?: PENDING
    }
}
