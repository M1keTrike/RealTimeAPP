package com.duelmath.features.game.domain.usecases

import com.duelmath.features.game.domain.entities.RoundResult
import com.duelmath.features.game.domain.repositories.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRoundResultsUseCase @Inject constructor(
    private val repository: GameRepository
) {
    operator fun invoke(): Flow<List<RoundResult>> = repository.observeRoundResults()
}
