package com.duelmath.features.game.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelmath.features.auth.data.datasources.local.AuthLocalDataSource
import com.duelmath.features.game.domain.entities.GameEvent
import com.duelmath.features.game.domain.usecases.ConnectToGameUseCase
import com.duelmath.features.game.domain.usecases.DisconnectFromGameUseCase
import com.duelmath.features.game.domain.usecases.ObserveGameEventsUseCase
import com.duelmath.features.game.domain.usecases.SendAnswerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val connectToGameUseCase: ConnectToGameUseCase,
    private val observeGameEventsUseCase: ObserveGameEventsUseCase,
    private val sendAnswerUseCase: SendAnswerUseCase,
    private val disconnectFromGameUseCase: DisconnectFromGameUseCase,
    private val authLocalDataSource: AuthLocalDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // One-time events: errors, round feedback, game over — never replayed
    private val _sideEffect = MutableSharedFlow<GameSideEffect>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val sideEffect: SharedFlow<GameSideEffect> = _sideEffect.asSharedFlow()

    private var myUserId: String? = null
    private var countdownJob: Job? = null

    init {
        observeEvents()
        viewModelScope.launch { connectOnStart() }
    }

    private suspend fun connectOnStart() {
        val token = authLocalDataSource.getToken() ?: return
        myUserId = authLocalDataSource.getUserId()
        _uiState.update { it.copy(isConnecting = true) }
        connectToGameUseCase(token)
    }

    /**
     * Optimistic update: the UI immediately reflects the user's selection.
     * Rollback happens when [GameEvent.RoundEnded] arrives and
     * [RoundResult.correctOptionId] differs from the selected option —
     * the Screen highlights the correct answer and marks the selected one as wrong.
     */
    fun selectAnswer(questionId: String, optionId: String) {
        // Ignore if answer already submitted for this round or result is showing
        if (_uiState.value.selectedOptionId != null || _uiState.value.isShowingResult) return

        // Optimistic update — instantly reflect in the UI
        _uiState.update { it.copy(selectedOptionId = optionId) }

        // Send to WebSocket server
        sendAnswerUseCase(questionId, optionId)
    }

    fun disconnect() {
        countdownJob?.cancel()
        disconnectFromGameUseCase()
        _uiState.update { it.copy(isConnected = false) }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            observeGameEventsUseCase().collect { event -> handleEvent(event) }
        }
    }

    private suspend fun handleEvent(event: GameEvent) {
        when (event) {
            is GameEvent.Authenticated -> {
                _uiState.update { it.copy(isConnecting = false, isConnected = true) }
            }
            is GameEvent.Waiting -> {
                _uiState.update { it.copy(isWaiting = true) }
            }
            is GameEvent.GameStarted -> {
                _uiState.update {
                    it.copy(
                        isWaiting = false,
                        sessionId = event.sessionId,
                        opponentUsername = event.opponentUsername,
                        totalRounds = event.totalRounds
                    )
                }
            }
            is GameEvent.RoundStarted -> {
                countdownJob?.cancel()
                _uiState.update {
                    it.copy(
                        currentRound = event.roundNumber,
                        question = event.question,
                        timeLimitSeconds = event.timeLimitSeconds,
                        remainingSeconds = event.timeLimitSeconds,
                        selectedOptionId = null,   // reset optimistic selection
                        correctOptionId = null,
                        isShowingResult = false,
                        roundWinnerId = null
                    )
                }
                startCountdown(event.timeLimitSeconds)
            }
            is GameEvent.RoundEnded -> {
                countdownJob?.cancel()
                val result = event.result
                val myId = myUserId
                val myScore = if (myId != null) result.scores[myId] ?: 0 else 0
                val opponentScore = result.scores.entries
                    .firstOrNull { it.key != myId }?.value ?: 0

                // If correctOptionId != selectedOptionId → rollback: UI shows error highlight
                _uiState.update {
                    it.copy(
                        correctOptionId = result.correctOptionId,
                        roundWinnerId = result.winnerId,
                        myScore = myScore,
                        opponentScore = opponentScore,
                        isShowingResult = true
                    )
                }
                if (result.winnerId == myId) {
                    _sideEffect.emit(GameSideEffect.RoundWon)
                } else if (result.winnerId != null) {
                    _sideEffect.emit(GameSideEffect.RoundLost)
                }
            }
            is GameEvent.GameOver -> {
                countdownJob?.cancel()
                val iWon = event.winnerId == myUserId
                _uiState.update {
                    it.copy(
                        isGameOver = true,
                        gameWinnerId = event.winnerId,
                        gameOverReason = event.reason
                    )
                }
                if (iWon) {
                    _sideEffect.emit(GameSideEffect.GameWon)
                } else if (event.winnerId != null) {
                    _sideEffect.emit(GameSideEffect.GameLost)
                }
            }
            is GameEvent.Error -> {
                _uiState.update {
                    it.copy(
                        isConnecting = false,
                        isWaiting = false,
                        isConnected = false
                    )
                }
                _sideEffect.emit(GameSideEffect.Error(event.message))
            }
            is GameEvent.Disconnected -> {
                _uiState.update {
                    it.copy(
                        isConnecting = false,
                        isWaiting = false,
                        isConnected = false
                    )
                }
                _sideEffect.emit(GameSideEffect.Error("Conexión con el servidor perdida."))
            }
        }
    }

    private fun startCountdown(seconds: Int) {
        countdownJob = viewModelScope.launch {
            for (remaining in seconds downTo 0) {
                _uiState.update { it.copy(remainingSeconds = remaining) }
                if (remaining > 0) delay(1000L)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
