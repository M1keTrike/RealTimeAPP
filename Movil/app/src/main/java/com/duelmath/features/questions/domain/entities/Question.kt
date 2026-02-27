package com.duelmath.features.questions.domain.entities

enum class QuestionDifficulty {
    EASY,
    MEDIUM,
    HARD,
    PRO,
}

data class QuestionOption(
    val id: String,
    val text: String,
)

data class Question(
    val id: String,
    val statement: String,
    val difficulty: QuestionDifficulty,
    val options: List<QuestionOption>,
    val correctOptionId: String?,
)