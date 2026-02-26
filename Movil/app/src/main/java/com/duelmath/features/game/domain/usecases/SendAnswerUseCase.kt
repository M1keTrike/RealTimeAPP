package com.duelmath.features.game.domain.usecases

import com.duelmath.features.game.domain.repositories.GameRepository
import javax.inject.Inject

class SendAnswerUseCase @Inject constructor(
    private val repository: GameRepository
) {
    operator fun invoke(questionId: String, optionId: String) =
        repository.sendAnswer(questionId, optionId)
}
