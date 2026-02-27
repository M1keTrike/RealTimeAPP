package com.duelmath.features.game.data.datasources.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey val sessionId: String,
    val opponentUserId: String,
    val opponentUsername: String,
    val totalRounds: Int,
    val startedAt: Long = System.currentTimeMillis()
)
