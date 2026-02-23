import { Question, QuestionOption, QuestionDifficulty } from '../../domain/entities/question.entity';
import { QuestionOrmEntity } from './question.orm-entity';
import { QuestionOptionOrmEntity } from './question-option.orm-entity';
import { CorrectOptionOrmEntity } from './correct-option.orm-entity';

export class QuestionMapper {
  static toDomain(ormEntity: QuestionOrmEntity): Question {
    const options = ormEntity.options?.map(opt => new QuestionOption(opt.id, opt.optionText)) || [];
    const correctOptionId = ormEntity.correctOption?.optionId || null;

    return new Question(
      ormEntity.id,
      ormEntity.statement,
      ormEntity.difficulty as QuestionDifficulty,
      options,
      correctOptionId,
    );
  }

  static toPersistence(domainEntity: Question): QuestionOrmEntity {
    const ormEntity = new QuestionOrmEntity();
    ormEntity.id = domainEntity.id;
    ormEntity.statement = domainEntity.statement;
    ormEntity.difficulty = domainEntity.difficulty;

    ormEntity.options = domainEntity.options.map((opt) => {
      const optOrm = new QuestionOptionOrmEntity();
      optOrm.id = opt.id;
      optOrm.questionId = domainEntity.id;
      optOrm.optionText = opt.text;
      return optOrm;
    });

    if (domainEntity.correctOptionId) {
      const correctOrm = new CorrectOptionOrmEntity();
      correctOrm.questionId = domainEntity.id;
      correctOrm.optionId = domainEntity.correctOptionId;
      ormEntity.correctOption = correctOrm;
    }

    return ormEntity;
  }
}