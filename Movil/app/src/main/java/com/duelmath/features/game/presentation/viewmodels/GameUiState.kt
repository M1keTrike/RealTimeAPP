package com.duelmath.features.game.presentation.viewmodels

import com.duelmath.features.game.domain.entities.GameQuestion

data class GameUiState(
    // Connection
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    // Waiting for opponent
    val isWaiting: Boolean = false,
    // Session info
    val sessionId: String? = null,
    val opponentUsername: String? = null,
    val totalRounds: Int = 0,
    // Round
    val currentRound: Int = 0,
    val question: GameQuestion? = null,
    val timeLimitSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    // Answer — optimistic update: selectedOptionId is set immediately on tap
    // Rollback: when round_result arrives, correctOptionId reveals the truth;
    // if selectedOptionId != correctOptionId the UI highlights the error
    val selectedOptionId: String? = null,
    val correctOptionId: String? = null,
    // Round result
    val isShowingResult: Boolean = false,
    val roundWinnerId: String? = null,
    val myScore: Int = 0,
    val opponentScore: Int = 0,
    // Game over
    val isGameOver: Boolean = false,
    val gameWinnerId: String? = null,
    val gameOverReason: String? = null,
    // ELO change after game (null = no data received yet)
    val myEloChange: Int? = null,
    val myNewElo: Int? = null
)

/** One-time side-effects emitted via SharedFlow — never stored in state. */
sealed class GameSideEffect {
    data class Error(val message: String) : GameSideEffect()
    data object RoundWon : GameSideEffect()
    data object RoundLost : GameSideEffect()
    data object GameWon : GameSideEffect()
    data object GameLost : GameSideEffect()
}
