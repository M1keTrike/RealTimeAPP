package com.duelmath.features.matchmaking.domain.entities

enum class GameSessionStatus {
    WAITING, IN_PROGRESS, FINISHED, UNKNOWN
}

data class GameSession(
    val id: String,
    val user1Id: String,
    val user2Id: String?,
    val winnerId: String?,
    val status: GameSessionStatus
)