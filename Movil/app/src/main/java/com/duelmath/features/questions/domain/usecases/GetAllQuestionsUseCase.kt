package com.duelmath.features.questions.domain.usecases

import com.duelmath.features.questions.domain.entities.Question
import com.duelmath.features.questions.domain.repositories.QuestionsRepository
import javax.inject.Inject

class GetAllQuestionsUseCase @Inject constructor(
    private val repository: QuestionsRepository,
) {
    suspend operator fun invoke(): Result<List<Question>> {
        return repository.getAllQuestions()
    }
}