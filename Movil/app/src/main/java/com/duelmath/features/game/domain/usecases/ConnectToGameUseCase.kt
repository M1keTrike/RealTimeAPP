package com.duelmath.features.game.domain.usecases

import com.duelmath.features.game.domain.repositories.GameRepository
import javax.inject.Inject

class ConnectToGameUseCase @Inject constructor(
    private val repository: GameRepository
) {
    operator fun invoke(token: String) = repository.connect(token)
}
