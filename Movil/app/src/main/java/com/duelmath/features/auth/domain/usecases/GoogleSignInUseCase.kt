package com.duelmath.features.auth.domain.usecases

import com.duelmath.features.auth.domain.entities.AuthResult
import com.duelmath.features.auth.domain.repositories.AuthRepository
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<AuthResult> {
        return authRepository.googleSignIn(idToken)
    }
}
