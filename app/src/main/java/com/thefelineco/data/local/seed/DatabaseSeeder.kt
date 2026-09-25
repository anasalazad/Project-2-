package com.thefelineco.data.local.seed

import androidx.room.withTransaction
import com.thefelineco.data.local.FelineDatabase
import com.thefelineco.data.local.PasswordHasher
import com.thefelineco.data.local.entity.CreditTransactionEntity
import com.thefelineco.data.local.entity.UserEntity
import com.thefelineco.data.local.entity.toEntity
import com.thefelineco.domain.CreditRules
import com.thefelineco.domain.model.TransactionType
import com.thefelineco.domain.model.UserRole
import java.util.concurrent.TimeUnit

/** Fills an empty database with the demo accounts, cats and products. Safe to call on every launch. */
class DatabaseSeeder(private val db: FelineDatabase) {

    suspend fun seedIfEmpty() {
        if (db.userDao().count() > 0) return
        db.withTransaction {
            seedUsers()
            val now = System.currentTimeMillis()
            // Stagger listing dates so "Newest arrivals" has a meaningful order.
            db.catDao().insertAll(
                SeedData.cats.mapIndexed { index, cat ->
                    cat.copy(listedAt = now - TimeUnit.HOURS.toMillis(index * 20L)).toEntity()
                }
            )
            db.shopDao().insertProducts(
                SeedData.products.mapIndexed { index, product -> product.toEntity(sortOrder = index) }
            )
        }
    }

    private suspend fun seedUsers() {
        val userDao = db.userDao()
        userDao.insert(account("Felicity Admin", ADMIN_EMAIL, "Admin123!", UserRole.ADMIN, credits = 0))

        val demoId = userDao.insert(
            account("Jordan Demo", DEMO_EMAIL, "Demo123!", UserRole.CUSTOMER, credits = DEMO_CREDITS)
        )
        userDao.insertTransaction(
            CreditTransactionEntity(
                userId = demoId,
                amount = CreditRules.WELCOME_BONUS,
                type = TransactionType.WELCOME_BONUS,
                description = "Welcome to The Feline Co.",
            )
        )
        userDao.insertTransaction(
            CreditTransactionEntity(
                userId = demoId,
                amount = DEMO_CREDITS - CreditRules.WELCOME_BONUS,
                type = TransactionType.ADOPTION_REWARD,
                description = "Demo bonus credits",
            )
        )
    }

    private fun account(name: String, email: String, password: String, role: UserRole, credits: Int): UserEntity {
        val salt = PasswordHasher.newSalt()
        return UserEntity(
            fullName = name,
            email = email,
            passwordHash = PasswordHasher.hash(password, salt),
            salt = salt,
            role = role,
            credits = credits,
        )
    }

    companion object {
        const val ADMIN_EMAIL = "admin@thefelineco.com"
        const val DEMO_EMAIL = "demo@thefelineco.com"
        private const val DEMO_CREDITS = 400
    }
}
