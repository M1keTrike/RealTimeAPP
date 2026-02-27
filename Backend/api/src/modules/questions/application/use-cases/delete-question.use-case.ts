import { Injectable, Inject, NotFoundException } from '@nestjs/common';
import { QUESTION_REPOSITORY } from '../../domain/repositories/question.repository.interface';
import type { IQuestionRepository } from '../../domain/repositories/question.repository.interface';

@Injectable()
export class DeleteQuestionUseCase {
  constructor(
    @Inject(QUESTION_REPOSITORY)
    private readonly questionRepo: IQuestionRepository,
  ) {}

  async execute(id: string): Promise<void> {
    const question = await this.questionRepo.findById(id);
    if (!question) {
      throw new NotFoundException(`Pregunta con ID ${id} no encontrada`);
    }
    await this.questionRepo.delete(id);
  }
}