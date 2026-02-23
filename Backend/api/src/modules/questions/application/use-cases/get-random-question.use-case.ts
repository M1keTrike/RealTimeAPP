import { Injectable, Inject, NotFoundException } from '@nestjs/common';
import { Question, QuestionDifficulty } from '../../domain/entities/question.entity';
import type { IQuestionRepository } from '../../domain/repositories/question.repository.interface';
import { QUESTION_REPOSITORY } from '../../domain/repositories/question.repository.interface';

@Injectable()
export class GetRandomQuestionUseCase {
  constructor(
    @Inject(QUESTION_REPOSITORY)
    private readonly questionRepo: IQuestionRepository,
  ) {}

  async execute(difficulty: QuestionDifficulty): Promise<Question> {
    const question = await this.questionRepo.getRandomByDifficulty(difficulty);
    if (!question) {
      throw new NotFoundException(`No hay preguntas disponibles para la dificultad ${difficulty}`);
    }
    return question;
  }
}