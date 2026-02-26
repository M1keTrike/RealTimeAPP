package com.duelmath.features.questions.data.datasources.remote.model

import com.duelmath.features.questions.domain.entities.QuestionDifficulty

data class QuestionOptionResponse(
    val id: String,
    val text: String,
)

data class QuestionResponse(
    val id: String,
    val statement: String,
    val difficulty: QuestionDifficulty,
    val options: List<QuestionOptionResponse>,
    val correctOptionId: String?,
)

data class CreateQuestionRequest(
    val statement: String,
    val difficulty: QuestionDifficulty,
    val options: List<String>,
    val correctOptionIndex: Int,
)

data class UpdateQuestionRequest(
    val statement: String,
    val difficulty: QuestionDifficulty,
    val options: List<String>,
    val correctOptionIndex: Int,
)