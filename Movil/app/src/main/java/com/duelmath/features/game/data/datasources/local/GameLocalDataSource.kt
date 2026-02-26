package com.duelmath.features.game.data.datasources.local

interface GameLocalDataSource {
    suspend fun saveSessionInfo(sessionId: String, opponentUsername: String, totalRounds: Int)
    suspend fun saveCurrentRound(roundNumber: Int, questionJson: String, timeLimitSeconds: Int)
    suspend fun saveScoresJson(scoresJson: String)
    suspend fun clearGameData()

    suspend fun getSessionId(): String?
    suspend fun getOpponentUsername(): String?
    suspend fun getTotalRounds(): Int
    suspend fun getCurrentRoundNumber(): Int
    suspend fun getCurrentQuestionJson(): String?
    suspend fun getScoresJson(): String?
}
