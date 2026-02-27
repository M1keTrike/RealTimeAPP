package com.duelmath.features.game.data.repositories

import com.duelmath.features.game.data.datasources.local.db.GameRoundDao
import com.duelmath.features.game.data.datasources.local.db.GameRoundEntity
import com.duelmath.features.game.data.datasources.local.db.GameSessionDao
import com.duelmath.features.game.data.datasources.local.db.GameSessionEntity
import com.duelmath.features.game.data.datasources.local.db.RoundResultDao
import com.duelmath.features.game.data.datasources.local.db.RoundResultEntity
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val wsDataSource: GameWebSocketDataSource,
    private val roundResultDao: RoundResultDao,
    private val gameSessionDao: GameSessionDao,
    private val gameRoundDao: GameRoundDao
) : GameRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Cached across messages: RoundStarted doesn't carry sessionId, so we track it from GameStarted
    private var currentSessionId: String = ""

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

    // Room is the SSOT for all game data that comes from the WebSocket.
    // DataStore (AuthLocalDataSource) is only used for auth credentials.
    private suspend fun processMessage(message: GameWsMessage) {
        when (message) {
            is GameWsMessage.Authenticated -> {
                _events.emit(GameEvent.Authenticated(message.userId, message.username))
            }
            is GameWsMessage.Waiting -> {
                _events.emit(GameEvent.Waiting)
            }
            is GameWsMessage.GameStarted -> {
                currentSessionId = message.sessionId
                // Clear round results from the previous session
                roundResultDao.clearAll()
                // Persist session metadata in Room
                gameSessionDao.insert(
                    GameSessionEntity(
                        sessionId = message.sessionId,
                        opponentUserId = message.opponentUserId,
                        opponentUsername = message.opponentUsername,
                        totalRounds = message.totalRounds
                    )
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
                // Persist round data in Room (sessionId tracked from GameStarted)
                gameRoundDao.insert(
                    GameRoundEntity(
                        sessionId = currentSessionId,
                        roundNumber = message.roundNumber,
                        questionJson = serializeQuestion(question),
                        timeLimitSeconds = message.timeLimitSeconds
                    )
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
                // Write to Room — ViewModel observes via observeRoundResults()
                roundResultDao.insert(
                    RoundResultEntity(
                        roundNumber = message.roundNumber,
                        winnerId = message.winnerId,
                        correctOptionId = message.correctOptionId,
                        scoresJson = serializeScores(message.scores)
                    )
                )
            }
            is GameWsMessage.GameOver -> {
                // Room retains session and round history after the game ends.
                // Only round results (SSOT for the active game) are reset on next GameStarted.
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
            // Normal server-initiated close (e.g. after game_over): signal disconnection
            // so the ViewModel can update isConnected, but without treating it as an error.
            is GameWsMessage.ConnectionClosed -> {
                _events.emit(GameEvent.Disconnected)
            }
            // Unknown message type: protocol mismatch — treat as unexpected disconnection.
            is GameWsMessage.Unknown -> {
                _events.emit(GameEvent.Disconnected)
            }
            is GameWsMessage.Pong -> Unit
        }
    }

    override fun observeRoundResults(): Flow<List<RoundResult>> =
        roundResultDao.observeAll().map { entities -> entities.map { it.toDomain() } }

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

    private fun serializeScores(scores: Map<String, Int>): String =
        JSONObject(scores as Map<*, *>).toString()

    private fun RoundResultEntity.toDomain(): RoundResult {
        val json = JSONObject(scoresJson)
        val scores = json.keys().asSequence().associateWith { json.optInt(it) }
        return RoundResult(
            roundNumber = roundNumber,
            winnerId = winnerId,
            correctOptionId = correctOptionId,
            scores = scores
        )
    }
}
