package com.duelmath.features.game.domain.entities

data class GameQuestion(
    val id: String,
    val statement: String,
    val difficulty: String,
    val options: List<GameOption>
)
