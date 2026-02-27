import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { IQuestionRepository } from '../../domain/repositories/question.repository.interface';
import { Question, QuestionDifficulty } from '../../domain/entities/question.entity';
import { QuestionOrmEntity } from './question.orm-entity';
import { QuestionMapper } from './question.mapper';
import { QuestionOptionOrmEntity } from './question-option.orm-entity';
import { CorrectOptionOrmEntity } from './correct-option.orm-entity';

@Injectable()
export class QuestionTypeOrmRepository implements IQuestionRepository {
  constructor(
    @InjectRepository(QuestionOrmEntity)
    private readonly repository: Repository<QuestionOrmEntity>,
  ) {}

  async save(question: Question): Promise<void> {
    await this.repository.manager.transaction(async (manager) => {
      const questionRepo = manager.getRepository(QuestionOrmEntity);
      const optionRepo = manager.getRepository(QuestionOptionOrmEntity);
      const correctOptionRepo = manager.getRepository(CorrectOptionOrmEntity);

      const exists = await questionRepo.exist({ where: { id: question.id } });

      if (!exists) {
        const ormEntity = QuestionMapper.toPersistence(question);
        await questionRepo.save(ormEntity);
        return;
      }

      await questionRepo.update(question.id, {
        statement: question.statement,
        difficulty: question.difficulty,
      });

      await correctOptionRepo.delete({ questionId: question.id });
      await optionRepo.delete({ questionId: question.id });

      const options = question.options.map((option) => {
        const optionOrm = new QuestionOptionOrmEntity();
        optionOrm.id = option.id;
        optionOrm.questionId = question.id;
        optionOrm.optionText = option.text;
        return optionOrm;
      });

      if (options.length > 0) {
        await optionRepo.save(options);
      }

      if (question.correctOptionId) {
        const correctOrm = new CorrectOptionOrmEntity();
        correctOrm.questionId = question.id;
        correctOrm.optionId = question.correctOptionId;
        await correctOptionRepo.save(correctOrm);
      }
    });
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
    await this.repository.manager.transaction(async (manager) => {
      const questionRepo = manager.getRepository(QuestionOrmEntity);
      const optionRepo = manager.getRepository(QuestionOptionOrmEntity);
      const correctOptionRepo = manager.getRepository(CorrectOptionOrmEntity);

      await correctOptionRepo.delete({ questionId: id });
      await optionRepo.delete({ questionId: id });
      await questionRepo.delete(id);
    });
  }
}