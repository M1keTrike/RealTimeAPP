package com.duelmath.features.auth.data.repositories

import com.duelmath.features.auth.data.datasources.local.AuthLocalDataSource
import com.duelmath.features.auth.data.datasources.remote.api.AuthApiService
import com.duelmath.features.auth.data.datasources.remote.mapper.toDomain
import com.duelmath.features.auth.data.datasources.remote.model.GoogleSignInRequest
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
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthResult> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email = email, password = password)
                val response = authApiService.login(request)
                val domainData = response.data.toDomain()
                localDataSource.saveToken(response.data.accessToken)
                localDataSource.saveUserId(domainData.user.id)
                localDataSource.saveUsername(domainData.user.username)
                localDataSource.saveUserRole(domainData.user.role.value)
                Result.success(response.data.toDomain())
            } catch (e: HttpException) {
                Result.failure(Exception(parseHttpError(e, "Error en el servidor de autenticación")))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión. Verifica tu internet."))
            }
        }
    }

    override suspend fun register(username: String, email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(username = username, email = email, password = password)
                val response = authApiService.register(request)
                Result.success(response.data.toDomain())
            } catch (e: HttpException) {
                Result.failure(Exception(parseHttpError(e, "Error al intentar registrar el usuario")))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión. Verifica tu internet."))
            }
        }
    }

    override suspend fun googleSignIn(idToken: String): Result<AuthResult> {
        return withContext(Dispatchers.IO) {
            try {
                val request = GoogleSignInRequest(idToken = idToken)
                val response = authApiService.googleSignIn(request)
                val domainData = response.data.toDomain()
                localDataSource.saveToken(response.data.accessToken)
                localDataSource.saveUserId(domainData.user.id)
                localDataSource.saveUsername(domainData.user.username)
                localDataSource.saveUserRole(domainData.user.role.value)
                Result.success(domainData)
            } catch (e: HttpException) {
                Result.failure(Exception(parseHttpError(e, "Error al autenticar con Google")))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión. Verifica tu internet."))
            }
        }
    }

    override suspend fun logout() {
        localDataSource.clearSession()
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