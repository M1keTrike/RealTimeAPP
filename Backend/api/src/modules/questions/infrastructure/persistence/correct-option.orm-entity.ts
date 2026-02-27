import { Entity, PrimaryGeneratedColumn, Column, OneToOne, JoinColumn } from 'typeorm';
import { QuestionOrmEntity } from './question.orm-entity';
import { QuestionOptionOrmEntity } from './question-option.orm-entity';

@Entity('correct_options')
export class CorrectOptionOrmEntity {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ name: 'question_id', type: 'uuid' })
  questionId: string;

  @Column({ name: 'option_id', type: 'uuid' })
  optionId: string;

  @OneToOne(() => QuestionOrmEntity, (question) => question.correctOption)
  @JoinColumn({ name: 'question_id' })
  question: QuestionOrmEntity;

  @OneToOne(() => QuestionOptionOrmEntity)
  @JoinColumn({ name: 'option_id' })
  option: QuestionOptionOrmEntity;
}