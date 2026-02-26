package com.duelmath.features.matchmaking.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelmath.features.auth.data.datasources.local.AuthLocalDataSource
import com.duelmath.features.matchmaking.domain.usecases.CancelMatchUseCase
import com.duelmath.features.matchmaking.domain.usecases.FindMatchUseCase
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
    private val localDataSource: AuthLocalDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyState())
    val uiState: StateFlow<LobbyState> = _uiState.asStateFlow()

    fun startMatchmaking() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, errorMessage = null) }
            val userId = localDataSource.getUserId()

            if (userId == null) {
                _uiState.update { it.copy(isSearching = false, errorMessage = "Error de sesión. Vuelve a iniciar.") }
                return@launch
            }
            val result = findMatchUseCase(userId)
            result.onSuccess { session ->
                _uiState.update { it.copy(isSearching = false, currentSession = session) }
            }.onFailure { error ->
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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}