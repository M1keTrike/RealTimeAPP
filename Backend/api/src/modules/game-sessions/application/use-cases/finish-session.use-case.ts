import { Injectable, Inject, NotFoundException } from '@nestjs/common';
import { GAME_SESSION_REPOSITORY } from '../../domain/repositories/game-session.repository.interface';
import type { IGameSessionRepository } from '../../domain/repositories/game-session.repository.interface';

@Injectable()
export class FinishSessionUseCase {
  constructor(
    @Inject(GAME_SESSION_REPOSITORY)
    private readonly gameSessionRepo: IGameSessionRepository,
  ) {}

  async execute(sessionId: string, winnerId: string): Promise<void> {
    const session = await this.gameSessionRepo.findById(sessionId);
    if (!session) {
      throw new NotFoundException(`Sesión con ID ${sessionId} no encontrada`);
    }
    session.finishGame(winnerId);
    await this.gameSessionRepo.save(session);
  }
}
