package com.duelmath.features.auth.data.datasources.remote.mapper

import com.duelmath.features.auth.data.datasources.remote.model.TokenResponse
import com.duelmath.features.auth.data.datasources.remote.model.UserResponse
import com.duelmath.features.auth.domain.entities.AuthResult
import com.duelmath.features.auth.domain.entities.User
import com.duelmath.features.auth.domain.entities.UserRole
import java.util.Date

fun TokenResponse.toDomain(): AuthResult {
    val user = User(
        id = this.id,
        username = this.username,
        email = this.email,
        eloRating = this.eloRating,
        role = this.role,
        createdAt = this.createdAt
    )

    return AuthResult(
        user = user,
        accessToken = this.accessToken,
        tokenType = this.tokenType
    )

}

fun UserResponse.toDomain(): User {
    return User(
        id = this.id,
        username = this.username,
        email = this.email,
        eloRating = this.eloRating,
        role = UserRole.PLAYER,
        createdAt = Date()
    )
}