package com.duelmath.features.game.data.datasources.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "round_results")
data class RoundResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roundNumber: Int,
    val winnerId: String?,
    val correctOptionId: String,
    val scoresJson: String   // JSON serialization of Map<String, Int>
)
