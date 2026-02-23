import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { GameSessionsController } from '../presentation/game-sessions.controller';
import { JoinOrCreateSessionUseCase } from '../application/use-cases/join-or-create-session.use-case';
import { GAME_SESSION_REPOSITORY } from '../domain/repositories/game-session.repository.interface';
import { GameSessionTypeOrmRepository } from './persistence/game-session.repository';
import { GameSessionOrmEntity } from './persistence/game-session.orm-entity';
import { RoundOrmEntity } from './persistence/round.orm-entity';

@Module({
  imports: [TypeOrmModule.forFeature([GameSessionOrmEntity, RoundOrmEntity])],
  controllers: [GameSessionsController],
  providers: [
    JoinOrCreateSessionUseCase,
    {
      provide: GAME_SESSION_REPOSITORY,
      useClass: GameSessionTypeOrmRepository,
    },
  ],
})
export class GameSessionsModule {}