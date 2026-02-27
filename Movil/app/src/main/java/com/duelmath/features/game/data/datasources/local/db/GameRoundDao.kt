package com.duelmath.features.game.data.datasources.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRoundDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GameRoundEntity)

    @Query("SELECT * FROM game_rounds WHERE sessionId = :sessionId ORDER BY roundNumber DESC LIMIT 1")
    fun observeLatestForSession(sessionId: String): Flow<GameRoundEntity?>

    @Query("DELETE FROM game_rounds")
    suspend fun clearAll()
}
