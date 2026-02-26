package com.duelmath.features.game.data.datasources.remote.ws

import com.duelmath.features.game.data.datasources.remote.model.GameWsMessage
import com.duelmath.features.game.data.datasources.remote.model.WsOption
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import javax.inject.Inject

private const val WS_BASE_URL = "ws://192.168.56.1:8080/ws"

class GameWebSocketDataSourceImpl @Inject constructor(
    private val okHttpClient: OkHttpClient
) : GameWebSocketDataSource {

    private val _messages = MutableSharedFlow<GameWsMessage>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val messages: SharedFlow<GameWsMessage> = _messages.asSharedFlow()

    private var webSocket: WebSocket? = null

    override fun connect(token: String) {
        val request = Request.Builder()
            .url("$WS_BASE_URL?token=$token")
            .build()
        webSocket = okHttpClient.newWebSocket(request, GameWebSocketListener())
    }

    override fun sendAnswer(questionId: String, optionId: String) {
        val json = JSONObject().apply {
            put("type", "answer")
            put("payload", JSONObject().apply {
                put("question_id", questionId)
                put("option_id", optionId)
            })
        }.toString()
        webSocket?.send(json)
    }

    override fun sendPing() {
        webSocket?.send(JSONObject().put("type", "ping").toString())
    }

    override fun disconnect() {
        webSocket?.close(1000, "User left")
        webSocket = null
    }

    private inner class GameWebSocketListener : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            _messages.tryEmit(parseMessage(text))
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            _messages.tryEmit(
                GameWsMessage.Error(
                    code = "CONNECTION_FAILED",
                    message = t.message ?: "Conexión WebSocket fallida"
                )
            )
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            _messages.tryEmit(GameWsMessage.Unknown)
        }
    }

    private fun parseMessage(raw: String): GameWsMessage {
        return try {
            val json = JSONObject(raw)
            val type = json.optString("type")
            val payload = json.optJSONObject("payload")

            when (type) {
                "authenticated" -> GameWsMessage.Authenticated(
                    userId = payload?.optString("user_id") ?: "",
                    username = payload?.optString("username") ?: ""
                )
                "waiting" -> GameWsMessage.Waiting
                "game_started" -> {
                    val opponent = payload?.optJSONObject("opponent")
                    GameWsMessage.GameStarted(
                        sessionId = payload?.optString("session_id") ?: "",
                        opponentUserId = opponent?.optString("user_id") ?: "",
                        opponentUsername = opponent?.optString("username") ?: "",
                        totalRounds = payload?.optInt("total_rounds") ?: 0
                    )
                }
                "round_started" -> {
                    val question = payload?.optJSONObject("question")
                    val optionsArray = question?.optJSONArray("options")
                        ?: return GameWsMessage.Unknown
                    val options = (0 until optionsArray.length()).map { i ->
                        val opt = optionsArray.getJSONObject(i)
                        WsOption(id = opt.optString("id"), text = opt.optString("text"))
                    }
                    GameWsMessage.RoundStarted(
                        roundNumber = payload?.optInt("round_number") ?: 0,
                        questionId = question?.optString("id") ?: "",
                        statement = question?.optString("statement") ?: "",
                        difficulty = question?.optString("difficulty") ?: "",
                        options = options,
                        timeLimitSeconds = payload?.optInt("time_limit_seconds") ?: 30
                    )
                }
                "round_result" -> {
                    val scoresJson = payload?.optJSONObject("scores")
                    val scores = buildScoresMap(scoresJson)
                    GameWsMessage.RoundResult(
                        roundNumber = payload?.optInt("round_number") ?: 0,
                        winnerId = payload?.optString("winner_id")?.takeIf { it.isNotBlank() },
                        correctOptionId = payload?.optString("correct_option_id") ?: "",
                        scores = scores
                    )
                }
                "game_over" -> {
                    val scoresJson = payload?.optJSONObject("scores")
                    val scores = buildScoresMap(scoresJson)
                    GameWsMessage.GameOver(
                        winnerId = payload?.optString("winner_id")?.takeIf { it.isNotBlank() },
                        reason = payload?.optString("reason") ?: "",
                        scores = scores
                    )
                }
                "error" -> GameWsMessage.Error(
                    code = payload?.optString("code") ?: "UNKNOWN",
                    message = payload?.optString("message") ?: "Error desconocido"
                )
                "pong" -> GameWsMessage.Pong
                else -> GameWsMessage.Unknown
            }
        } catch (e: Exception) {
            GameWsMessage.Error(
                code = "PARSE_ERROR",
                message = "Error al procesar mensaje del servidor"
            )
        }
    }

    private fun buildScoresMap(scoresJson: JSONObject?): Map<String, Int> {
        val scores = mutableMapOf<String, Int>()
        scoresJson?.keys()?.forEach { key -> scores[key] = scoresJson.optInt(key) }
        return scores
    }
}
