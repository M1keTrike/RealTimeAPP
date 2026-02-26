package com.duelmath.features.game.data.datasources.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GameLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : GameLocalDataSource {

    private object Keys {
        val SESSION_ID          = stringPreferencesKey("game_session_id")
        val OPPONENT_USERNAME   = stringPreferencesKey("game_opponent_username")
        val TOTAL_ROUNDS        = intPreferencesKey("game_total_rounds")
        val CURRENT_ROUND       = intPreferencesKey("game_current_round")
        val CURRENT_QUESTION    = stringPreferencesKey("game_current_question_json")
        val TIME_LIMIT          = intPreferencesKey("game_time_limit_seconds")
        val SCORES_JSON         = stringPreferencesKey("game_scores_json")
    }

    override suspend fun saveSessionInfo(
        sessionId: String,
        opponentUsername: String,
        totalRounds: Int
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.SESSION_ID]        = sessionId
            prefs[Keys.OPPONENT_USERNAME] = opponentUsername
            prefs[Keys.TOTAL_ROUNDS]      = totalRounds
        }
    }

    override suspend fun saveCurrentRound(
        roundNumber: Int,
        questionJson: String,
        timeLimitSeconds: Int
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.CURRENT_ROUND]    = roundNumber
            prefs[Keys.CURRENT_QUESTION] = questionJson
            prefs[Keys.TIME_LIMIT]       = timeLimitSeconds
        }
    }

    override suspend fun saveScoresJson(scoresJson: String) {
        dataStore.edit { prefs -> prefs[Keys.SCORES_JSON] = scoresJson }
    }

    override suspend fun clearGameData() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.SESSION_ID)
            prefs.remove(Keys.OPPONENT_USERNAME)
            prefs.remove(Keys.TOTAL_ROUNDS)
            prefs.remove(Keys.CURRENT_ROUND)
            prefs.remove(Keys.CURRENT_QUESTION)
            prefs.remove(Keys.TIME_LIMIT)
            prefs.remove(Keys.SCORES_JSON)
        }
    }

    override suspend fun getSessionId(): String? =
        dataStore.data.first()[Keys.SESSION_ID]

    override suspend fun getOpponentUsername(): String? =
        dataStore.data.first()[Keys.OPPONENT_USERNAME]

    override suspend fun getTotalRounds(): Int =
        dataStore.data.first()[Keys.TOTAL_ROUNDS] ?: 0

    override suspend fun getCurrentRoundNumber(): Int =
        dataStore.data.first()[Keys.CURRENT_ROUND] ?: 0

    override suspend fun getCurrentQuestionJson(): String? =
        dataStore.data.first()[Keys.CURRENT_QUESTION]

    override suspend fun getScoresJson(): String? =
        dataStore.data.first()[Keys.SCORES_JSON]
}
