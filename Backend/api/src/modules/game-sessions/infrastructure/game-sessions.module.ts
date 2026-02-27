import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { GameSessionsController } from '../presentation/game-sessions.controller';
import { JoinOrCreateSessionUseCase } from '../application/use-cases/join-or-create-session.use-case';
import { CreateSessionUseCase } from '../application/use-cases/create-session.use-case';
import { FinishSessionUseCase } from '../application/use-cases/finish-session.use-case';
import { GAME_SESSION_REPOSITORY } from '../domain/repositories/game-session.repository.interface';
import { GameSessionTypeOrmRepository } from './persistence/game-session.repository';
import { GameSessionOrmEntity } from './persistence/game-session.orm-entity';
import { RoundOrmEntity } from './persistence/round.orm-entity';
import { CancelSessionUseCase } from '../application/use-cases/CancelSessionUseCase';

@Module({
  imports: [TypeOrmModule.forFeature([GameSessionOrmEntity, RoundOrmEntity])],
  controllers: [GameSessionsController],
  providers: [
    JoinOrCreateSessionUseCase,
    CreateSessionUseCase,
    FinishSessionUseCase,
    CancelSessionUseCase,
    {
      provide: GAME_SESSION_REPOSITORY,
      useClass: GameSessionTypeOrmRepository,
    },
  ],
})
export class GameSessionsModule {}
