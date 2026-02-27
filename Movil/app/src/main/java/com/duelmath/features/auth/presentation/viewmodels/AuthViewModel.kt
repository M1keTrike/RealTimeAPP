package com.duelmath.features.auth.presentation.viewmodels

import android.content.Context
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelmath.features.auth.data.datasources.remote.services.GoogleAuthService
import com.duelmath.features.auth.domain.usecases.GoogleSignInUseCase
import com.duelmath.features.auth.domain.usecases.LoginUseCase
import com.duelmath.features.auth.domain.usecases.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val googleAuthService: GoogleAuthService
) : ViewModel() {

    var username = MutableStateFlow("")
    var email = MutableStateFlow("")
    var password = MutableStateFlow("")

    private val _uiState = MutableStateFlow(AuthState())
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = loginUseCase(email.value, password.value)

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = registerUseCase(username.value, email.value, password.value)

            result.onSuccess {
                password.value = ""
                _uiState.update { it.copy(isLoading = false, registerSuccess = true) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    fun googleSignIn(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val idToken = googleAuthService.getIdToken(context)
                val result = googleSignInUseCase(idToken)

                result.onSuccess {
                    _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
            } catch (e: GetCredentialCancellationException) {
                // El usuario canceló el selector — no mostrar error
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: NoCredentialException) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "No hay cuentas de Google disponibles en este dispositivo")
                }
            } catch (e: GetCredentialException) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error al obtener credenciales de Google")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Error al iniciar sesión con Google")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setLoginPasswordVisible(isVisible: Boolean) {
        _uiState.update { it.copy(isLoginPasswordVisible = isVisible) }
    }

    fun setRegisterPasswordVisible(isVisible: Boolean) {
        _uiState.update { it.copy(isRegisterPasswordVisible = isVisible) }
    }

    fun resetSuccessStates() {
        _uiState.update {
            it.copy(
                loginSuccess = false,
                registerSuccess = false,
                isLoginPasswordVisible = false,
                isRegisterPasswordVisible = false,
            )
        }
    }
}