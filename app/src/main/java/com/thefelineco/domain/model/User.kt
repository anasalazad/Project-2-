package com.thefelineco.domain.model

/** A signed-in account. Password data never leaves the data layer. */
data class User(
    val id: Long,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val credits: Int,
) {
    val isAdmin: Boolean get() = role == UserRole.ADMIN

    /** First name, used for friendly greetings. */
    val firstName: String get() = fullName.substringBefore(' ')
}

/** One entry in the wallet history. [amount] is positive for credits in, negative for credits out. */
data class CreditTransaction(
    val id: Long,
    val userId: Long,
    val amount: Int,
    val type: TransactionType,
    val description: String,
    val createdAt: Long,
)
