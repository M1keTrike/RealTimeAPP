package com.duelmath.features.matchmaking.data.datasources.remote.model

data class MatchmakeRequest(
    val userId: String
)

data class GameSessionResponse(
    val id: String,
    val user1Id: String,
    val user2Id: String?,
    val winnerId: String?,
    val status: String,
    val createdAt: String
)