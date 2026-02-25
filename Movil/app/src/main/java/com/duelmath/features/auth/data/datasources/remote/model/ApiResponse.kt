package com.duelmath.features.auth.data.datasources.remote.model

data class ApiResponse<T>(
    val success: Boolean,
    val data: T,
    val message: String? = null
)