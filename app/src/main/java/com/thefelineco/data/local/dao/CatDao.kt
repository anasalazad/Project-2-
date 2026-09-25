package com.thefelineco.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.thefelineco.data.local.entity.CatEntity
import com.thefelineco.domain.model.AdoptionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CatDao {
    @Query("SELECT * FROM cats ORDER BY listedAt DESC")
    fun observeAll(): Flow<List<CatEntity>>

    @Query("SELECT * FROM cats WHERE id = :id")
    fun observeById(id: Long): Flow<CatEntity?>

    @Query("SELECT * FROM cats WHERE id = :id")
    suspend fun getById(id: Long): CatEntity?

    @Upsert
    suspend fun upsert(cat: CatEntity): Long

    @Insert
    suspend fun insertAll(cats: List<CatEntity>)

    @Query("DELETE FROM cats WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE cats SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: AdoptionStatus)

    @Query("SELECT COUNT(*) FROM cats")
    suspend fun count(): Int
}
