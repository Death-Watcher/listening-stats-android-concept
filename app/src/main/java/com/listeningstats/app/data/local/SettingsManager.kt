package com.listeningstats.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        private val KEY_LASTFM_USER = stringPreferencesKey("lastfm_user")
        private val KEY_LASTFM_API_KEY = stringPreferencesKey("lastfm_api_key")
        private val KEY_STATSFM_USER = stringPreferencesKey("statsfm_user")
    }

    val lastFmUser: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LASTFM_USER] ?: ""
    }

    val lastFmApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LASTFM_API_KEY] ?: ""
    }

    val statsFmUser: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_STATSFM_USER] ?: ""
    }

    suspend fun setLastFmUser(user: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LASTFM_USER] = user
        }
    }

    suspend fun setLastFmApiKey(apiKey: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LASTFM_API_KEY] = apiKey
        }
    }

    suspend fun setStatsFmUser(user: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_STATSFM_USER] = user
        }
    }
}
