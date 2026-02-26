package com.duelmath.features.questions.domain.repositories

import com.duelmath.features.questions.domain.entities.Question
import com.duelmath.features.questions.domain.entities.QuestionDifficulty

interface QuestionsRepository {
    suspend fun getAllQuestions(): Result<List<Question>>
    suspend fun createQuestion(
        statement: String,
        difficulty: QuestionDifficulty,
        options: List<String>,
        correctOptionIndex: Int,
    ): Result<Question>

    suspend fun updateQuestion(
        id: String,
        statement: String,
        difficulty: QuestionDifficulty,
        options: List<String>,
        correctOptionIndex: Int,
    ): Result<Question>

    suspend fun deleteQuestion(id: String): Result<Boolean>
}