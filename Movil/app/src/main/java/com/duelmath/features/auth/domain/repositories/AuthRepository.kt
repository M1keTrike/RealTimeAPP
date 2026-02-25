package com.duelmath.features.auth.domain.repositories

import com.duelmath.features.auth.domain.entities.AuthResult
import com.duelmath.features.auth.domain.entities.User

interface AuthRepository{
    suspend fun login(email: String, password: String): Result<AuthResult>

    suspend fun register(username: String,email: String, password: String): Result<User>


}