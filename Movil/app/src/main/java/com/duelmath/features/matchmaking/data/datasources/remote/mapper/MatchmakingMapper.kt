package com.duelmath.features.matchmaking.data.datasources.remote.mapper

import com.duelmath.features.matchmaking.data.datasources.remote.model.GameSessionResponse
import com.duelmath.features.matchmaking.domain.entities.GameSession
import com.duelmath.features.matchmaking.domain.entities.GameSessionStatus

fun GameSessionResponse.toDomain(): GameSession {
    val parsedStatus = try {
        GameSessionStatus.valueOf(this.status)
    } catch (e: Exception) {
        GameSessionStatus.UNKNOWN
    }

    return GameSession(
        id = this.id,
        user1Id = this.user1Id,
        user2Id = this.user2Id,
        winnerId = this.winnerId,
        status = parsedStatus
    )
}