import { Injectable, Inject } from '@nestjs/common';
import { GameSession } from '../../domain/entities/game-session.entity';
import { GameSessionStatus } from '../../domain/entities/game-session.entity';
import { GAME_SESSION_REPOSITORY } from '../../domain/repositories/game-session.repository.interface';
import type { IGameSessionRepository } from '../../domain/repositories/game-session.repository.interface';
import { v4 as uuidv4 } from 'uuid';

@Injectable()
export class JoinOrCreateSessionUseCase {
  private static readonly MATCH_WAIT_TIMEOUT_MS = 12000;
  private static readonly MATCH_POLL_INTERVAL_MS = 400;

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

    return this.waitUntilMatchedOrTimeout(newSession);
  }

  private async waitUntilMatchedOrTimeout(session: GameSession): Promise<GameSession> {
    const startedAt = Date.now();

    while (Date.now() - startedAt < JoinOrCreateSessionUseCase.MATCH_WAIT_TIMEOUT_MS) {
      const latest = await this.gameSessionRepo.findById(session.id);
      if (!latest) {
        return session;
      }

      if (latest.status === GameSessionStatus.IN_PROGRESS) {
        return latest;
      }

      await this.sleep(JoinOrCreateSessionUseCase.MATCH_POLL_INTERVAL_MS);
    }

    return session;
  }

  private sleep(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }
}