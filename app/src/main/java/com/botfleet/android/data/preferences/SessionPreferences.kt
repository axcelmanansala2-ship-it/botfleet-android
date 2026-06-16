package com.botfleet.android.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "botfleet_prefs")

@Singleton
class SessionPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val SESSION_TOKEN = stringPreferencesKey("session_token")
        val SERVER_URL = stringPreferencesKey("server_url")
    }

    val sessionToken: Flow<String?> = context.dataStore.data.map { it[SESSION_TOKEN] }
    val serverUrl: Flow<String> = context.dataStore.data.map {
        it[SERVER_URL] ?: "https://your-botfleet-server.replit.app"
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[SESSION_TOKEN] = token }
    }

    suspend fun clearToken() {
        context.dataStore.edit { it.remove(SESSION_TOKEN) }
    }

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { it[SERVER_URL] = url.trimEnd('/') }
    }
}
