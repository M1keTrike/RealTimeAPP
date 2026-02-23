import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, ManyToOne, JoinColumn } from 'typeorm';
import { GameSessionOrmEntity } from './game-session.orm-entity';

@Entity('rounds')
export class RoundOrmEntity {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ name: 'game_session_id', type: 'uuid' })
  gameSessionId: string;

  @Column({ name: 'question_id', type: 'uuid' })
  questionId: string;

  @Column({ name: 'winner_id', type: 'uuid', nullable: true })
  winnerId: string | null;

  @CreateDateColumn({ name: 'started_at' })
  startedAt: Date;

  @Column({ name: 'ended_at', type: 'timestamp', nullable: true })
  endedAt: Date | null;

  @ManyToOne(() => GameSessionOrmEntity, (session) => session.rounds)
  @JoinColumn({ name: 'game_session_id' })
  gameSession: GameSessionOrmEntity;
}