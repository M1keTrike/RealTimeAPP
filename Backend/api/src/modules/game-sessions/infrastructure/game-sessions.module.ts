import { Module } from '@nestjs/common';
import { GameSessionsController } from '../presentation/game-sessions.controller';
import { JoinOrCreateSessionUseCase } from '../application/use-cases/join-or-create-session.use-case';
import { GAME_SESSION_REPOSITORY } from '../domain/repositories/game-session.repository.interface';

// Un mock temporal para que compile y funcione sin base de datos real aún
const mockRepository = {
  save: jest.fn(),
  findById: jest.fn(),
  findAvailableSession: jest.fn().mockResolvedValue(null), 
};

@Module({
  controllers: [GameSessionsController],
  providers: [
    JoinOrCreateSessionUseCase,
    {
      provide: GAME_SESSION_REPOSITORY,
      useValue: mockRepository,
    },
  ],
})
export class GameSessionsModule {}