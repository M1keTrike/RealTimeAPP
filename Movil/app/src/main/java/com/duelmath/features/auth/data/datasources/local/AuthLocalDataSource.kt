package com.duelmath.features.auth.data.datasources.local

interface AuthLocalDataSource {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun saveUserId(userId: String)
    suspend fun getUserId(): String?

    suspend fun saveUsername(username: String)
    suspend fun getUsername(): String?
    suspend fun saveUserRole(role: String)
    suspend fun getUserRole(): String?
    suspend fun clearSession()
}