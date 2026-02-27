package com.duelmath.features.game.domain.entities

data class RoundResult(
    val roundNumber: Int,
    val winnerId: String?,
    val correctOptionId: String,
    val scores: Map<String, Int>
)
