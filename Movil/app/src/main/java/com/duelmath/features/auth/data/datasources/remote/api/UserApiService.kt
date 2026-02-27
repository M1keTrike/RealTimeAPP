package com.duelmath.features.auth.data.datasources.remote.api

import com.duelmath.features.auth.data.datasources.remote.model.ApiResponse
import com.duelmath.features.auth.data.datasources.remote.model.EloResponse
import com.duelmath.features.auth.data.datasources.remote.model.UpdateEloRequest
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Path

interface UserApiService {
    @PATCH("users/{id}/elo")
    suspend fun updateEloRating(
        @Path("id") userId: String,
        @Body request: UpdateEloRequest
    ): ApiResponse<EloResponse>
}
