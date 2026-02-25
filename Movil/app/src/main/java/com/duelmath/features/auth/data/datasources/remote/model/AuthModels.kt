package com.duelmath.features.auth.data.datasources.remote.model

import com.duelmath.features.auth.domain.entities.UserRole
import java.util.Date

data class LoginRequest(
    val email: String,
    val password: String
)

data class TokenResponse(
    val accessToken: String,
    val id: String,
    val username: String,
    val email: String,
    val eloRating: Int,
    val role: UserRole,
    val createdAt: Date
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val eloRating: Int
)