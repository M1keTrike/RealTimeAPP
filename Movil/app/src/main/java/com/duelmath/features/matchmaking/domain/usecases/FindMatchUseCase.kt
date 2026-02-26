package com.duelmath.features.matchmaking.domain.usecases

import com.duelmath.features.matchmaking.domain.entities.GameSession
import com.duelmath.features.matchmaking.domain.repositories.MatchmakingRepository
import javax.inject.Inject

class FindMatchUseCase @Inject constructor(
    private val repository: MatchmakingRepository
) {
    suspend operator fun invoke(userId: String): Result<GameSession> {
        if (userId.isBlank()) return Result.failure(Exception("ID de usuario no válido"))
        return repository.findMatch(userId)
    }
}