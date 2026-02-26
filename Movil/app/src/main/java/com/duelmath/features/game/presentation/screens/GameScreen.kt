package com.duelmath.features.game.presentation.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duelmath.features.game.domain.entities.GameOption
import com.duelmath.features.game.presentation.viewmodels.GameSideEffect
import com.duelmath.features.game.presentation.viewmodels.GameUiState
import com.duelmath.features.game.presentation.viewmodels.GameViewModel
import com.duelmath.core.ui.components.DuelMathLoadingIndicator

private val DarkBg       = Color(0xFF0D1117)
private val CardBg       = Color(0xFF161B22)
private val BluePrimary  = Color(0xFF2563EB)
private val GreenCorrect = Color(0xFF16A34A)
private val RedWrong     = Color(0xFFDC2626)
private val GoldAccent   = Color(0xFFF59E0B)

@Composable
fun GameScreen(
    sessionId: String,
    viewModel: GameViewModel = hiltViewModel(),
    onGameOver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // One-time side-effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is GameSideEffect.Error -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is GameSideEffect.RoundWon ->
                    Toast.makeText(context, "Ronda ganada!", Toast.LENGTH_SHORT).show()
                is GameSideEffect.RoundLost ->
                    Toast.makeText(context, "Ronda perdida", Toast.LENGTH_SHORT).show()
                is GameSideEffect.GameWon, is GameSideEffect.GameLost -> Unit
            }
        }
    }

    // Navigate away when game ends and user taps the result button
    Surface(modifier = Modifier.fillMaxSize(), color = DarkBg) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isConnecting -> {
                    DuelMathLoadingIndicator(
                        message = "Conectando al servidor...",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                uiState.isWaiting -> {
                    DuelMathLoadingIndicator(
                        message = "Sala creada. Esperando al jugador 2...",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                uiState.isGameOver -> GameOverOverlay(uiState, onGameOver)
                uiState.question != null -> {
                    ActiveGameContent(
                        uiState = uiState,
                        onAnswerSelected = { optionId ->
                            uiState.question?.let { q ->
                                viewModel.selectAnswer(q.id, optionId)
                            }
                        },
                        onQuit = {
                            viewModel.disconnect()
                            onGameOver()
                        }
                    )
                }
                else -> {
                    DuelMathLoadingIndicator(
                        message = "Conectando al servidor...",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Loading states
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ConnectingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = BluePrimary)
        Spacer(Modifier.height(16.dp))
        Text("Conectando al servidor...", color = Color.LightGray, fontSize = 16.sp)
    }
}

@Composable
private fun WaitingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = GoldAccent)
        Spacer(Modifier.height(16.dp))
        Text("Buscando oponente...", color = Color.LightGray, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Text("ARENA MODE", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Active game UI
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ActiveGameContent(
    uiState: GameUiState,
    onAnswerSelected: (String) -> Unit,
    onQuit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        TopBar(
            currentRound = uiState.currentRound,
            totalRounds = uiState.totalRounds,
            remainingSeconds = uiState.remainingSeconds,
            timeLimitSeconds = uiState.timeLimitSeconds,
            onQuit = onQuit
        )

        Spacer(Modifier.height(12.dp))

        // Player vs Opponent scores
        ScoreRow(
            myScore = uiState.myScore,
            opponentUsername = uiState.opponentUsername ?: "Oponente",
            opponentScore = uiState.opponentScore
        )

        Spacer(Modifier.height(16.dp))

        // Question card
        uiState.question?.let { question ->
            QuestionCard(statement = question.statement, difficulty = question.difficulty)

            Spacer(Modifier.height(24.dp))

            // 2×2 answer grid
            AnswerGrid(
                options = question.options,
                selectedOptionId = uiState.selectedOptionId,
                correctOptionId = uiState.correctOptionId,
                isShowingResult = uiState.isShowingResult,
                onOptionSelected = onAnswerSelected
            )
        }

        Spacer(Modifier.weight(1f))

        // Round indicator
        Text(
            text = "Ronda ${uiState.currentRound} de ${uiState.totalRounds}",
            color = Color.Gray,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TopBar(
    currentRound: Int,
    totalRounds: Int,
    remainingSeconds: Int,
    timeLimitSeconds: Int,
    onQuit: () -> Unit
) {
    val progress = if (timeLimitSeconds > 0) remainingSeconds.toFloat() / timeLimitSeconds else 0f
    val timerColor by animateColorAsState(
        targetValue = when {
            progress > 0.5f -> Color(0xFF22C55E)
            progress > 0.25f -> GoldAccent
            else -> RedWrong
        },
        animationSpec = tween(300),
        label = "timerColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quit button
        IconButton(onClick = onQuit) {
            Icon(Icons.Default.Close, contentDescription = "Salir", tint = Color.White)
        }

        // Center: mode + progress + timer
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ARENA MODE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = timerColor,
                trackColor = Color.DarkGray
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "0:${remainingSeconds.toString().padStart(2, '0')}",
                color = timerColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Placeholder to balance the quit button
        Spacer(Modifier.width(48.dp))
    }
}

@Composable
private fun ScoreRow(myScore: Int, opponentUsername: String, opponentScore: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // My side
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BluePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text("TÚ", color = Color.Gray, fontSize = 10.sp)
                Text(myScore.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            // VS
            Text(
                text = "VS",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            // Opponent side
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C3AED)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text(opponentUsername.take(10), color = Color.Gray, fontSize = 10.sp)
                Text(opponentScore.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuestionCard(statement: String, difficulty: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RESOLVER PARA X",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = statement,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            // Difficulty badge
            Surface(
                shape = RoundedCornerShape(50),
                color = when (difficulty.uppercase()) {
                    "EASY"   -> Color(0xFF16A34A)
                    "HARD"   -> Color(0xFFDC2626)
                    else     -> GoldAccent
                }
            ) {
                Text(
                    text = difficulty,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AnswerGrid(
    options: List<GameOption>,
    selectedOptionId: String?,
    correctOptionId: String?,
    isShowingResult: Boolean,
    onOptionSelected: (String) -> Unit
) {
    val rows = options.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowOptions.forEach { option ->
                    AnswerButton(
                        option = option,
                        selectedOptionId = selectedOptionId,
                        correctOptionId = correctOptionId,
                        isShowingResult = isShowingResult,
                        onSelected = onOptionSelected,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Pad row if odd number of options
                if (rowOptions.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AnswerButton(
    option: GameOption,
    selectedOptionId: String?,
    correctOptionId: String?,
    isShowingResult: Boolean,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = selectedOptionId == option.id
    val isCorrect  = correctOptionId == option.id

    // Color logic — implements optimistic update + visual rollback
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isShowingResult && isCorrect  -> GreenCorrect           // server confirmed correct
            isShowingResult && isSelected -> RedWrong               // rollback: user was wrong
            isSelected                   -> BluePrimary             // optimistic highlight
            else                         -> CardBg
        },
        animationSpec = tween(300),
        label = "answerBg_${option.id}"
    )
    val borderColor = when {
        isShowingResult && isCorrect -> GreenCorrect
        isShowingResult && isSelected -> RedWrong
        isSelected -> BluePrimary
        else -> Color(0xFF30363D)
    }
    val textColor = if (isSelected || (isShowingResult && isCorrect)) Color.White else Color.LightGray

    val isClickable = selectedOptionId == null && !isShowingResult

    Card(
        modifier = modifier
            .height(64.dp)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = isClickable) { onSelected(option.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = option.text,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = if (isSelected || isCorrect) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Game over overlay
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GameOverOverlay(uiState: GameUiState, onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC0D1117)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val isWinner = uiState.gameWinnerId != null

                Text(
                    text = if (uiState.gameOverReason == "opponent_disconnected")
                        "Victoria por abandono" else if (isWinner) "Partida terminada" else "Partida terminada",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (uiState.gameOverReason == "opponent_disconnected") "Tu oponente se fue"
                    else "Resultado final",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TÚ", color = Color.Gray, fontSize = 11.sp)
                        Text(
                            uiState.myScore.toString(),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("-", color = Color.Gray, fontSize = 24.sp)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.opponentUsername ?: "Oponente", color = Color.Gray, fontSize = 11.sp)
                        Text(
                            uiState.opponentScore.toString(),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("VOLVER AL LOBBY", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
