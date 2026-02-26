package com.duelmath.features.questions.data.repositories

import com.duelmath.features.questions.data.datasources.remote.api.QuestionsApiService
import com.duelmath.features.questions.data.datasources.remote.mapper.toDomain
import com.duelmath.features.questions.data.datasources.remote.model.CreateQuestionRequest
import com.duelmath.features.questions.data.datasources.remote.model.UpdateQuestionRequest
import com.duelmath.features.questions.domain.entities.Question
import com.duelmath.features.questions.domain.entities.QuestionDifficulty
import com.duelmath.features.questions.domain.repositories.QuestionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

class QuestionsRepositoryImpl @Inject constructor(
    private val apiService: QuestionsApiService,
) : QuestionsRepository {

    override suspend fun getAllQuestions(): Result<List<Question>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllQuestions()
                Result.success(response.data.map { it.toDomain() })
            } catch (e: HttpException) {
                Result.failure(Exception(parseHttpError(e, "Error al obtener preguntas")))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión al obtener preguntas"))
            }
        }
    }

    override suspend fun createQuestion(
        statement: String,
        difficulty: QuestionDifficulty,
        options: List<String>,
        correctOptionIndex: Int,
    ): Result<Question> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateQuestionRequest(
                    statement = statement,
                    difficulty = difficulty,
                    options = options,
                    correctOptionIndex = correctOptionIndex,
                )
                val response = apiService.createQuestion(request)
                Result.success(response.data.toDomain())
            } catch (e: HttpException) {
                Result.failure(Exception(parseHttpError(e, "Error al crear pregunta")))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión al crear pregunta"))
            }
        }
    }

    override suspend fun updateQuestion(
        id: String,
        statement: String,
        difficulty: QuestionDifficulty,
        options: List<String>,
        correctOptionIndex: Int,
    ): Result<Question> {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateQuestionRequest(
                    statement = statement,
                    difficulty = difficulty,
                    options = options,
                    correctOptionIndex = correctOptionIndex,
                )
                val response = apiService.updateQuestion(id, request)
                Result.success(response.data.toDomain())
            } catch (e: HttpException) {
                Result.failure(Exception(parseHttpError(e, "Error al actualizar pregunta")))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión al actualizar pregunta"))
            }
        }
    }

    override suspend fun deleteQuestion(id: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteQuestion(id)
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
                Result.failure(Exception("Error de conexión al eliminar pregunta"))
            }
        }
    }

    private fun parseHttpError(e: HttpException, fallback: String): String {
        val errorBody = e.response()?.errorBody()?.string()
        return try {
            JSONObject(errorBody!!).getString("message")
        } catch (ex: Exception) {
            fallback
        }
    }
}