package com.duelmath.features.game.domain.repositories

import com.duelmath.features.game.domain.entities.GameEvent
import com.duelmath.features.game.domain.entities.RoundResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface GameRepository {
    val events: SharedFlow<GameEvent>
    fun connect(token: String)
    fun sendAnswer(questionId: String, optionId: String)
    fun disconnect()
    fun observeRoundResults(): Flow<List<RoundResult>>
}
