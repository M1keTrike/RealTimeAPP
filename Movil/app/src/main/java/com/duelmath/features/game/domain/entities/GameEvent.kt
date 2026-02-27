package com.duelmath.features.game.domain.entities

sealed class GameEvent {
    data class Authenticated(val userId: String, val username: String) : GameEvent()
    data object Waiting : GameEvent()
    data class GameStarted(
        val sessionId: String,
        val opponentUserId: String,
        val opponentUsername: String,
        val totalRounds: Int
    ) : GameEvent()
    data class RoundStarted(
        val roundNumber: Int,
        val question: GameQuestion,
        val timeLimitSeconds: Int
    ) : GameEvent()
    data class RoundEnded(val result: RoundResult) : GameEvent()
    data class GameOver(
        val winnerId: String?,
        val reason: String,
        val scores: Map<String, Int>,
        val eloChanges: Map<String, Int> = emptyMap()
    ) : GameEvent()
    data class Error(val code: String, val message: String) : GameEvent()
    data object Disconnected : GameEvent()
}
