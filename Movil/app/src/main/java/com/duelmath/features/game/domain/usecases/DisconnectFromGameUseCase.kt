package com.duelmath.features.game.domain.usecases

import com.duelmath.features.game.domain.repositories.GameRepository
import javax.inject.Inject

class DisconnectFromGameUseCase @Inject constructor(
    private val repository: GameRepository
) {
    operator fun invoke() = repository.disconnect()
}
