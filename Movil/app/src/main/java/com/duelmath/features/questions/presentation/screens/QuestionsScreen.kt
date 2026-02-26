package com.duelmath.features.questions.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duelmath.core.ui.theme.BorderWhite
import com.duelmath.core.ui.theme.DarkBackground
import com.duelmath.core.ui.theme.InputBackground
import com.duelmath.core.ui.theme.TextGray
import com.duelmath.features.questions.domain.entities.Question
import com.duelmath.features.questions.domain.entities.QuestionDifficulty
import com.duelmath.features.questions.presentation.viewmodels.QuestionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionsScreen(
    onBack: () -> Unit,
    viewModel: QuestionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Administrar preguntas", color = Color.White) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Volver", color = TextGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                ),
            )
        },
    ) { innerPadding ->
        if (!uiState.isAdmin) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Solo administradores pueden gestionar preguntas.",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = if (uiState.editingQuestionId == null) "Crear pregunta" else "Editar pregunta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.statementInput,
                    onValueChange = viewModel::onStatementChange,
                    label = { Text("Enunciado") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        value = uiState.difficultyInput.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dificultad") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = fieldColors(),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        QuestionDifficulty.entries.forEach { difficulty ->
                            DropdownMenuItem(
                                text = { Text(difficulty.name) },
                                onClick = {
                                    viewModel.onDifficultyChange(difficulty)
                                    expanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.optionAInput,
                    onValueChange = viewModel::onOptionAChange,
                    label = { Text("Opción 0") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.optionBInput,
                    onValueChange = viewModel::onOptionBChange,
                    label = { Text("Opción 1") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.optionCInput,
                    onValueChange = viewModel::onOptionCChange,
                    label = { Text("Opción 2") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.optionDInput,
                    onValueChange = viewModel::onOptionDChange,
                    label = { Text("Opción 3") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.correctOptionIndexInput,
                    onValueChange = viewModel::onCorrectOptionIndexChange,
                    label = { Text("Índice correcto (0 a n)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = viewModel::saveQuestion,
                        enabled = !uiState.isLoading,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(if (uiState.editingQuestionId == null) "Crear" else "Actualizar")
                    }

                    OutlinedButton(
                        onClick = viewModel::clearForm,
                        enabled = !uiState.isLoading,
                        border = BorderStroke(1.dp, BorderWhite),
                    ) {
                        Text("Limpiar", color = TextGray)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Catálogo de preguntas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            items(uiState.questions, key = { it.id }) { question ->
                QuestionItem(
                    question = question,
                    onEdit = { viewModel.startEditing(question) },
                    onDelete = { viewModel.deleteQuestion(question.id) },
                    isLoading = uiState.isLoading,
                )
            }
        }
    }
}

@Composable
private fun QuestionItem(
    question: Question,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isLoading: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = InputBackground),
        border = BorderStroke(1.dp, BorderWhite),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = question.statement,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(text = "Dificultad: ${question.difficulty.name}", color = TextGray)
            Text(text = "Opciones: ${question.options.joinToString { it.text }}", color = TextGray)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEdit,
                    enabled = !isLoading,
                    border = BorderStroke(1.dp, BorderWhite),
                ) {
                    Text("Editar", color = Color.White)
                }
                OutlinedButton(
                    onClick = onDelete,
                    enabled = !isLoading,
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                ) {
                    Text("Eliminar", color = Color(0xFFEF4444))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun fieldColors() = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
    focusedContainerColor = InputBackground,
    unfocusedContainerColor = InputBackground,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color.White.copy(alpha = 0.2f),
    unfocusedBorderColor = BorderWhite,
    focusedLabelColor = TextGray,
    unfocusedLabelColor = TextGray,
)