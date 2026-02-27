package com.duelmath.core.navigation

import kotlinx.serialization.Serializable

@Serializable data object LoginRoute
@Serializable data object RegisterRoute

@Serializable data object LobbyRoute
@Serializable data object QuestionsRoute

@Serializable data class GameRoute(val sessionId: String)