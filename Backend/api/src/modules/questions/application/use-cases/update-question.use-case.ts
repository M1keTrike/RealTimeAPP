import { Injectable, Inject, NotFoundException } from '@nestjs/common';
import { v4 as uuidv4 } from 'uuid';
import { UpdateQuestionDto } from '../dtos/update-question.dto';
import { Question, QuestionOption } from '../../domain/entities/question.entity';
import { QUESTION_REPOSITORY } from '../../domain/repositories/question.repository.interface';
import type { IQuestionRepository } from '../../domain/repositories/question.repository.interface';

@Injectable()
export class UpdateQuestionUseCase {
  constructor(
    @Inject(QUESTION_REPOSITORY)
    private readonly questionRepo: IQuestionRepository,
  ) {}

  async execute(id: string, dto: UpdateQuestionDto): Promise<Question> {
    const question = await this.questionRepo.findById(id);
    if (!question) {
      throw new NotFoundException(`Pregunta con ID ${id} no encontrada`);
    }

    if (dto.statement) question.statement = dto.statement;
    if (dto.difficulty) question.difficulty = dto.difficulty;

    if (dto.options && dto.correctOptionIndex !== undefined) {
      const newOptions = dto.options.map(text => new QuestionOption(uuidv4(), text));
      const correctOptionId = newOptions[dto.correctOptionIndex]?.id;
      
      if (!correctOptionId) {
        throw new Error('El índice de la respuesta correcta no es válido.');
      }

      question.options = newOptions;
      question.correctOptionId = correctOptionId;
    }

    await this.questionRepo.save(question);
    return question;
  }
}