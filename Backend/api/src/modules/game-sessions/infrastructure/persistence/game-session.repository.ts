import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { IGameSessionRepository } from '../../domain/repositories/game-session.repository.interface';
import { GameSession } from '../../domain/entities/game-session.entity';
import { GameSessionOrmEntity } from './game-session.orm-entity';
import { GameSessionMapper } from './game-session.mapper';

@Injectable()
export class GameSessionTypeOrmRepository implements IGameSessionRepository {
  constructor(
    @InjectRepository(GameSessionOrmEntity)
    private readonly repository: Repository<GameSessionOrmEntity>,
  ) {}

  async save(session: GameSession): Promise<void> {
    const ormEntity = GameSessionMapper.toPersistence(session);
    await this.repository.save(ormEntity);
  }

  async findById(id: string): Promise<GameSession | null> {
    const ormEntity = await this.repository.findOne({ where: { id } });
    if (!ormEntity) return null;
    return GameSessionMapper.toDomain(ormEntity);
  }

  async findAvailableSession(): Promise<GameSession | null> {
    // Buscamos la sala más antigua que siga en estado WAITING
    const ormEntity = await this.repository.findOne({
      where: { status: 'WAITING' },
      order: { createdAt: 'ASC' }, 
    });
    
    if (!ormEntity) return null;
    return GameSessionMapper.toDomain(ormEntity);
  }

  async findInProgressByPlayers(userAId: string, userBId: string): Promise<GameSession | null> {
    const ormEntity = await this.repository.findOne({
      where: [
        { status: 'IN_PROGRESS', user1Id: userAId, user2Id: userBId },
        { status: 'IN_PROGRESS', user1Id: userBId, user2Id: userAId },
      ],
      order: { createdAt: 'DESC' },
    });

    if (!ormEntity) return null;
    return GameSessionMapper.toDomain(ormEntity);
  }

  async delete(id: string): Promise<void> {
    await this.repository.delete(id);
  }
}