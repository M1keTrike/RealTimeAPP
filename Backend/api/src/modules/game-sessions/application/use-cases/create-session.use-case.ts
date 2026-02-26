import { Injectable, Inject } from '@nestjs/common';
import { v4 as uuidv4 } from 'uuid';
import { GameSession } from '../../domain/entities/game-session.entity';
import { GAME_SESSION_REPOSITORY } from '../../domain/repositories/game-session.repository.interface';
import type { IGameSessionRepository } from '../../domain/repositories/game-session.repository.interface';

@Injectable()
export class CreateSessionUseCase {
  constructor(
    @Inject(GAME_SESSION_REPOSITORY)
    private readonly gameSessionRepo: IGameSessionRepository,
  ) {}

  async execute(user1Id: string, user2Id: string): Promise<GameSession> {
    const existing = await this.gameSessionRepo.findInProgressByPlayers(user1Id, user2Id);
    if (existing) {
      return existing;
    }

    const session = new GameSession(uuidv4(), user1Id);
    session.joinPlayer(user2Id); // sets user2Id and status = IN_PROGRESS
    await this.gameSessionRepo.save(session);
    return session;
  }
}
