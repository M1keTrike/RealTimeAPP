package com.duelmath.features.game.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelmath.features.auth.data.datasources.local.AuthLocalDataSource
import com.duelmath.features.auth.domain.repositories.AuthRepository
import com.duelmath.features.game.domain.entities.GameEvent
import com.duelmath.features.game.domain.entities.RoundResult
import com.duelmath.features.game.domain.usecases.ConnectToGameUseCase
import com.duelmath.features.game.domain.usecases.DisconnectFromGameUseCase
import com.duelmath.features.game.domain.usecases.ObserveGameEventsUseCase
import com.duelmath.features.game.domain.usecases.ObserveRoundResultsUseCase
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
    private val observeRoundResultsUseCase: ObserveRoundResultsUseCase,
    private val sendAnswerUseCase: SendAnswerUseCase,
    private val disconnectFromGameUseCase: DisconnectFromGameUseCase,
    private val authLocalDataSource: AuthLocalDataSource,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<GameSideEffect>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val sideEffect: SharedFlow<GameSideEffect> = _sideEffect.asSharedFlow()

    private var myUserId: String? = null
    private var countdownJob: Job? = null

    // Tracks the last round result processed from Room to avoid re-handling on re-emission
    private var lastProcessedRound = 0

    init {
        observeEvents()
        observeRoundResults()
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
     * The round result from Room will later reveal whether the selection was correct.
     */
    fun selectAnswer(questionId: String, optionId: String) {
        if (_uiState.value.selectedOptionId != null || _uiState.value.isShowingResult) return
        _uiState.update { it.copy(selectedOptionId = optionId) }
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

    // Observes Room as SSOT for round results.
    // Room emits the full list on every insert; we only react to rounds not yet processed.
    private fun observeRoundResults() {
        viewModelScope.launch {
            observeRoundResultsUseCase().collect { results ->
                val newResult = results.lastOrNull { it.roundNumber > lastProcessedRound }
                    ?: return@collect
                lastProcessedRound = newResult.roundNumber
                handleRoundResult(newResult)
            }
        }
    }

    private suspend fun handleRoundResult(result: RoundResult) {
        countdownJob?.cancel()
        val myId = myUserId
        val myScore = if (myId != null) result.scores[myId] ?: 0 else 0
        val opponentScore = result.scores.entries
            .firstOrNull { it.key != myId }?.value ?: 0

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

    private suspend fun handleEvent(event: GameEvent) {
        when (event) {
            is GameEvent.Authenticated -> {
                _uiState.update { it.copy(isConnecting = false, isConnected = true) }
            }
            is GameEvent.Waiting -> {
                _uiState.update { it.copy(isWaiting = true) }
            }
            is GameEvent.GameStarted -> {
                // Reset round tracking for the new game session
                lastProcessedRound = 0
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
                        selectedOptionId = null,
                        correctOptionId = null,
                        isShowingResult = false,
                        roundWinnerId = null
                    )
                }
                startCountdown(event.timeLimitSeconds)
            }
            is GameEvent.GameOver -> {
                countdownJob?.cancel()
                val iWon = event.winnerId == myUserId
                val isDraw = event.winnerId == null

                val eloChange = myUserId?.let { event.eloChanges[it] } ?: when {
                    iWon   -> +15
                    isDraw ->   0
                    else   -> -15
                }
                val currentElo = authLocalDataSource.getEloRating() ?: 1200
                val newElo = (currentElo + eloChange).coerceAtLeast(0)

                val userId = myUserId
                if (userId != null) {
                    viewModelScope.launch {
                        authRepository.updateEloRating(userId, newElo)
                    }
                } else {
                    authLocalDataSource.saveEloRating(newElo)
                }

                _uiState.update {
                    it.copy(
                        isGameOver = true,
                        gameWinnerId = event.winnerId,
                        gameOverReason = event.reason,
                        myEloChange = eloChange,
                        myNewElo = newElo
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
