import { Entity, PrimaryGeneratedColumn, Column, ManyToOne, JoinColumn } from 'typeorm';
import { QuestionOrmEntity } from './question.orm-entity';

@Entity('question_options')
export class QuestionOptionOrmEntity {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ name: 'question_id', type: 'uuid' })
  questionId: string;

  @Column({ name: 'option_text', type: 'varchar' })
  optionText: string;

  @ManyToOne(() => QuestionOrmEntity, (question) => question.options)
  @JoinColumn({ name: 'question_id' })
  question: QuestionOrmEntity;
}