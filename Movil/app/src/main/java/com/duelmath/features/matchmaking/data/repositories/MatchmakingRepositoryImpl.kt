package com.duelmath.features.matchmaking.data.repositories

import com.duelmath.features.matchmaking.data.datasources.remote.api.MatchmakingApiService
import com.duelmath.features.matchmaking.data.datasources.remote.mapper.toDomain
import com.duelmath.features.matchmaking.data.datasources.remote.model.MatchmakeRequest
import com.duelmath.features.matchmaking.domain.entities.GameSession
import com.duelmath.features.matchmaking.domain.repositories.MatchmakingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class MatchmakingRepositoryImpl @Inject constructor(
    private val apiService: MatchmakingApiService
) : MatchmakingRepository {

    override suspend fun findMatch(userId: String): Result<GameSession> {
        return withContext(Dispatchers.IO) {
            try {
                val request = MatchmakeRequest(userId)
                val response = apiService.findMatch(request)
                Result.success(response.data.toDomain())
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = try {
                    JSONObject(errorBody!!).getString("message")
                } catch (ex: Exception) { "Error al buscar partida" }
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión al servidor de emparejamiento."))
            }
        }
    }
    override suspend fun cancelMatch(sessionId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.cancelMatch(sessionId)
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        JSONObject(errorBody!!).getString("message")
                    } catch (ex: Exception) {
                        "Error HTTP: ${response.code()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión al cancelar."))
            }
        }
    }
}