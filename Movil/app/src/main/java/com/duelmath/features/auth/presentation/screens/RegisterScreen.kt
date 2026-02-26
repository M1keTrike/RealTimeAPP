package com.duelmath.features.auth.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duelmath.core.ui.theme.*
import com.duelmath.features.auth.presentation.components.*
import com.duelmath.features.auth.presentation.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val username by viewModel.username.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.registerSuccess, uiState.errorMessage) {
        if (uiState.registerSuccess) {
            viewModel.resetSuccessStates()
            onRegisterSuccess()
        }
        if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(uiState.errorMessage!!)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al Login"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        RegisterContent(
            modifier = Modifier.padding(innerPadding),
            username = username,
            email = email,
            password = password,
            isLoading = uiState.isLoading,
            isPasswordVisible = uiState.isRegisterPasswordVisible,
            onUsernameChange = { viewModel.username.value = it },
            onEmailChange = { viewModel.email.value = it },
            onPasswordChange = { viewModel.password.value = it },
            onPasswordVisibilityChange = viewModel::setRegisterPasswordVisible,
            onRegisterClick = { viewModel.register() },
            onGoogleClick = { viewModel.googleSignIn(context) }
        )
    }
}

// 2. Versión Stateless
@Composable
private fun RegisterContent(
    modifier: Modifier = Modifier,
    username: String,
    email: String,
    password: String,
    isLoading: Boolean,
    isPasswordVisible: Boolean,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AuthHeader("Sign Up", "Create your account to start dueling")
        Spacer(modifier = Modifier.height(32.dp))

        AuthSocialLogins(
            onGoogleClick = onGoogleClick,
        )

        Spacer(modifier = Modifier.height(24.dp))
        AuthTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = "Username",
            placeholder = "patata",
            keyboardType = KeyboardType.Text
        )
        Spacer(modifier = Modifier.height(16.dp))
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
            placeholder = "At least 6 characters",
            isPassword = true,
            passwordVisible = isPasswordVisible,
            onPasswordVisibilityChange = onPasswordVisibilityChange,
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuthMainButton(
            text = "Create Account",
            onClick = onRegisterClick,
            isLoading = isLoading
        )
    }
}

// 3. Preview
@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
fun RegisterContentPreview() {
    MaterialTheme {
        RegisterContent(
            username = "",
            email = "",
            password = "",
            isLoading = false,
            isPasswordVisible = false,
            onUsernameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordVisibilityChange = {},
            onRegisterClick = {},
            onGoogleClick = {}
        )
    }
}