package com.duelmath.features.game.data.datasources.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GameSessionEntity)

    @Query("SELECT * FROM game_sessions ORDER BY startedAt DESC LIMIT 1")
    fun observeLatest(): Flow<GameSessionEntity?>

    @Query("DELETE FROM game_sessions")
    suspend fun clearAll()
}
