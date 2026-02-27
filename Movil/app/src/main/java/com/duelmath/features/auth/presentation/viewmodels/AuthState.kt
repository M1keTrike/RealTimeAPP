package com.duelmath.features.auth.presentation.viewmodels

data class AuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false,
    val registerSuccess: Boolean = false,
    val isLoginPasswordVisible: Boolean = false,
    val isRegisterPasswordVisible: Boolean = false,
)