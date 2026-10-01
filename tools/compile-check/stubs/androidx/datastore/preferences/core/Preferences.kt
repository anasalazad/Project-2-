@file:Suppress("unused", "UNUSED_PARAMETER")
package androidx.datastore.preferences.core
import androidx.datastore.core.DataStore
abstract class Preferences {
    class Key<T>(val name: String)
    abstract operator fun <T> get(key: Key<T>): T?
}
abstract class MutablePreferences : Preferences() {
    abstract operator fun <T> set(key: Key<T>, value: T)
    abstract fun <T> remove(key: Key<T>): T
}
suspend fun DataStore<Preferences>.edit(transform: suspend (MutablePreferences) -> Unit): Preferences = TODO()
fun longPreferencesKey(name: String): Preferences.Key<Long> = Preferences.Key(name)
fun booleanPreferencesKey(name: String): Preferences.Key<Boolean> = Preferences.Key(name)
