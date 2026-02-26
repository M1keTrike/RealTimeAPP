import { GameSession } from '../entities/game-session.entity';

export const GAME_SESSION_REPOSITORY = Symbol('GAME_SESSION_REPOSITORY');

export interface IGameSessionRepository {
  save(session: GameSession): Promise<void>;
  findById(id: string): Promise<GameSession | null>;
  findAvailableSession(): Promise<GameSession | null>;
  delete(id: string): Promise<void>;
}