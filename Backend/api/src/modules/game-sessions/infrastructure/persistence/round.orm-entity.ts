import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, ManyToOne, JoinColumn } from 'typeorm';
import { GameSessionOrmEntity } from './game-session.orm-entity';
import { QuestionOrmEntity } from 'src/modules/questions/infrastructure/persistence/question.orm-entity';
import { UserOrmEntity } from 'src/modules/users/infrastructure/persistence/user.orm-entity';

@Entity('rounds')
export class RoundOrmEntity {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ name: 'game_session_id', type: 'uuid' })
  gameSessionId: string;

  @Column({ name: 'question_id', type: 'uuid' })
  questionId: string;

  @ManyToOne(() => QuestionOrmEntity)
  @JoinColumn({ name: 'question_id' })
  question: QuestionOrmEntity;
  
  @Column({ name: 'winner_id', type: 'uuid', nullable: true })
  winnerId: string | null;

  @ManyToOne(() => UserOrmEntity)
  @JoinColumn({ name: 'winner_id' })
  winner: UserOrmEntity;

  @CreateDateColumn({ name: 'started_at' })
  startedAt: Date;

  @Column({ name: 'ended_at', type: 'timestamp', nullable: true })
  endedAt: Date | null;

  @ManyToOne(() => GameSessionOrmEntity, (session) => session.rounds)
  @JoinColumn({ name: 'game_session_id' })
  gameSession: GameSessionOrmEntity;
}