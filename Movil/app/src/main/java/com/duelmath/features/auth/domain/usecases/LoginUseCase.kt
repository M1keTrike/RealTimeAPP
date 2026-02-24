package com.duelmath.features.auth.domain.usecases

import com.duelmath.features.auth.domain.entities.AuthResult
import com.duelmath.features.auth.domain.entities.User
import com.duelmath.features.auth.domain.repositories.AuthRepository
import jakarta.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
)   {
    suspend operator fun invoke(email: String, password: String): Result<AuthResult> {
        return authRepository.login(email, password)
    }
}