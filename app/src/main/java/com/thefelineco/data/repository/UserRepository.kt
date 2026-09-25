package com.thefelineco.data.repository

import androidx.room.withTransaction
import com.thefelineco.data.local.FelineDatabase
import com.thefelineco.data.local.PasswordHasher
import com.thefelineco.data.local.entity.UserEntity
import com.thefelineco.data.local.entity.toDomain
import com.thefelineco.data.local.recordCredits
import com.thefelineco.domain.CreditRules
import com.thefelineco.domain.model.CreditTransaction
import com.thefelineco.domain.model.TransactionType
import com.thefelineco.domain.model.User
import com.thefelineco.domain.model.UserRole
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** Accounts, sign-in state and the credit wallet. */
interface UserRepository {
    /** The signed-in user, updated live (for example when their credit balance changes). Null when signed out. */
    val currentUser: Flow<User?>

    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(fullName: String, email: String, password: String): Result<User>
    suspend fun logout()
    fun observeTransactions(userId: Long): Flow<List<CreditTransaction>>
}

class OfflineUserRepository(
    private val db: FelineDatabase,
    private val session: SessionStore,
) : UserRepository {

    private val userDao = db.userDao()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentUser: Flow<User?> = session.userId.flatMapLatest { id ->
        if (id == null) flowOf(null) else userDao.observeById(id).map { it?.toDomain() }
    }

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val user = userDao.findByEmail(email.trim())
            ?.takeIf { PasswordHasher.matches(password, it.salt, it.passwordHash) }
            ?: throw FelineException("Incorrect email or password")
        session.signIn(user.id)
        user.toDomain()
    }

    override suspend fun register(fullName: String, email: String, password: String): Result<User> = runCatching {
        val cleanEmail = email.trim()
        if (userDao.findByEmail(cleanEmail) != null) {
            throw FelineException("An account with this email already exists")
        }
        val salt = PasswordHasher.newSalt()
        val id = db.withTransaction {
            val newId = userDao.insert(
                UserEntity(
                    fullName = fullName.trim(),
                    email = cleanEmail,
                    passwordHash = PasswordHasher.hash(password, salt),
                    salt = salt,
                    role = UserRole.CUSTOMER,
                    credits = 0,
                )
            )
            db.recordCredits(newId, CreditRules.WELCOME_BONUS, TransactionType.WELCOME_BONUS, "Welcome to The Feline Co.")
            newId
        }
        session.signIn(id)
        checkNotNull(userDao.getById(id)).toDomain()
    }

    override suspend fun logout() = session.signOut()

    override fun observeTransactions(userId: Long): Flow<List<CreditTransaction>> =
        userDao.observeTransactions(userId).map { rows -> rows.map { it.toDomain() } }
}
