package com.duelmath.features.auth.data.datasources.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject
class AuthLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AuthLocalDataSource {

    private object PreferencesKeys {
        val JWT_TOKEN = stringPreferencesKey("jwt_token")
    }

    override suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.JWT_TOKEN] = token
        }
    }

    override suspend fun getToken(): String? {
        val preferences = dataStore.data.first()
        return preferences[PreferencesKeys.JWT_TOKEN]
    }

    override suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.JWT_TOKEN)
        }
    }
}