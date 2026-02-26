package com.duelmath.features.questions.data.datasources.remote.api

import com.duelmath.features.auth.data.datasources.remote.model.ApiResponse
import com.duelmath.features.questions.data.datasources.remote.model.CreateQuestionRequest
import com.duelmath.features.questions.data.datasources.remote.model.QuestionResponse
import com.duelmath.features.questions.data.datasources.remote.model.UpdateQuestionRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface QuestionsApiService {
    @GET("questions")
    suspend fun getAllQuestions(): ApiResponse<List<QuestionResponse>>

    @POST("questions")
    suspend fun createQuestion(@Body request: CreateQuestionRequest): ApiResponse<QuestionResponse>

    @PUT("questions/{id}")
    suspend fun updateQuestion(
        @Path("id") id: String,
        @Body request: UpdateQuestionRequest,
    ): ApiResponse<QuestionResponse>

    @DELETE("questions/{id}")
    suspend fun deleteQuestion(@Path("id") id: String): ApiResponse<Map<String, Any>?>
}