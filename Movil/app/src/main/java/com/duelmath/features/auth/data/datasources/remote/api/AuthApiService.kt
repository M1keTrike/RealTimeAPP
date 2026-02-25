package com.duelmath.features.auth.data.datasources.remote.api

import com.duelmath.features.auth.data.datasources.remote.model.ApiResponse
import com.duelmath.features.auth.data.datasources.remote.model.LoginRequest
import com.duelmath.features.auth.data.datasources.remote.model.RegisterRequest
import com.duelmath.features.auth.data.datasources.remote.model.TokenResponse
import com.duelmath.features.auth.data.datasources.remote.model.UserResponse
import retrofit2.http.*

interface AuthApiService{
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<TokenResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<UserResponse>
}