package com.duelmath.features.game.data.datasources.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        RoundResultEntity::class,
        GameSessionEntity::class,
        GameRoundEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun roundResultDao(): RoundResultDao
    abstract fun gameSessionDao(): GameSessionDao
    abstract fun gameRoundDao(): GameRoundDao
}
