package com.duelmath.features.auth.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duelmath.core.ui.theme.*
import com.duelmath.features.auth.presentation.components.*
import com.duelmath.features.auth.presentation.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.loginSuccess, uiState.errorMessage) {
        if (uiState.loginSuccess) {
            viewModel.resetSuccessStates()
            onLoginSuccess()
        }
        if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(uiState.errorMessage!!)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LoginContent(
            modifier = Modifier.padding(innerPadding),
            email = email,
            password = password,
            isLoading = uiState.isLoading,
            isPasswordVisible = uiState.isLoginPasswordVisible,
            onEmailChange = { viewModel.email.value = it },
            onPasswordChange = { viewModel.password.value = it },
            onPasswordVisibilityChange = viewModel::setLoginPasswordVisible,
            onLoginClick = { viewModel.login() },
            onGoogleClick = { viewModel.googleSignIn(context) },
            onNavigateToRegister = onNavigateToRegister
        )
    }
}

// 2. Versión Stateless (Solo UI, ideal para Previews)
@Composable
private fun LoginContent(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
    isLoading: Boolean,
    isPasswordVisible: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AuthHeader("Sign In", "Welcome back to the arena")

        Spacer(modifier = Modifier.height(32.dp))

        AuthSocialLogins(
            onGoogleClick = onGoogleClick,
        )

        Spacer(modifier = Modifier.height(24.dp))

        AuthTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            placeholder = "player@example.com",
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            placeholder = "Enter your password",
            isPassword = true,
            passwordVisible = isPasswordVisible,
            onPasswordVisibilityChange = onPasswordVisibilityChange,
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuthMainButton(
            text = "Sign In",
            onClick = onLoginClick,
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.Center) {
            Text("Don't have an account? ", color = TextGray, fontSize = 14.sp)
            Text(
                text = "Sign up",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
}

// 3. Preview
@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
fun LoginContentPreview() {
    MaterialTheme {
        LoginContent(
            email = "",
            password = "",
            isLoading = false,
            isPasswordVisible = false,
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordVisibilityChange = {},
            onLoginClick = {},
            onGoogleClick = {},
            onNavigateToRegister = {}
        )
    }
}