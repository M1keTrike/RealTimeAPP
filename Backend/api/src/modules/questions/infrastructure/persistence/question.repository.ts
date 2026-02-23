import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { IQuestionRepository } from '../../domain/repositories/question.repository.interface';
import { Question, QuestionDifficulty } from '../../domain/entities/question.entity';
import { QuestionOrmEntity } from './question.orm-entity';
import { QuestionMapper } from './question.mapper';

@Injectable()
export class QuestionTypeOrmRepository implements IQuestionRepository {
  constructor(
    @InjectRepository(QuestionOrmEntity)
    private readonly repository: Repository<QuestionOrmEntity>,
  ) {}

  async save(question: Question): Promise<void> {
    const ormEntity = QuestionMapper.toPersistence(question);
    await this.repository.save(ormEntity);
  }

  async getRandomByDifficulty(difficulty: QuestionDifficulty): Promise<Question | null> {
    const ormEntity = await this.repository.createQueryBuilder('question')
      .leftJoinAndSelect('question.options', 'options')
      .leftJoinAndSelect('question.correctOption', 'correctOption')
      .where('question.difficulty = :difficulty', { difficulty })
      .orderBy('RANDOM()') 
      .getOne();

    if (!ormEntity) return null;
    return QuestionMapper.toDomain(ormEntity);
  }
}