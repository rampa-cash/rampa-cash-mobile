package com.example.rampacashmobile.domain.valueobjects

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Money value object representing a monetary amount with currency
 * 
 * This value object enforces business rules:
 * - Amount cannot be negative
 * - Currency must be valid
 * - Operations between different currencies are not allowed
 */
data class Money(
    val amount: BigDecimal,
    val currency: Currency
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Amount cannot be negative" }
        require(amount.scale() <= currency.decimalPlaces) { 
            "Amount precision exceeds currency decimal places" 
        }
    }

    companion object {
        fun of(amount: BigDecimal, currency: Currency): Money {
            return Money(amount.setScale(currency.decimalPlaces, RoundingMode.HALF_UP), currency)
        }

        fun of(amount: Double, currency: Currency): Money {
            return of(BigDecimal.valueOf(amount), currency)
        }

        fun of(amount: String, currency: Currency): Money {
            return of(BigDecimal(amount), currency)
        }

        val ZERO_USD = of(BigDecimal.ZERO, Currency.USD)
        val ZERO_EUR = of(BigDecimal.ZERO, Currency.EUR)
        val ZERO_SOL = of(BigDecimal.ZERO, Currency.SOL)
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Cannot add different currencies" }
        return of(amount + other.amount, currency)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "Cannot subtract different currencies" }
        val result = amount - other.amount
        require(result >= BigDecimal.ZERO) { "Result cannot be negative" }
        return of(result, currency)
    }

    operator fun times(multiplier: BigDecimal): Money {
        return of(amount * multiplier, currency)
    }

    operator fun times(multiplier: Double): Money {
        return times(BigDecimal.valueOf(multiplier))
    }

    fun isGreaterThan(other: Money): Boolean {
        require(currency == other.currency) { "Cannot compare different currencies" }
        return amount > other.amount
    }

    fun isGreaterThanOrEqual(other: Money): Boolean {
        require(currency == other.currency) { "Cannot compare different currencies" }
        return amount >= other.amount
    }

    fun isLessThan(other: Money): Boolean {
        require(currency == other.currency) { "Cannot compare different currencies" }
        return amount < other.amount
    }

    fun isLessThanOrEqual(other: Money): Boolean {
        require(currency == other.currency) { "Cannot compare different currencies" }
        return amount <= other.amount
    }

    fun isZero(): Boolean = amount == BigDecimal.ZERO

    fun isPositive(): Boolean = amount > BigDecimal.ZERO

    fun format(): String = "${currency.symbol}${amount.setScale(currency.decimalPlaces, RoundingMode.HALF_UP)}"

    override fun toString(): String = format()
}

/**
 * Currency enumeration with decimal places and symbol information
 */
enum class Currency(val code: String, val symbol: String, val decimalPlaces: Int) {
    USD("USD", "$", 2),
    EUR("EUR", "€", 2),
    SOL("SOL", "◎", 9),
    USDC("USDC", "$", 6),
    EURC("EURC", "€", 6);

    companion object {
        fun fromCode(code: String): Currency? = values().find { it.code == code }
    }
}
