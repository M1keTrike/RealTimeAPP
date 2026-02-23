import { GameSession, GameSessionStatus } from '../../domain/entities/game-session.entity';
import { GameSessionOrmEntity } from './game-session.orm-entity';

export class GameSessionMapper {
  static toDomain(ormEntity: GameSessionOrmEntity): GameSession {
    return new GameSession(
      ormEntity.id,
      ormEntity.user1Id,
      ormEntity.user2Id,
      ormEntity.winnerId,
      ormEntity.status as GameSessionStatus,
      ormEntity.createdAt,
    );
  }

  static toPersistence(domainEntity: GameSession): GameSessionOrmEntity {
    const ormEntity = new GameSessionOrmEntity();
    ormEntity.id = domainEntity.id;
    ormEntity.user1Id = domainEntity.user1Id;
    ormEntity.user2Id = domainEntity.user2Id;
    ormEntity.winnerId = domainEntity.winnerId;
    ormEntity.status = domainEntity.status;
    ormEntity.createdAt = domainEntity.createdAt;
    return ormEntity;
  }
}