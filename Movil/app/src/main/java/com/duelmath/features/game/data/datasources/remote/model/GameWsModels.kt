package com.duelmath.features.game.data.datasources.remote.model

data class WsOption(val id: String, val text: String)

sealed class GameWsMessage {
    data class Authenticated(val userId: String, val username: String) : GameWsMessage()
    data object Waiting : GameWsMessage()
    data class GameStarted(
        val sessionId: String,
        val opponentUserId: String,
        val opponentUsername: String,
        val totalRounds: Int
    ) : GameWsMessage()
    data class RoundStarted(
        val roundNumber: Int,
        val questionId: String,
        val statement: String,
        val difficulty: String,
        val options: List<WsOption>,
        val timeLimitSeconds: Int
    ) : GameWsMessage()
    data class RoundResult(
        val roundNumber: Int,
        val winnerId: String?,
        val correctOptionId: String,
        val scores: Map<String, Int>
    ) : GameWsMessage()
    data class GameOver(
        val winnerId: String?,
        val reason: String,
        val scores: Map<String, Int>
    ) : GameWsMessage()
    data class Error(val code: String, val message: String) : GameWsMessage()
    data object Pong : GameWsMessage()
    data object Unknown : GameWsMessage()
}
