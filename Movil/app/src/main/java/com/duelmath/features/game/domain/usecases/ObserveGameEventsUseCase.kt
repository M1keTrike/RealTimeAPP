package com.duelmath.features.game.domain.usecases

import com.duelmath.features.game.domain.entities.GameEvent
import com.duelmath.features.game.domain.repositories.GameRepository
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class ObserveGameEventsUseCase @Inject constructor(
    private val repository: GameRepository
) {
    operator fun invoke(): SharedFlow<GameEvent> = repository.events
}
