package com.duelmath.features.questions.domain.usecases

import com.duelmath.features.questions.domain.repositories.QuestionsRepository
import javax.inject.Inject

class DeleteQuestionUseCase @Inject constructor(
    private val repository: QuestionsRepository,
) {
    suspend operator fun invoke(id: String): Result<Boolean> {
        return repository.deleteQuestion(id)
    }
}