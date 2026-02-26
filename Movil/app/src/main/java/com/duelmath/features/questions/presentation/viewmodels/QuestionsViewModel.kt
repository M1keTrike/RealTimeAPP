package com.duelmath.features.questions.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelmath.features.auth.data.datasources.local.AuthLocalDataSource
import com.duelmath.features.auth.domain.entities.UserRole
import com.duelmath.features.questions.domain.entities.Question
import com.duelmath.features.questions.domain.entities.QuestionDifficulty
import com.duelmath.features.questions.domain.usecases.CreateQuestionUseCase
import com.duelmath.features.questions.domain.usecases.DeleteQuestionUseCase
import com.duelmath.features.questions.domain.usecases.GetAllQuestionsUseCase
import com.duelmath.features.questions.domain.usecases.UpdateQuestionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class QuestionsViewModel @Inject constructor(
    private val getAllQuestionsUseCase: GetAllQuestionsUseCase,
    private val createQuestionUseCase: CreateQuestionUseCase,
    private val updateQuestionUseCase: UpdateQuestionUseCase,
    private val deleteQuestionUseCase: DeleteQuestionUseCase,
    private val authLocalDataSource: AuthLocalDataSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionsUiState())
    val uiState: StateFlow<QuestionsUiState> = _uiState.asStateFlow()

    init {
        checkAdminAndLoad()
    }

    private fun checkAdminAndLoad() {
        viewModelScope.launch {
            val role = authLocalDataSource.getUserRole()
            val isAdmin = role == UserRole.ADMIN.value
            _uiState.update { it.copy(isAdmin = isAdmin) }

            if (isAdmin) {
                loadQuestions()
            } else {
                _uiState.update { it.copy(errorMessage = "Acceso denegado: solo administradores") }
            }
        }
    }

    fun loadQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val result = getAllQuestionsUseCase()
            result.onSuccess { questions ->
                _uiState.update { it.copy(isLoading = false, questions = questions) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    fun onStatementChange(value: String) {
        _uiState.update { it.copy(statementInput = value) }
    }

    fun onDifficultyChange(value: QuestionDifficulty) {
        _uiState.update { it.copy(difficultyInput = value) }
    }

    fun onOptionAChange(value: String) {
        _uiState.update { it.copy(optionAInput = value) }
    }

    fun onOptionBChange(value: String) {
        _uiState.update { it.copy(optionBInput = value) }
    }

    fun onOptionCChange(value: String) {
        _uiState.update { it.copy(optionCInput = value) }
    }

    fun onOptionDChange(value: String) {
        _uiState.update { it.copy(optionDInput = value) }
    }

    fun onCorrectOptionIndexChange(value: String) {
        val filtered = value.filter { it.isDigit() }
        _uiState.update { it.copy(correctOptionIndexInput = filtered) }
    }

    fun startEditing(question: Question) {
        val selectedIndex = question.options.indexOfFirst { option -> option.id == question.correctOptionId }
            .coerceAtLeast(0)

        _uiState.update {
            it.copy(
                editingQuestionId = question.id,
                statementInput = question.statement,
                difficultyInput = question.difficulty,
                optionAInput = question.options.getOrNull(0)?.text.orEmpty(),
                optionBInput = question.options.getOrNull(1)?.text.orEmpty(),
                optionCInput = question.options.getOrNull(2)?.text.orEmpty(),
                optionDInput = question.options.getOrNull(3)?.text.orEmpty(),
                correctOptionIndexInput = selectedIndex.toString(),
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun clearForm() {
        _uiState.update {
            it.copy(
                editingQuestionId = null,
                statementInput = "",
                difficultyInput = QuestionDifficulty.MEDIUM,
                optionAInput = "",
                optionBInput = "",
                optionCInput = "",
                optionDInput = "",
                correctOptionIndexInput = "0",
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun saveQuestion() {
        val current = _uiState.value
        if (!current.isAdmin) {
            _uiState.update { it.copy(errorMessage = "Acceso denegado: solo administradores") }
            return
        }

        val options = listOf(
            current.optionAInput.trim(),
            current.optionBInput.trim(),
            current.optionCInput.trim(),
            current.optionDInput.trim(),
        ).filter { it.isNotBlank() }

        val correctIndex = current.correctOptionIndexInput.toIntOrNull()
        if (current.statementInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El enunciado es obligatorio") }
            return
        }

        if (options.size < 2) {
            _uiState.update { it.copy(errorMessage = "Debe haber al menos 2 opciones") }
            return
        }

        if (correctIndex == null || correctIndex !in options.indices) {
            _uiState.update { it.copy(errorMessage = "Índice de respuesta correcta inválido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val result = if (current.editingQuestionId == null) {
                createQuestionUseCase(
                    statement = current.statementInput.trim(),
                    difficulty = current.difficultyInput,
                    options = options,
                    correctOptionIndex = correctIndex,
                )
            } else {
                updateQuestionUseCase(
                    id = current.editingQuestionId,
                    statement = current.statementInput.trim(),
                    difficulty = current.difficultyInput,
                    options = options,
                    correctOptionIndex = correctIndex,
                )
            }

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = if (current.editingQuestionId == null) {
                            "Pregunta creada correctamente"
                        } else {
                            "Pregunta actualizada correctamente"
                        },
                    )
                }
                clearForm()
                loadQuestions()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    fun deleteQuestion(questionId: String) {
        val current = _uiState.value
        if (!current.isAdmin) {
            _uiState.update { it.copy(errorMessage = "Acceso denegado: solo administradores") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = deleteQuestionUseCase(questionId)

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Pregunta eliminada correctamente",
                    )
                }
                loadQuestions()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}