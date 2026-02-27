package com.duelmath.features.game.data.repositories

import com.duelmath.features.game.data.datasources.local.GameLocalDataSource
import com.duelmath.features.game.data.datasources.remote.model.GameWsMessage
import com.duelmath.features.game.data.datasources.remote.ws.GameWebSocketDataSource
import com.duelmath.features.game.domain.entities.GameEvent
import com.duelmath.features.game.domain.entities.GameOption
import com.duelmath.features.game.domain.entities.GameQuestion
import com.duelmath.features.game.domain.entities.RoundResult
import com.duelmath.features.game.domain.repositories.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val wsDataSource: GameWebSocketDataSource,
    private val localDataSource: GameLocalDataSource
) : GameRepository {

    // Scope tied to the Singleton lifetime — lives as long as the app
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _events = MutableSharedFlow<GameEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    init {
        repositoryScope.launch {
            wsDataSource.messages.collect { message -> processMessage(message) }
        }
    }

    // SSOT: persist to DataStore BEFORE emitting to the domain layer
    private suspend fun processMessage(message: GameWsMessage) {
        when (message) {
            is GameWsMessage.Authenticated -> {
                _events.emit(GameEvent.Authenticated(message.userId, message.username))
            }
            is GameWsMessage.Waiting -> {
                _events.emit(GameEvent.Waiting)
            }
            is GameWsMessage.GameStarted -> {
                localDataSource.saveSessionInfo(
                    sessionId = message.sessionId,
                    opponentUsername = message.opponentUsername,
                    totalRounds = message.totalRounds
                )
                _events.emit(
                    GameEvent.GameStarted(
                        sessionId = message.sessionId,
                        opponentUserId = message.opponentUserId,
                        opponentUsername = message.opponentUsername,
                        totalRounds = message.totalRounds
                    )
                )
            }
            is GameWsMessage.RoundStarted -> {
                val options = message.options.map { GameOption(it.id, it.text) }
                val question = GameQuestion(
                    id = message.questionId,
                    statement = message.statement,
                    difficulty = message.difficulty,
                    options = options
                )
                localDataSource.saveCurrentRound(
                    roundNumber = message.roundNumber,
                    questionJson = serializeQuestion(question),
                    timeLimitSeconds = message.timeLimitSeconds
                )
                _events.emit(
                    GameEvent.RoundStarted(
                        roundNumber = message.roundNumber,
                        question = question,
                        timeLimitSeconds = message.timeLimitSeconds
                    )
                )
            }
            is GameWsMessage.RoundResult -> {
                localDataSource.saveScoresJson(serializeScores(message.scores))
                _events.emit(
                    GameEvent.RoundEnded(
                        RoundResult(
                            roundNumber = message.roundNumber,
                            winnerId = message.winnerId,
                            correctOptionId = message.correctOptionId,
                            scores = message.scores
                        )
                    )
                )
            }
            is GameWsMessage.GameOver -> {
                localDataSource.clearGameData()
                _events.emit(
                    GameEvent.GameOver(
                        winnerId = message.winnerId,
                        reason = message.reason,
                        scores = message.scores,
                        eloChanges = message.eloChanges
                    )
                )
            }
            is GameWsMessage.Error -> {
                _events.emit(GameEvent.Error(message.code, message.message))
            }
            is GameWsMessage.Unknown -> {
                _events.emit(GameEvent.Disconnected)
            }
            is GameWsMessage.Pong -> Unit
        }
    }

    override fun connect(token: String) = wsDataSource.connect(token)

    override fun sendAnswer(questionId: String, optionId: String) =
        wsDataSource.sendAnswer(questionId, optionId)

    override fun disconnect() = wsDataSource.disconnect()

    private fun serializeQuestion(question: GameQuestion): String {
        val optionsArray = JSONArray().apply {
            question.options.forEach { opt ->
                put(JSONObject().apply {
                    put("id", opt.id)
                    put("text", opt.text)
                })
            }
        }
        return JSONObject().apply {
            put("id", question.id)
            put("statement", question.statement)
            put("difficulty", question.difficulty)
            put("options", optionsArray)
        }.toString()
    }

    private fun serializeScores(scores: Map<String, Int>): String {
        return JSONObject(scores as Map<*, *>).toString()
    }
}
