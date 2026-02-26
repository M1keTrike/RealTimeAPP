package com.duelmath.features.game.data.datasources.remote.ws

import com.duelmath.features.game.data.datasources.remote.model.GameWsMessage
import kotlinx.coroutines.flow.SharedFlow

interface GameWebSocketDataSource {
    val messages: SharedFlow<GameWsMessage>
    fun connect(token: String)
    fun sendAnswer(questionId: String, optionId: String)
    fun sendPing()
    fun disconnect()
}
