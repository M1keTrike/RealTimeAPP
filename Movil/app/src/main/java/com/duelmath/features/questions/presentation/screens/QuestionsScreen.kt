package com.duelmath.features.questions.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
        floatingActionButton = {
            if (uiState.isAdmin) {
                FloatingActionButton(
                    onClick = {
                        viewModel.openCreateEditor()
                    },
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White,
                ) {
                    Text("+")
                }
            }
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
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Catálogo de preguntas (${uiState.questions.size})",
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

        if (uiState.showEditor) {
            QuestionEditorSheet(
                isEditing = uiState.editingQuestionId != null,
                statement = uiState.statementInput,
                difficulty = uiState.difficultyInput,
                optionA = uiState.optionAInput,
                optionB = uiState.optionBInput,
                optionC = uiState.optionCInput,
                optionD = uiState.optionDInput,
                correctOptionIndex = uiState.correctOptionIndexInput,
                isLoading = uiState.isLoading,
                onStatementChange = viewModel::onStatementChange,
                onDifficultyChange = viewModel::onDifficultyChange,
                onOptionAChange = viewModel::onOptionAChange,
                onOptionBChange = viewModel::onOptionBChange,
                onOptionCChange = viewModel::onOptionCChange,
                onOptionDChange = viewModel::onOptionDChange,
                onCorrectOptionIndexChange = viewModel::onCorrectOptionIndexChange,
                onSave = viewModel::saveQuestion,
                onClear = viewModel::clearForm,
                onDismiss = viewModel::closeEditor,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionEditorSheet(
    isEditing: Boolean,
    statement: String,
    difficulty: QuestionDifficulty,
    optionA: String,
    optionB: String,
    optionC: String,
    optionD: String,
    correctOptionIndex: String,
    isLoading: Boolean,
    onStatementChange: (String) -> Unit,
    onDifficultyChange: (QuestionDifficulty) -> Unit,
    onOptionAChange: (String) -> Unit,
    onOptionBChange: (String) -> Unit,
    onOptionCChange: (String) -> Unit,
    onOptionDChange: (String) -> Unit,
    onCorrectOptionIndexChange: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkBackground,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    text = if (isEditing) "Editar pregunta" else "Crear pregunta",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }

            item {
                OutlinedTextField(
                    value = statement,
                    onValueChange = onStatementChange,
                    label = { Text("Enunciado") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        value = difficulty.name,
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
                        QuestionDifficulty.entries.forEach { entry ->
                            DropdownMenuItem(
                                text = { Text(entry.name) },
                                onClick = {
                                    onDifficultyChange(entry)
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = optionA,
                    onValueChange = onOptionAChange,
                    label = { Text("Opción 0") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
            }
            item {
                OutlinedTextField(
                    value = optionB,
                    onValueChange = onOptionBChange,
                    label = { Text("Opción 1") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
            }
            item {
                OutlinedTextField(
                    value = optionC,
                    onValueChange = onOptionCChange,
                    label = { Text("Opción 2") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
            }
            item {
                OutlinedTextField(
                    value = optionD,
                    onValueChange = onOptionDChange,
                    label = { Text("Opción 3") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
            }
            item {
                OutlinedTextField(
                    value = correctOptionIndex,
                    onValueChange = onCorrectOptionIndexChange,
                    label = { Text("Índice correcto (0 a n)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onSave,
                        enabled = !isLoading,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(if (isEditing) "Actualizar" else "Crear")
                    }

                    OutlinedButton(
                        onClick = onClear,
                        enabled = !isLoading,
                        border = BorderStroke(1.dp, BorderWhite),
                    ) {
                        Text("Limpiar", color = TextGray)
                    }
                }
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