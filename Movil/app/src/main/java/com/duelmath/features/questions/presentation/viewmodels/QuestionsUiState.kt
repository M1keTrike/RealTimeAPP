package com.duelmath.features.questions.presentation.viewmodels

import com.duelmath.features.questions.domain.entities.Question
import com.duelmath.features.questions.domain.entities.QuestionDifficulty

data class QuestionsUiState(
    val isLoading: Boolean = false,
    val isAdmin: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val questions: List<Question> = emptyList(),
    val editingQuestionId: String? = null,
    val statementInput: String = "",
    val difficultyInput: QuestionDifficulty = QuestionDifficulty.MEDIUM,
    val optionAInput: String = "",
    val optionBInput: String = "",
    val optionCInput: String = "",
    val optionDInput: String = "",
    val correctOptionIndexInput: String = "0",
)