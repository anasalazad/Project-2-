package com.thefelineco.data.local

import com.thefelineco.data.local.entity.CreditTransactionEntity
import com.thefelineco.domain.model.TransactionType

/**
 * Changes a user's balance and writes the matching history row. Every credit change goes through
 * here, so the balance and the wallet history always agree. Call it inside a transaction.
 */
suspend fun FelineDatabase.recordCredits(
    userId: Long,
    amount: Int,
    type: TransactionType,
    description: String,
) {
    if (amount == 0) return
    userDao().adjustCredits(userId, amount)
    userDao().insertTransaction(
        CreditTransactionEntity(userId = userId, amount = amount, type = type, description = description)
    )
}
