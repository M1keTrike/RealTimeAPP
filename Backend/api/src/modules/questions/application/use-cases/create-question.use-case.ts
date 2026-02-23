import { Injectable, Inject } from '@nestjs/common';
import { v4 as uuidv4 } from 'uuid';
import { CreateQuestionDto } from '../dtos/create-question.dto';
import { Question, QuestionOption, QuestionDifficulty } from '../../domain/entities/question.entity';
import type { IQuestionRepository } from '../../domain/repositories/question.repository.interface';
import { QUESTION_REPOSITORY } from '../../domain/repositories/question.repository.interface';

@Injectable()
export class CreateQuestionUseCase {
  constructor(
    @Inject(QUESTION_REPOSITORY)
    private readonly questionRepo: IQuestionRepository,
  ) {}

  async execute(dto: CreateQuestionDto): Promise<Question> {
    const options = dto.options.map(text => new QuestionOption(uuidv4(), text));
    
    const correctOptionId = options[dto.correctOptionIndex]?.id;
    if (!correctOptionId) {
      throw new Error('El índice de la respuesta correcta no es válido.');
    }

    const question = new Question(
      uuidv4(),
      dto.statement,
      dto.difficulty,
      options,
      correctOptionId
    );

    await this.questionRepo.save(question);
    return question;
  }
}