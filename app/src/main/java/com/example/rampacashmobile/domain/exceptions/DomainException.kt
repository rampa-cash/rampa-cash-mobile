package com.example.rampacashmobile.domain.exceptions

/**
 * Base domain exception class
 * 
 * All domain-specific exceptions should extend this class
 */
abstract class DomainException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * User-related domain exceptions
 */
class UserCannotChangeEmailException(
    message: String = "User cannot change email at this time"
) : DomainException(message)

class UserCannotActivateException(
    message: String = "User cannot be activated"
) : DomainException(message)

class UserCannotSuspendException(
    message: String = "User cannot be suspended"
) : DomainException(message)

class UserCannotDeactivateException(
    message: String = "User cannot be deactivated"
) : DomainException(message)

class UserCannotUpdatePersonalInfoException(
    message: String = "User cannot update personal information"
) : DomainException(message)

class UserCannotChangePhoneNumberException(
    message: String = "User cannot change phone number"
) : DomainException(message)

class UserCannotSetWalletAddressException(
    message: String = "User cannot set wallet address"
) : DomainException(message)

class UserCannotVerifyEmailException(
    message: String = "User cannot verify email"
) : DomainException(message)

class UserCannotVerifyPhoneException(
    message: String = "User cannot verify phone"
) : DomainException(message)

class UserCannotUpdateProfileImageException(
    message: String = "User cannot update profile image"
) : DomainException(message)

class UserCannotChangeLanguageException(
    message: String = "User cannot change language"
) : DomainException(message)

/**
 * Wallet-related domain exceptions
 */
class WalletCannotUpdateLabelException(
    message: String = "Wallet cannot update label"
) : DomainException(message)

class WalletCannotActivateException(
    message: String = "Wallet cannot be activated"
) : DomainException(message)

class WalletCannotDeactivateException(
    message: String = "Wallet cannot be deactivated"
) : DomainException(message)

/**
 * Transaction-related domain exceptions
 */
class TransactionCannotConfirmException(
    message: String = "Transaction cannot be confirmed"
) : DomainException(message)

class TransactionCannotCompleteException(
    message: String = "Transaction cannot be completed"
) : DomainException(message)

class TransactionCannotFailException(
    message: String = "Transaction cannot be marked as failed"
) : DomainException(message)

class TransactionCannotCancelException(
    message: String = "Transaction cannot be cancelled"
) : DomainException(message)

class TransactionCannotUpdateDescriptionException(
    message: String = "Transaction description cannot be updated"
) : DomainException(message)

class InsufficientFundsException(
    message: String = "Insufficient funds for transaction"
) : DomainException(message)

class CannotSendToSelfException(
    message: String = "Cannot send money to yourself"
) : DomainException(message)

class ZeroAmountException(
    message: String = "Transaction amount cannot be zero"
) : DomainException(message)

class InvalidTransactionAmountException(
    message: String = "Invalid transaction amount"
) : DomainException(message)

class TransactionLimitExceededException(
    message: String = "Transaction limit exceeded"
) : DomainException(message)

/**
 * Contact-related domain exceptions
 */
class ContactCannotUpdateNameException(
    message: String = "Contact name cannot be updated"
) : DomainException(message)

class ContactCannotUpdateEmailException(
    message: String = "Contact email cannot be updated"
) : DomainException(message)

class ContactCannotUpdatePhoneNumberException(
    message: String = "Contact phone number cannot be updated"
) : DomainException(message)

class ContactCannotUpdateWalletAddressException(
    message: String = "Contact wallet address cannot be updated"
) : DomainException(message)

class ContactCannotAddToFavoritesException(
    message: String = "Contact cannot be added to favorites"
) : DomainException(message)

class ContactCannotRemoveFromFavoritesException(
    message: String = "Contact cannot be removed from favorites"
) : DomainException(message)

class ContactNotFoundException(
    message: String = "Contact not found"
) : DomainException(message)

class ContactAlreadyExistsException(
    message: String = "Contact already exists"
) : DomainException(message)

/**
 * General domain exceptions
 */
class InvalidInputException(
    message: String = "Invalid input provided"
) : DomainException(message)

class OperationNotAllowedException(
    message: String = "Operation not allowed"
) : DomainException(message)

class ResourceNotFoundException(
    message: String = "Resource not found"
) : DomainException(message)

class ResourceAlreadyExistsException(
    message: String = "Resource already exists"
) : DomainException(message)

class BusinessRuleViolationException(
    message: String = "Business rule violation"
) : DomainException(message)
