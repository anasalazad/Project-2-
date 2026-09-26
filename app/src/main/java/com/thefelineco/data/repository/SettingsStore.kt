package com.thefelineco.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Device-wide display preferences (not tied to an account). */
interface SettingsStore {
    /** True for the dark theme. The brand is dark-first, so this defaults to true. */
    val darkTheme: Flow<Boolean>
    suspend fun setDarkTheme(enabled: Boolean)
}

class DataStoreSettingsStore(context: Context) : SettingsStore {
    private val store = context.applicationContext.settingsDataStore

    override val darkTheme: Flow<Boolean> = store.data.map { it[DARK_THEME] ?: true }

    override suspend fun setDarkTheme(enabled: Boolean) {
        store.edit { it[DARK_THEME] = enabled }
    }

    private companion object {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }
}
