import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { QuestionOrmEntity } from './persistence/question.orm-entity';
import { QuestionOptionOrmEntity } from './persistence/question-option.orm-entity';
import { CorrectOptionOrmEntity } from './persistence/correct-option.orm-entity';
import { QuestionTypeOrmRepository } from './persistence/question.repository';
import { QUESTION_REPOSITORY } from '../domain/repositories/question.repository.interface';
import { QuestionsController } from '../presentation/questions.controller';
import { CreateQuestionUseCase } from '../application/use-cases/create-question.use-case';
import { GetRandomQuestionUseCase } from '../application/use-cases/get-random-question.use-case';

@Module({
  imports: [
    TypeOrmModule.forFeature([
      QuestionOrmEntity, 
      QuestionOptionOrmEntity, 
      CorrectOptionOrmEntity
    ])
  ],
  controllers: [QuestionsController],
  providers: [
    CreateQuestionUseCase,
    GetRandomQuestionUseCase,
    {
      provide: QUESTION_REPOSITORY,
      useClass: QuestionTypeOrmRepository,
    },
  ],
  exports: [
    QUESTION_REPOSITORY,
    GetRandomQuestionUseCase,
  ]
})
export class QuestionsModule {}