import { Question, QuestionDifficulty } from '../entities/question.entity';

export const QUESTION_REPOSITORY = Symbol('QUESTION_REPOSITORY');

export interface IQuestionRepository {
  save(question: Question): Promise<void>;
  getRandomByDifficulty(difficulty: QuestionDifficulty): Promise<Question | null>;
  findAll(): Promise<Question[]>;
  findById(id: string): Promise<Question | null>;
  delete(id: string): Promise<void>;
}