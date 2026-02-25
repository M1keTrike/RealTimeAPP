package com.duelmath.features.auth.domain.entities

import java.util.Date

enum class UserRole(val value: String) {
    PLAYER("PLAYER"),
    ADMIN("ADMIN")
}

data class User(
    val id: String,
    val username: String,
    val email: String,
    val eloRating: Int,
    val role: UserRole,
    val createdAt: Date
)

data class AuthResult(
    val user: User,
    val accessToken: String,
    val tokenType: String
)