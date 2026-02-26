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
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val USER_ROLE = stringPreferencesKey("user_role")
    }

    override suspend fun saveToken(token: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.JWT_TOKEN] = token }
    }

    override suspend fun getToken(): String? {
        val preferences = dataStore.data.first()
        return preferences[PreferencesKeys.JWT_TOKEN]
    }

    override suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.USER_ID] = userId }
    }

    override suspend fun getUserId(): String? {
        val preferences = dataStore.data.first()
        return preferences[PreferencesKeys.USER_ID]
    }

    override suspend fun saveUsername(username: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.USERNAME] = username }
    }

    override suspend fun getUsername(): String? {
        val preferences = dataStore.data.first()
        return preferences[PreferencesKeys.USERNAME]
    }

    override suspend fun saveUserRole(role: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.USER_ROLE] = role }
    }

    override suspend fun getUserRole(): String? {
        val preferences = dataStore.data.first()
        return preferences[PreferencesKeys.USER_ROLE]
    }

    override suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.JWT_TOKEN)
            preferences.remove(PreferencesKeys.USER_ID)
            preferences.remove(PreferencesKeys.USERNAME)
            preferences.remove(PreferencesKeys.USER_ROLE)
        }
    }
}