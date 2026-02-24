package com.duelmath.features.auth.data.datasources.local

interface AuthLocalDataSource {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearSession()
}