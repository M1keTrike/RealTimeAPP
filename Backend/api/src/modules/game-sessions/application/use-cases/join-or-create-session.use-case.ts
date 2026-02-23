import { Injectable, Inject } from '@nestjs/common';
import { GameSession } from '../../domain/entities/game-session.entity';
import { GAME_SESSION_REPOSITORY } from '../../domain/repositories/game-session.repository.interface';
import type { IGameSessionRepository } from '../../domain/repositories/game-session.repository.interface';
import { v4 as uuidv4 } from 'uuid';

@Injectable()
export class JoinOrCreateSessionUseCase {
  constructor(
    @Inject(GAME_SESSION_REPOSITORY)
    private readonly gameSessionRepo: IGameSessionRepository,
  ) {}

  async execute(userId: string): Promise<GameSession> {
    const availableSession = await this.gameSessionRepo.findAvailableSession();

    if (availableSession) {
      availableSession.joinPlayer(userId);
      await this.gameSessionRepo.save(availableSession);
      return availableSession;
    }

    const newSession = new GameSession(
      uuidv4(),
      userId
    );
    await this.gameSessionRepo.save(newSession);
    return newSession;
  }
}