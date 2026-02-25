package com.duelmath.features.auth.data.repositories

import com.duelmath.features.auth.data.datasources.local.AuthLocalDataSource
import com.duelmath.features.auth.data.datasources.remote.api.AuthApiService
import com.duelmath.features.auth.data.datasources.remote.mapper.toDomain
import com.duelmath.features.auth.data.datasources.remote.model.LoginRequest
import com.duelmath.features.auth.data.datasources.remote.model.RegisterRequest
import com.duelmath.features.auth.domain.entities.AuthResult
import com.duelmath.features.auth.domain.entities.User
import com.duelmath.features.auth.domain.repositories.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val localDataSource: AuthLocalDataSource
): AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthResult> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email = email, password = password)
                val response = authApiService.login(request)
                localDataSource.saveToken(response.data.accessToken)
                Result.success(response.data.toDomain())

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = try {
                    JSONObject(errorBody!!).getString("message")
                } catch (ex: Exception) {
                    "Error en el servidor de autenticación"
                }
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión. Verifica tu internet."))
            }
        }
    }

    override suspend fun register( username: String,email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {

                val request = RegisterRequest( username= username, email = email, password = password)
                val response = authApiService.register(request)
                Result.success(response.data.toDomain())

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = try {
                    JSONObject(errorBody!!).getString("message")
                } catch (ex: Exception) {
                    "Error al intentar registrar el usuario"
                }
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión. Verifica tu internet."))
            }
        }
    }
}