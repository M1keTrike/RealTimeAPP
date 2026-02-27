package com.duelmath.features.matchmaking.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelmath.features.auth.data.datasources.local.AuthLocalDataSource
import com.duelmath.features.auth.domain.entities.UserRole
import com.duelmath.features.auth.domain.usecases.LogoutUseCase
import com.duelmath.features.game.domain.usecases.DisconnectFromGameUseCase
import com.duelmath.features.matchmaking.domain.usecases.CancelMatchUseCase
import com.duelmath.features.matchmaking.domain.usecases.FindMatchUseCase
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val findMatchUseCase: FindMatchUseCase,
    private val cancelMatchUseCase: CancelMatchUseCase,
    private val localDataSource: AuthLocalDataSource,
    private val logoutUseCase: LogoutUseCase,
    private val disconnectFromGameUseCase: DisconnectFromGameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyState())
    val uiState: StateFlow<LobbyState> = _uiState.asStateFlow()

    init {
        Log.d("LobbyVM", "init — LobbyViewModel created (${hashCode()})")
        loadUserRole()
    }

    private fun loadUserRole() {
        viewModelScope.launch {
            val role = localDataSource.getUserRole()
            val elo = localDataSource.getEloRating()
            _uiState.update { it.copy(isAdmin = role == UserRole.ADMIN.value, currentElo = elo) }
        }
    }

    fun refreshElo() {
        Log.d("LobbyVM", "refreshElo called")
        viewModelScope.launch {
            val elo = localDataSource.getEloRating()
            _uiState.update { it.copy(currentElo = elo) }
        }
    }

    /**
     * Clears the previous game session so that the LaunchedEffect in LobbyScreen
     * does not re-trigger navigation to GameRoute with a stale IN_PROGRESS session.
     */
    fun clearSession() {
        Log.d("LobbyVM", "clearSession called — currentSession was: ${_uiState.value.currentSession?.id}, status: ${_uiState.value.currentSession?.status}")
        _uiState.update { it.copy(currentSession = null, isSearching = false, navigateToGameSessionId = null) }
    }

    fun startMatchmaking() {
        Log.d("LobbyVM", "startMatchmaking called")
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, errorMessage = null) }
            val userId = localDataSource.getUserId()

            if (userId == null) {
                Log.e("LobbyVM", "startMatchmaking — userId is null!")
                _uiState.update { it.copy(isSearching = false, errorMessage = "Error de sesión. Vuelve a iniciar.") }
                return@launch
            }
            Log.d("LobbyVM", "startMatchmaking — calling findMatchUseCase for userId=$userId")
            val result = findMatchUseCase(userId)
            result.onSuccess { session ->
                Log.d("LobbyVM", "startMatchmaking — SUCCESS session=${session.id}, status=${session.status}")
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        currentSession = session,
                        navigateToGameSessionId = if (session.status == com.duelmath.features.matchmaking.domain.entities.GameSessionStatus.IN_PROGRESS) session.id else null
                    )
                }
            }.onFailure { error ->
                Log.e("LobbyVM", "startMatchmaking — FAILURE: ${error.message}")
                _uiState.update { it.copy(isSearching = false, errorMessage = error.message) }
            }
        }
    }

    fun cancelMatchmaking() {
        viewModelScope.launch {
            val currentSessionId = _uiState.value.currentSession?.id
            if (currentSessionId != null) {
                val result = cancelMatchUseCase(currentSessionId)
                result.onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
            }
            _uiState.update {
                it.copy(
                    isSearching = false,
                    currentSession = null
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val currentSessionId = _uiState.value.currentSession?.id
            if (currentSessionId != null) {
                cancelMatchUseCase(currentSessionId)
            }
            disconnectFromGameUseCase()
            logoutUseCase()
            _uiState.update { it.copy(logoutSuccess = true) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** Called by the UI after it has navigated to GameRoute, so the event is not replayed. */
    fun onGameNavigated() {
        Log.d("LobbyVM", "onGameNavigated — consuming navigation event")
        _uiState.update { it.copy(navigateToGameSessionId = null) }
    }
}