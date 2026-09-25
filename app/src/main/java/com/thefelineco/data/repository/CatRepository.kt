package com.thefelineco.data.repository

import com.thefelineco.data.local.FelineDatabase
import com.thefelineco.data.local.entity.toDomain
import com.thefelineco.data.local.entity.toEntity
import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.Cat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Cat listings. Customers read them; admins also create, edit and delete them. */
interface CatRepository {
    fun observeCats(): Flow<List<Cat>>
    fun observeCat(id: Long): Flow<Cat?>
    suspend fun getCat(id: Long): Cat?

    /** Inserts a new cat (id = 0) or updates an existing one. Returns its id. */
    suspend fun saveCat(cat: Cat): Long
    suspend fun deleteCat(id: Long): Result<Unit>
    suspend fun setStatus(id: Long, status: AdoptionStatus)
}

class OfflineCatRepository(private val db: FelineDatabase) : CatRepository {
    private val catDao = db.catDao()

    override fun observeCats(): Flow<List<Cat>> = catDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeCat(id: Long): Flow<Cat?> = catDao.observeById(id).map { it?.toDomain() }

    override suspend fun getCat(id: Long): Cat? = catDao.getById(id)?.toDomain()

    override suspend fun saveCat(cat: Cat): Long {
        val rowId = catDao.upsert(cat.toEntity())
        // @Upsert returns -1 when it updated an existing row rather than inserting one.
        return if (rowId == -1L) cat.id else rowId
    }

    override suspend fun deleteCat(id: Long): Result<Unit> = runCatching {
        if (db.bookingDao().activeCountForCat(id) > 0) {
            throw FelineException("This cat has an active booking. Decline or complete it first.")
        }
        catDao.delete(id)
    }

    override suspend fun setStatus(id: Long, status: AdoptionStatus) = catDao.updateStatus(id, status)
}
