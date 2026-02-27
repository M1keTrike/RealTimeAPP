package com.duelmath.features.matchmaking.domain.usecases

import com.duelmath.features.matchmaking.domain.repositories.MatchmakingRepository
import javax.inject.Inject

class CancelMatchUseCase @Inject constructor(
    private val repository: MatchmakingRepository
) {
    suspend operator fun invoke(sessionId: String): Result<Boolean> {
        if (sessionId.isBlank()) return Result.failure(Exception("ID de sala inválido"))
        return repository.cancelMatch(sessionId)
    }
}