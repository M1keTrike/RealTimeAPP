package com.duelmath.features.matchmaking.presentation.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Star
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
import com.duelmath.core.ui.theme.*
import com.duelmath.features.matchmaking.domain.entities.GameSessionStatus
import com.duelmath.features.matchmaking.presentation.viewmodels.LobbyViewModel

@Composable
fun LobbyScreen(
    viewModel: LobbyViewModel = hiltViewModel(),
    onMatchFound: (String) -> Unit,
    onOpenQuestionsAdmin: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Refresh ELO every time this screen becomes active (e.g. returning from game)
    // and clear any stale session so the match-found LaunchedEffect does not
    // immediately re-navigate to a finished game.
    LaunchedEffect(Unit) {
        Log.d("LobbyScreen", "LaunchedEffect(Unit) — clearing session & refreshing ELO")
        viewModel.clearSession()
        viewModel.refreshElo()
    }

    LaunchedEffect(uiState.errorMessage) {
        Log.d("LobbyScreen", "LaunchedEffect(errorMessage) — errorMessage=${uiState.errorMessage}")
        if (uiState.errorMessage != null) {
            Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.navigateToGameSessionId) {
        val sessionId = uiState.navigateToGameSessionId
        Log.d("LobbyScreen", "LaunchedEffect(navigateToGameSessionId) — sessionId=$sessionId")
        if (sessionId != null) {
            Log.d("LobbyScreen", ">>> NAVIGATING to GameRoute sessionId=$sessionId")
            viewModel.onGameNavigated()   // consume immediately BEFORE navigating
            onMatchFound(sessionId)
        }
    }

    LaunchedEffect(uiState.logoutSuccess) {
        Log.d("LobbyScreen", "LaunchedEffect(logoutSuccess) — logoutSuccess=${uiState.logoutSuccess}")
        if (uiState.logoutSuccess) {
            onLogout()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Cerrar sesión",
                    tint = Color(0xFFEF4444)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AuthHeader("Lobby", "Encuentra un rival digno")

                Spacer(modifier = Modifier.height(16.dp))

                // ELO chip
                uiState.currentElo?.let { elo ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$elo ELO",
                                color = Color(0xFFF59E0B),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                if (uiState.isSearching || uiState.currentSession?.status == GameSessionStatus.WAITING) {
                    val text = if (uiState.isSearching) "Buscando oponente..." else "Sala creada. Esperando al jugador 2..."

                    DuelMathLoadingIndicator(message = text)

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedButton(
                        onClick = { viewModel.cancelMatchmaking() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                    ) {
                        Text("CANCELAR BÚSQUEDA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else {
                    if (uiState.isAdmin) {
                        OutlinedButton(
                            onClick = onOpenQuestionsAdmin,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ADMIN: GESTIONAR PREGUNTAS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

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
}
