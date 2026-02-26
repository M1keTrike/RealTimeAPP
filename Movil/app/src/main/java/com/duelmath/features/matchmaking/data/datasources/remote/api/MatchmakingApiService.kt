package com.duelmath.features.matchmaking.data.datasources.remote.api

import com.duelmath.features.auth.data.datasources.remote.model.ApiResponse
import com.duelmath.features.matchmaking.data.datasources.remote.model.GameSessionResponse
import com.duelmath.features.matchmaking.data.datasources.remote.model.MatchmakeRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.Response

interface MatchmakingApiService {
    @POST("game-sessions/matchmake")
    suspend fun findMatch(@Body request: MatchmakeRequest): ApiResponse<GameSessionResponse>

    @DELETE("game-sessions/{id}/cancel")
    suspend fun cancelMatch(@Path("id") sessionId: String): Response<Unit>
}