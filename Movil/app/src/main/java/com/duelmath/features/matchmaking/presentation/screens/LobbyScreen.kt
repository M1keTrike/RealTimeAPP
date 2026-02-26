package com.duelmath.features.matchmaking.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duelmath.core.ui.components.DuelMathLoadingIndicator
import com.duelmath.features.auth.presentation.components.AuthHeader
import com.duelmath.features.auth.presentation.components.DarkBackground
import com.duelmath.features.matchmaking.domain.entities.GameSessionStatus
import com.duelmath.features.matchmaking.presentation.viewmodels.LobbyViewModel

@Composable
fun LobbyScreen(
    viewModel: LobbyViewModel = hiltViewModel(),
    onMatchFound: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.currentSession) {
        if (uiState.currentSession?.status == GameSessionStatus.IN_PROGRESS) {
            onMatchFound(uiState.currentSession!!.id)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AuthHeader("Lobby", "Encuentra un rival digno")

            Spacer(modifier = Modifier.height(64.dp))

            if (uiState.isSearching || uiState.currentSession?.status == GameSessionStatus.WAITING) {
                val text = if (uiState.isSearching) "Buscando oponente..." else "Sala creada. Esperando al jugador 2..."

                DuelMathLoadingIndicator(message = text)

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = { viewModel.cancelMatchmaking() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)), // Rojo Tailwind
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                ) {
                    Text("CANCELAR BÚSQUEDA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            else {
                Button(
                    onClick = { viewModel.startMatchmaking() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("BUSCAR PARTIDA", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}