package com.duelmath.features.questions.domain.usecases

import com.duelmath.features.questions.domain.entities.Question
import com.duelmath.features.questions.domain.entities.QuestionDifficulty
import com.duelmath.features.questions.domain.repositories.QuestionsRepository
import javax.inject.Inject

class CreateQuestionUseCase @Inject constructor(
    private val repository: QuestionsRepository,
) {
    suspend operator fun invoke(
        statement: String,
        difficulty: QuestionDifficulty,
        options: List<String>,
        correctOptionIndex: Int,
    ): Result<Question> {
        return repository.createQuestion(
            statement = statement,
            difficulty = difficulty,
            options = options,
            correctOptionIndex = correctOptionIndex,
        )
    }
}