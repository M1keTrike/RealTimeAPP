package com.duelmath.features.game.data.datasources.local.db

import androidx.room.Entity

@Entity(
    tableName = "game_rounds",
    primaryKeys = ["sessionId", "roundNumber"]
)
data class GameRoundEntity(
    val sessionId: String,
    val roundNumber: Int,
    val questionJson: String,
    val timeLimitSeconds: Int
)
