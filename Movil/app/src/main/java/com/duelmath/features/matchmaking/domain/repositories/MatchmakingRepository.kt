package com.duelmath.features.matchmaking.domain.repositories

import com.duelmath.features.matchmaking.domain.entities.GameSession

interface MatchmakingRepository {
    suspend fun findMatch(userId: String): Result<GameSession>
    suspend fun cancelMatch(sessionId: String): Result<Boolean>
}