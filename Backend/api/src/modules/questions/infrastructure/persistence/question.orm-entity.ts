import { Entity, PrimaryGeneratedColumn, Column, OneToMany, OneToOne } from 'typeorm';
import { QuestionOptionOrmEntity } from './question-option.orm-entity';
import { CorrectOptionOrmEntity } from './correct-option.orm-entity';
@Entity('questions')
export class QuestionOrmEntity {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ type: 'text' })
  statement: string;

  @Column({ type: 'varchar', length: 20 })
  difficulty: string;

  @OneToMany(() => QuestionOptionOrmEntity, (option) => option.question, { cascade: true })
  options: QuestionOptionOrmEntity[];

  @OneToOne(() => CorrectOptionOrmEntity, (correctOption) => correctOption.question, { cascade: true })
  correctOption: CorrectOptionOrmEntity;
}