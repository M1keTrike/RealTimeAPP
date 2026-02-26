import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, OneToMany, JoinColumn, ManyToOne } from 'typeorm';
import { RoundOrmEntity } from './round.orm-entity';
import { UserOrmEntity } from 'src/modules/users/infrastructure/persistence/user.orm-entity';

@Entity('game_sessions')
export class GameSessionOrmEntity {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ name: 'user1_id', type: 'uuid' })
  user1Id: string;

  @ManyToOne(() => UserOrmEntity)
  @JoinColumn({ name: 'user1_id' })
  user1: UserOrmEntity;

  @Column({ name: 'user2_id', type: 'uuid', nullable: true })
  user2Id: string | null;

  @ManyToOne(() => UserOrmEntity)
  @JoinColumn({ name: 'user2_id' })
  user2: UserOrmEntity;

  @Column({ name: 'winner_id', type: 'uuid', nullable: true })
  winnerId: string | null;

  @Column({ type: 'varchar', length: 20, default: 'WAITING' })
  status: string;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;
  
  @OneToMany(() => RoundOrmEntity, (round) => round.gameSession, {
    cascade: true,
  })
  rounds: RoundOrmEntity[];
}