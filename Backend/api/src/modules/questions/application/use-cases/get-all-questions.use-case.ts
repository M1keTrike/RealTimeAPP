import { Injectable, Inject } from '@nestjs/common';
import { Question } from '../../domain/entities/question.entity';
import { QUESTION_REPOSITORY } from '../../domain/repositories/question.repository.interface';
import type { IQuestionRepository } from '../../domain/repositories/question.repository.interface';

@Injectable()
export class GetAllQuestionsUseCase {
  constructor(
    @Inject(QUESTION_REPOSITORY)
    private readonly questionRepo: IQuestionRepository,
  ) {}

  async execute(): Promise<Question[]> {
    return this.questionRepo.findAll();
  }
}