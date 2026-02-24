package com.duelmath.features.auth.domain.usecases

import com.duelmath.features.auth.domain.entities.User
import com.duelmath.features.auth.domain.repositories.AuthRepository
import jakarta.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Todos los campos son obligatorios"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }
        if (!email.contains("@")) {
            return Result.failure(Exception("Ingresa un correo válido"))
        }
        return authRepository.register( email, password)
    }
}