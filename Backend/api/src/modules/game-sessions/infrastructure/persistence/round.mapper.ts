import { Round } from '../../domain/entities/round.entity';
import { RoundOrmEntity } from './round.orm-entity';

export class RoundMapper {
  static toDomain(ormEntity: RoundOrmEntity): Round {
    return new Round(
      ormEntity.id,
      ormEntity.gameSessionId,
      ormEntity.questionId,
      ormEntity.winnerId,
      ormEntity.startedAt,
      ormEntity.endedAt,
    );
  }

  static toPersistence(domainEntity: Round): RoundOrmEntity {
    const ormEntity = new RoundOrmEntity();
    ormEntity.id = domainEntity.id;
    ormEntity.gameSessionId = domainEntity.gameSessionId;
    ormEntity.questionId = domainEntity.questionId;
    ormEntity.winnerId = domainEntity.winnerId;
    ormEntity.startedAt = domainEntity.startedAt;
    ormEntity.endedAt = domainEntity.endedAt;
    return ormEntity;
  }
}