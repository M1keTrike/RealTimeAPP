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

  async findAll(): Promise<Question[]> {
    const ormEntities = await this.repository.find({
      relations: ['options', 'correctOption'],
    });
    return ormEntities.map(orm => QuestionMapper.toDomain(orm));
  }

  async findById(id: string): Promise<Question | null> {
    const ormEntity = await this.repository.findOne({
      where: { id },
      relations: ['options', 'correctOption'],
    });
    if (!ormEntity) return null;
    return QuestionMapper.toDomain(ormEntity);
  }

  async delete(id: string): Promise<void> {
    const entity = await this.repository.findOne({ where: { id }, relations: ['options', 'correctOption'] });
    if (entity) {
      await this.repository.remove(entity); // remove dispara los cascades de eliminación
    }
  }
}