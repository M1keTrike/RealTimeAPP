package com.duelmath.features.matchmaking.presentation.viewmodels

import com.duelmath.features.matchmaking.domain.entities.GameSession

data class LobbyState(
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
    val currentSession: GameSession? = null
)