package com.thefelineco.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.thefelineco.data.local.entity.CreditTransactionEntity
import com.thefelineco.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeById(id: Long): Flow<UserEntity?>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    /** Adds [delta] (which may be negative) to the user's balance. */
    @Query("UPDATE users SET credits = credits + :delta WHERE id = :id")
    suspend fun adjustCredits(id: Long, delta: Int)

    @Insert
    suspend fun insertTransaction(transaction: CreditTransactionEntity)

    @Query("SELECT * FROM credit_transactions WHERE userId = :userId ORDER BY createdAt DESC, id DESC")
    fun observeTransactions(userId: Long): Flow<List<CreditTransactionEntity>>
}
