@file:Suppress("unused", "UNUSED_PARAMETER")
package androidx.room
import android.content.Context
import kotlin.reflect.KClass
annotation class Entity(val tableName: String = "", val indices: Array<Index> = [], val inheritSuperIndices: Boolean = false, val primaryKeys: Array<String> = [], val foreignKeys: Array<ForeignKey> = [], val ignoredColumns: Array<String> = [])
annotation class Index(vararg val value: String, val name: String = "", val unique: Boolean = false)
annotation class ForeignKey(val entity: KClass<*>, val parentColumns: Array<String>, val childColumns: Array<String>, val onDelete: Int = NO_ACTION, val onUpdate: Int = NO_ACTION, val deferred: Boolean = false) {
    companion object { const val NO_ACTION = 1; const val CASCADE = 5 }
}
annotation class PrimaryKey(val autoGenerate: Boolean = false)
annotation class Embedded(val prefix: String = "")
annotation class Relation(val entity: KClass<*> = Any::class, val parentColumn: String, val entityColumn: String)
annotation class Dao
annotation class Insert(val onConflict: Int = OnConflictStrategy.ABORT)
object OnConflictStrategy { const val ABORT = 3; const val IGNORE = 5; const val REPLACE = 1 }
annotation class Upsert
annotation class Query(val value: String)
annotation class Transaction
annotation class TypeConverter
annotation class TypeConverters(vararg val value: KClass<*>)
annotation class Database(val entities: Array<KClass<*>> = [], val version: Int, val exportSchema: Boolean = true)
abstract class RoomDatabase {
    class Builder<T : RoomDatabase> {
        fun fallbackToDestructiveMigration(): Builder<T> = this
        fun build(): T = TODO()
    }
}
object Room {
    fun <T : RoomDatabase> databaseBuilder(context: Context, klass: Class<T>, name: String?): RoomDatabase.Builder<T> = TODO()
}
suspend fun <R> RoomDatabase.withTransaction(block: suspend () -> R): R = block()
