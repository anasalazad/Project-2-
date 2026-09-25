package com.thefelineco.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

/** Remembers who is signed in between app launches. */
interface SessionStore {
    /** The signed-in user's id, or null when signed out. */
    val userId: Flow<Long?>
    suspend fun signIn(userId: Long)
    suspend fun signOut()
}

class DataStoreSessionStore(context: Context) : SessionStore {
    private val store = context.applicationContext.sessionDataStore

    override val userId: Flow<Long?> = store.data.map { it[USER_ID] }

    override suspend fun signIn(userId: Long) {
        store.edit { it[USER_ID] = userId }
    }

    override suspend fun signOut() {
        store.edit { it.remove(USER_ID) }
    }

    private companion object {
        val USER_ID = longPreferencesKey("user_id")
    }
}
