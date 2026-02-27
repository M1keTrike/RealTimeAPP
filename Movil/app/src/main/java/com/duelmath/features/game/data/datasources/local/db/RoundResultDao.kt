package com.duelmath.features.game.data.datasources.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RoundResultEntity)

    @Query("SELECT * FROM round_results ORDER BY roundNumber ASC")
    fun observeAll(): Flow<List<RoundResultEntity>>

    @Query("DELETE FROM round_results")
    suspend fun clearAll()
}
